package com.percy.espresso_java.lib;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import io.percy.espresso.lib.Tile;
import io.percy.espresso.metadata.Metadata;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TileTest {
    @Test
    public void testGetClientInfo() throws JSONException {
        Tile tile = new Tile("/tmp", 100, 120, 0, 0, false);
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(tile);
        JSONObject jsonTile = Tile.getTilesAsJson(tiles).get(0);
        Assert.assertEquals(jsonTile.getInt("statusBarHeight"), 100);
        Assert.assertEquals(jsonTile.getInt("navBarHeight"), 120);
        Assert.assertEquals(jsonTile.getInt("headerHeight"), 0);
        Assert.assertEquals(jsonTile.getInt("footerHeight"), 0);
        Assert.assertEquals(jsonTile.get("fullscreen"), false);
    }
}