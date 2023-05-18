package com.percy.espresso_java.providers;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.percy.espresso.lib.ScreenshotOptions;
import io.percy.espresso.lib.Tile;
import io.percy.espresso.metadata.Metadata;
import io.percy.espresso.providers.GenericProvider;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GenericProviderTest {

    @Test
    public void testGetTag() throws JSONException {
        GenericProvider genericProvider = new GenericProvider();
        genericProvider.setMetadata(new Metadata(new ScreenshotOptions()));

        Metadata metadata = genericProvider.getMetadata();
        JSONObject tile = genericProvider.getTag();
        Assert.assertEquals(tile.get("osName"), metadata.osName());
        Assert.assertEquals(tile.get("osVersion"), metadata.platformVersion());
        Assert.assertEquals(tile.get("width"), metadata.deviceScreenWidth());
        Assert.assertEquals(tile.get("height"), metadata.deviceScreenHeight());
        Assert.assertEquals(tile.get("orientation"), metadata.orientation());
    }

    @Test
    public void testcaptureTiles() {
        GenericProvider genericProvider = new GenericProvider();
        ScreenshotOptions options = new ScreenshotOptions();
        options.setNavBarHeight(200);
        options.setStatusBarHeight(100);
        genericProvider.setMetadata(new Metadata(options));

        Tile tile = genericProvider.captureTiles(false).get(0);
        Assert.assertEquals(tile.getStatusBarHeight().intValue(), 100);
        Assert.assertEquals(tile.getNavBarHeight().intValue(), 200);
        Assert.assertEquals(tile.getHeaderHeight().intValue(), 0);
        Assert.assertEquals(tile.getFooterHeight().intValue(), 0);
        Assert.assertEquals(tile.getFullScreen(), false);
    }

    @Test
    public void testScreenshot() throws JSONException {
        GenericProvider genericProvider = new GenericProvider();
        ScreenshotOptions options = new ScreenshotOptions();
        options.setNavBarHeight(200);
        options.setStatusBarHeight(100);
        genericProvider.screenshot("First SS", options);
    }

    @Test
    public void testGetSetMetadata() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("Device");
        Metadata metadata = new Metadata(options);
        GenericProvider genericProvider = new GenericProvider();
        genericProvider.setMetadata(metadata);
        Assert.assertEquals(genericProvider.getMetadata(), metadata);
    }

}
