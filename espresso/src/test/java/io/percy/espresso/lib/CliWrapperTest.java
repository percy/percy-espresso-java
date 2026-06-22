package io.percy.espresso.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.percy.espresso.testutil.StubHttpServer;

/**
 * Drives {@link CliWrapper} against a real in-process HTTP stub so every
 * response / version-gate / exception branch is exercised on the JVM with no
 * emulator and no real Percy CLI.
 */
public class CliWrapperTest {

    private StubHttpServer server;
    private String originalAddress;

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

    private void start(int status, String versionHeader, String body) throws IOException {
        server = new StubHttpServer(status, versionHeader == null ? null : "x-percy-core-version",
                versionHeader, body);
        CliWrapper.PERCY_SERVER_ADDRESS = server.getBaseUrl();
    }

    @Test
    public void testHealthcheckSuccessSupportedVersion() throws IOException {
        start(200, "1.27.0", "{\"link\":\"x\"}");
        assertEquals(true, new CliWrapper().healthcheck());
    }

    @Test
    public void testHealthcheckSuccessOldMinorVersionStillEnabled() throws IOException {
        // minorVersion < 24 -> logs warning but still returns true.
        start(200, "1.20.0", "{\"link\":\"x\"}");
        assertEquals(true, new CliWrapper().healthcheck());
    }

    @Test
    public void testHealthcheckUnsupportedMajorVersion() throws IOException {
        // majorVersion < 1 -> returns false.
        start(200, "0.40.0", "{\"link\":\"x\"}");
        assertEquals(false, new CliWrapper().healthcheck());
    }

    @Test
    public void testHealthcheckBadResponseCode() throws IOException {
        // Non-2xx -> IOException -> catch -> false.
        start(500, "1.27.0", "{\"link\":\"x\"}");
        assertEquals(false, new CliWrapper().healthcheck());
    }

    @Test
    public void testHealthcheckMissingVersionHeader() throws IOException {
        // No version header -> NullPointerException on version.split -> catch -> false.
        start(200, null, "{\"link\":\"x\"}");
        assertEquals(false, new CliWrapper().healthcheck());
    }

    @Test
    public void testHealthcheckServerNotRunning() {
        // Connection refused -> catch -> false.
        CliWrapper.PERCY_SERVER_ADDRESS = "http://127.0.0.1:1";
        assertEquals(false, new CliWrapper().healthcheck());
    }

    @Test
    public void testPostScreenshotSuccess() throws IOException {
        start(200, "1.27.0", "{\"link\":\"https://percy.io/build/1\"}");
        JSONObject tag = new JSONObject();
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(new Tile("content", 10, 20, 0, 0, false));

        String link = new CliWrapper().postScreenshot(
                "snap", tag, tiles, "http://debug", "tc", "labels");
        assertEquals("https://percy.io/build/1", link);
    }

    @Test
    public void testPostScreenshotBadResponseCode() throws IOException {
        start(500, "1.27.0", "fail");
        JSONObject tag = new JSONObject();
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(new Tile("content", 10, 20, 0, 0, false));

        String link = new CliWrapper().postScreenshot(
                "snap", tag, tiles, null, null, null);
        assertNull(link);
    }

    @Test
    public void testPostScreenshotServerNotRunning() {
        CliWrapper.PERCY_SERVER_ADDRESS = "http://127.0.0.1:1";
        JSONObject tag = new JSONObject();
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(new Tile("content", 10, 20, 0, 0, false));

        String link = new CliWrapper().postScreenshot(
                "snap", tag, tiles, null, null, null);
        assertNull(link);
    }
}
