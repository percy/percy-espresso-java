package io.percy.espresso.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import io.percy.espresso.lib.Cache;

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
