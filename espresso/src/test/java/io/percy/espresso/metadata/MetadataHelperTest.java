package io.percy.espresso.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import io.percy.espresso.lib.Cache;
import io.percy.espresso.testutil.StubHttpServer;

/**
 * Robolectric is required because the no-arg helpers read android.os.Build, and
 * the parseBufferReader fallback reads Build.MANUFACTURER / Build.MODEL.
 *
 * NOTE on devices.csv: the bundled resource is UTF-16 LE while
 * parseBufferReader reads it with the JVM default charset (UTF-8). The NUL
 * bytes that UTF-16 interleaves are stripped by sanitizedString(), so the
 * parsed device names still come out correct -- assertions below match the
 * ACTUAL parsed output, confirmed against the on-emulator androidTest suite.
 * The charset mismatch is a latent bug (it only works by accident of the
 * sanitizer), but production behavior is intentionally left unchanged here.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetadataHelperTest {

    @Before
    public void clearCache() {
        Cache.CACHE_MAP.clear();
    }

    @Test
    public void testHelperCanBeInstantiated() {
        // Covers the implicit default constructor (all members are static).
        assertNotNull(new MetadataHelper());
    }

    @Test
    public void testSanitizedString() {
        assertEquals("SM-123", MetadataHelper.sanitizedString("SM-12#3## "));
    }

    @Test
    public void testSanitizedStringKeepsAllowedChars() {
        assertEquals("a b1 ()._+-", MetadataHelper.sanitizedString("a b1 ()._+-"));
    }

    @Test
    public void testDeviceNameFromCSVBrandPlusMarketing() {
        // marketingName does NOT start with brand -> "brand marketing".
        assertEquals("Samsung Galaxy A14", MetadataHelper.deviceNameFromCSV("SM-A145F"));
    }

    @Test
    public void testDeviceNameFromCSVMarketingOnly() {
        // marketingName starts with brand -> marketing only.
        assertEquals("Redmi Note 11T Pro +", MetadataHelper.deviceNameFromCSV("22041216UC"));
    }

    @Test
    public void testDeviceNameFromCSVNoArgUsesBuildModel() {
        // Build.MODEL under Robolectric is "robolectric"; not in the CSV, so the
        // parseBufferReader end-of-stream fallback returns MANUFACTURER + MODEL.
        String name = MetadataHelper.deviceNameFromCSV();
        assertNotNull(name);
        assertEquals(android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL, name);
    }

    @Test
    public void testParseBufferReaderMatchBrandPrefixedMarketing() {
        // marketingName ("Pixel Pixel 6") starts with brand ("Pixel") -> marketing only.
        BufferedReader reader = new BufferedReader(new StringReader("Pixel,Pixel 6,oriole,G9S9B16\n"));
        assertEquals("Pixel 6", MetadataHelper.parseBufferReader(reader, "G9S9B16"));
    }

    @Test
    public void testParseBufferReaderMatchNonPrefixedMarketing() {
        BufferedReader reader = new BufferedReader(new StringReader("Acme,SuperPhone,codename,MODEL1\n"));
        assertEquals("Acme SuperPhone", MetadataHelper.parseBufferReader(reader, "MODEL1"));
    }

    @Test
    public void testParseBufferReaderSkipsMalformedRowsAndFallsBack() {
        // No row has exactly 4 columns matching MODEL -> end-of-stream fallback.
        BufferedReader reader = new BufferedReader(new StringReader("only,three,cols\nfive,col,row,here,extra\n"));
        String result = MetadataHelper.parseBufferReader(reader, "NOPE");
        assertEquals(android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL, result);
    }

    @Test
    public void testParseBufferReaderIOExceptionWrapped() {
        // A reader whose readLine throws IOException must surface as RuntimeException.
        BufferedReader broken = new BufferedReader(new StringReader("")) {
            @Override
            public String readLine() throws IOException {
                throw new IOException("boom");
            }
        };
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> MetadataHelper.parseBufferReader(broken, "X"));
        assertNotNull(ex.getCause());
    }

    /**
     * Exercises the remote-CSV fallback branch of {@code resolveDeviceNameFromCSV}.
     *
     * <p>The local lookup is forced to miss (returns null) so control enters the
     * {@code device == null} branch, and the REAL {@code openRemoteCsvReader()}
     * runs end-to-end -- {@code new URL(...)} plus {@code url.openStream()} -- but
     * pointed at a loopback {@link StubHttpServer} via the {@code remoteCsvUrl()}
     * seam instead of the live Google endpoint. The stub serves a single CSV row
     * which the real {@code parseBufferReader} then matches, proving the fallback
     * returns the parsed remote device name.
     */
    @Test
    public void testRemoteCsvFallbackSuccessPath() throws IOException {
        try (StubHttpServer server = new StubHttpServer(
                200, "Content-Type", "text/csv",
                "Acme,Acme RemotePhone,codename,REMOTE-MODEL\n")) {
            MetadataHelper helper = new MetadataHelper() {
                @Override
                protected String localLookup(String model) {
                    // Force a local miss so the remote fallback is taken.
                    return null;
                }
                @Override
                protected String remoteCsvUrl() {
                    // Redirect the real open/stream logic to the loopback stub.
                    return server.getBaseUrl() + "/supported_devices.csv";
                }
            };
            // marketingName ("Acme RemotePhone") starts with brand ("Acme") -> marketing only.
            assertEquals("Acme RemotePhone", helper.resolveDeviceNameFromCSV("REMOTE-MODEL"));
        }
    }

    /**
     * Exercises the {@code catch (IOException)} / {@code return null} tail of
     * {@code resolveDeviceNameFromCSV}. The local lookup misses, then the remote
     * reader throws an IOException (the same failure mode a real network outage
     * would produce), which the catch swallows (printStackTrace) and the method
     * returns null.
     */
    @Test
    public void testRemoteCsvFallbackIOExceptionReturnsNull() {
        MetadataHelper helper = new MetadataHelper() {
            @Override
            protected String localLookup(String model) {
                return null;
            }
            @Override
            protected BufferedReader openRemoteCsvReader() throws IOException {
                throw new IOException("simulated network failure");
            }
        };
        assertNull(helper.resolveDeviceNameFromCSV("ANY-MODEL"));
    }

    /**
     * Pins the production remote-CSV endpoint. Exercises the real
     * {@code remoteCsvUrl()} (the unoverridden constant) on a plain instance, so
     * the seam's default value is covered by behavior and a stray edit to the
     * Google Play URL would fail this test.
     */
    @Test
    public void testRemoteCsvUrlIsProductionEndpoint() {
        assertEquals("https://storage.googleapis.com/play_public/supported_devices.csv",
                new MetadataHelper().remoteCsvUrl());
    }

    @Test
    public void testValueFromStaticDevicesInfoPresent() {
        Integer val = MetadataHelper.valueFromStaticDevicesInfo("statusBarHeight",
                MetadataHelper.deviceNameFromCSV("SM-A115M").toLowerCase());
        assertEquals(65, val.intValue());
    }

    @Test
    public void testValueFromStaticDevicesInfoKeyMissingReturnsZero() {
        // Device present but key absent -> JSONException -> 0.
        Integer val = MetadataHelper.valueFromStaticDevicesInfo("navBarHeight", "redmi 10x 4g");
        assertEquals(0, val.intValue());
    }

    @Test
    public void testValueFromStaticDevicesInfoDeviceMissingReturnsZero() {
        Integer val = MetadataHelper.valueFromStaticDevicesInfo("statusBarHeight", "no such device");
        assertEquals(0, val.intValue());
    }

    @Test
    public void testGetDevicesJsonIsCachedAndParsed() throws IOException, org.json.JSONException {
        Cache.CACHE_MAP.clear();
        assertNotNull(MetadataHelper.getDevicesJson());
        // Second call returns the cached instance (covers the cache-hit branch).
        assertEquals(MetadataHelper.getDevicesJson(), MetadataHelper.getDevicesJson());
        assertEquals(100, MetadataHelper.getDevicesJson().getJSONObject("redmi 10x 4g").getInt("statusBarHeight"));
    }
}
