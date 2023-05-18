package io.percy.espresso.lib;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Tile {
    private String content;
    private Integer statusBarHeight;
    private Integer navBarHeight;
    private Integer headerHeight;
    private Integer footerHeight;
    private Boolean fullScreen;

    public Tile(String content, Integer statusBarHeight, Integer navBarHeight, Integer headerHeight,
            Integer footerHeight, Boolean fullScreen) {
        this.content = content;
        this.statusBarHeight = statusBarHeight;
        this.navBarHeight = navBarHeight;
        this.headerHeight = headerHeight;
        this.footerHeight = footerHeight;
        this.fullScreen = fullScreen;
    }

    public static List<JSONObject> getTilesAsJson(List<Tile> tilesList) throws JSONException {
        List<JSONObject> tiles = new ArrayList<JSONObject>();
        for (Tile tile : tilesList) {
            JSONObject tileData = new JSONObject();
            tileData.put("content", tile.content);
            tileData.put("statusBarHeight", tile.statusBarHeight);
            tileData.put("navBarHeight", tile.navBarHeight);
            tileData.put("headerHeight", tile.headerHeight);
            tileData.put("footerHeight", tile.footerHeight);
            tileData.put("fullscreen", tile.fullScreen);
            tiles.add(tileData);
        }
        return tiles;
    }

    public Integer getStatusBarHeight() {
        return statusBarHeight;
    }

    public Integer getNavBarHeight() {
        return navBarHeight;
    }

    public Integer getHeaderHeight() {
        return headerHeight;
    }

    public Integer getFooterHeight() {
        return footerHeight;
    }

    public Boolean getFullScreen() {
        return fullScreen;
    }

}
