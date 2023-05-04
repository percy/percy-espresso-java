package com.percy.espresso_java.providers;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import io.percy.espresso.AppPercy;
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
        genericProvider.setMetadata(new Metadata(null,null, null, null, null));

        Metadata metadata = genericProvider.getMetadata();
        JSONObject tile = genericProvider.getTag();
        Assert.assertEquals(tile.get("name"), metadata.deviceName());
        Assert.assertEquals(tile.get("osName"), metadata.osName());
        Assert.assertEquals(tile.get("osVersion"), metadata.platformVersion());
        Assert.assertEquals(tile.get("width"), metadata.deviceScreenWidth());
        Assert.assertEquals(tile.get("height"), metadata.deviceScreenHeight());
        Assert.assertEquals(tile.get("orientation"), metadata.orientation());
    }

    @Test
    public void testcaptureTiles() throws IOException, Exception {
        GenericProvider genericProvider = new GenericProvider();
        genericProvider.setMetadata(new Metadata(null, 100, 200, null, null));

        Tile tile = genericProvider.captureTiles(false).get(0);
        Assert.assertEquals(tile.getStatusBarHeight().intValue(), 100);
        Assert.assertEquals(tile.getNavBarHeight().intValue(), 200);
        Assert.assertEquals(tile.getHeaderHeight().intValue(), 0);
        Assert.assertEquals(tile.getFooterHeight().intValue(), 0);
        Assert.assertEquals(tile.getFullScreen(), false);
    }

    @Test
    public void testScreenshot() throws IOException, NoSuchAlgorithmException {
        GenericProvider genericProvider = new GenericProvider();
        genericProvider.screenshot("First SS", "Device", 120, 100, "portrait", false);
    }

    @Test
    public void testGetSetMetadata() {
        Metadata metadata = new Metadata("Device", null, null, null, null);
        GenericProvider genericProvider = new GenericProvider();
        genericProvider.setMetadata(metadata);
        Assert.assertEquals(genericProvider.getMetadata(), metadata);
    }

}