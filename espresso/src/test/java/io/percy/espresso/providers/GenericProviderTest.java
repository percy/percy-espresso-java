package io.percy.espresso.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.Screenshot;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.List;

import io.percy.espresso.lib.CliWrapper;
import io.percy.espresso.lib.ScreenshotOptions;
import io.percy.espresso.lib.Tile;
import io.percy.espresso.metadata.Metadata;
import io.percy.espresso.testutil.StubHttpServer;

/**
 * Robolectric provides shadow Bitmap / Base64. The androidx Screenshot.capture()
 * call cannot run without an emulator, so a behavior-preserving seam
 * (GenericProvider#captureBitmap) is overridden here to supply a fake Bitmap.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GenericProviderTest {

    private StubHttpServer server;
    private String originalAddress;

    /** GenericProvider whose bitmap source is a fake, so no emulator is needed. */
    private static class FakeBitmapProvider extends GenericProvider {
        @Override
        protected Bitmap captureBitmap() {
            return Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888);
        }
    }

    @Before
    public void setUp() {
        originalAddress = CliWrapper.PERCY_SERVER_ADDRESS;
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
        CliWrapper.PERCY_SERVER_ADDRESS = originalAddress;
    }

    @Test
    public void testGetTag() throws JSONException {
        GenericProvider provider = new FakeBitmapProvider();
        provider.setMetadata(new Metadata(new ScreenshotOptions()));
        Metadata metadata = provider.getMetadata();

        JSONObject tag = provider.getTag();
        assertEquals(metadata.deviceName(), tag.get("name"));
        assertEquals(metadata.osName(), tag.get("osName"));
        assertEquals(metadata.platformVersion(), tag.get("osVersion"));
        assertEquals(metadata.deviceScreenWidth(), tag.get("width"));
        assertEquals(metadata.deviceScreenHeight(), tag.get("height"));
        assertEquals(metadata.orientation(), tag.get("orientation"));
    }

    @Test
    public void testCaptureTiles() {
        GenericProvider provider = new FakeBitmapProvider();
        ScreenshotOptions options = new ScreenshotOptions();
        options.setNavBarHeight(200);
        options.setStatusBarHeight(100);
        provider.setMetadata(new Metadata(options));

        List<Tile> tiles = provider.captureTiles(false);
        Tile tile = tiles.get(0);
        assertEquals(1, tiles.size());
        assertEquals(100, tile.getStatusBarHeight().intValue());
        assertEquals(200, tile.getNavBarHeight().intValue());
        assertEquals(0, tile.getHeaderHeight().intValue());
        assertEquals(0, tile.getFooterHeight().intValue());
        assertEquals(Boolean.FALSE, tile.getFullScreen());
        assertNotNull(tile);
    }

    @Test
    public void testCaptureBitmapUsesScreenshotCapture() throws Exception {
        // Exercises the REAL captureBitmap() seam (the androidx Screenshot bridge)
        // by mocking the static Screenshot.capture() so the bitmap source is a
        // fake. This is the only way to drive that line without an emulator.
        Bitmap fakeBitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
        ScreenCapture screenCapture = Mockito.mock(ScreenCapture.class);
        when(screenCapture.getBitmap()).thenReturn(fakeBitmap);

        try (MockedStatic<Screenshot> mocked = mockStatic(Screenshot.class)) {
            mocked.when(Screenshot::capture).thenReturn(screenCapture);

            GenericProvider provider = new GenericProvider(); // real captureBitmap()
            ScreenshotOptions options = new ScreenshotOptions();
            options.setNavBarHeight(5);
            options.setStatusBarHeight(7);
            provider.setMetadata(new Metadata(options));

            List<Tile> tiles = provider.captureTiles(true);
            assertEquals(1, tiles.size());
            assertEquals(7, tiles.get(0).getStatusBarHeight().intValue());
            assertEquals(Boolean.TRUE, tiles.get(0).getFullScreen());
        }
    }

    @Test
    public void testSetAndGetMetadata() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("Device");
        Metadata metadata = new Metadata(options);
        GenericProvider provider = new FakeBitmapProvider();
        provider.setMetadata(metadata);
        assertSame(metadata, provider.getMetadata());
    }

    @Test
    public void testScreenshotFullFlow() throws JSONException, IOException {
        startScreenshotServer();
        GenericProvider provider = new FakeBitmapProvider();
        ScreenshotOptions options = new ScreenshotOptions();
        options.setNavBarHeight(200);
        options.setStatusBarHeight(100);
        options.setTestCase("case");
        options.setLabels("label");

        String link = provider.screenshot("My Screenshot", options);
        assertEquals("https://percy.io/build/42", link);
        // Metadata is set as a side effect of screenshot().
        assertNotNull(provider.getMetadata());
    }

    private void startScreenshotServer() throws IOException {
        server = new StubHttpServer(200, null, null, "{\"link\":\"https://percy.io/build/42\"}");
        CliWrapper.PERCY_SERVER_ADDRESS = server.getBaseUrl();
    }
}
