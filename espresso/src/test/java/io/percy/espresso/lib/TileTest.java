package io.percy.espresso.lib;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TileTest {

    @Test
    public void testGetTilesAsJson() throws JSONException {
        Tile tile = new Tile("/tmp", 100, 120, 0, 0, false);
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(tile);

        JSONObject jsonTile = Tile.getTilesAsJson(tiles).get(0);
        assertEquals("/tmp", jsonTile.getString("content"));
        assertEquals(100, jsonTile.getInt("statusBarHeight"));
        assertEquals(120, jsonTile.getInt("navBarHeight"));
        assertEquals(0, jsonTile.getInt("headerHeight"));
        assertEquals(0, jsonTile.getInt("footerHeight"));
        assertEquals(false, jsonTile.get("fullscreen"));
    }

    @Test
    public void testGetTilesAsJsonMultiple() throws JSONException {
        List<Tile> tiles = new ArrayList<Tile>();
        tiles.add(new Tile("a", 1, 2, 3, 4, true));
        tiles.add(new Tile("b", 5, 6, 7, 8, false));

        List<JSONObject> jsonTiles = Tile.getTilesAsJson(tiles);
        assertEquals(2, jsonTiles.size());
        assertEquals("a", jsonTiles.get(0).getString("content"));
        assertEquals(true, jsonTiles.get(0).get("fullscreen"));
        assertEquals("b", jsonTiles.get(1).getString("content"));
        assertEquals(false, jsonTiles.get(1).get("fullscreen"));
    }

    @Test
    public void testGetTilesAsJsonEmpty() throws JSONException {
        List<JSONObject> jsonTiles = Tile.getTilesAsJson(new ArrayList<Tile>());
        assertEquals(0, jsonTiles.size());
    }

    @Test
    public void testGetters() {
        Tile tile = new Tile("content", 11, 22, 33, 44, true);
        assertEquals(Integer.valueOf(11), tile.getStatusBarHeight());
        assertEquals(Integer.valueOf(22), tile.getNavBarHeight());
        assertEquals(Integer.valueOf(33), tile.getHeaderHeight());
        assertEquals(Integer.valueOf(44), tile.getFooterHeight());
        assertEquals(Boolean.TRUE, tile.getFullScreen());
    }
}
