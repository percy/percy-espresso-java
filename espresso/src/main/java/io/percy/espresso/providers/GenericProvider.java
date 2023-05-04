package io.percy.espresso.providers;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.util.Base64;

import androidx.test.runner.screenshot.Screenshot;

import io.percy.espresso.lib.CliWrapper;
import io.percy.espresso.lib.Tile;
import io.percy.espresso.metadata.Metadata;

public class GenericProvider {
    private Metadata metadata;
    private final CliWrapper cliWrapper;

    public GenericProvider() {
        this.cliWrapper = new CliWrapper();
    }

    public JSONObject getTag() {
        JSONObject tag = new JSONObject();
        try {
            tag.put("name", metadata.deviceName());
            tag.put("osName", metadata.osName());
            tag.put("osVersion", metadata.platformVersion());
            tag.put("width", metadata.deviceScreenWidth());
            tag.put("height", metadata.deviceScreenHeight());
            tag.put("orientation", metadata.orientation());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tag;
    }

    public List<Tile> captureTiles(Boolean fullScreen) {
        Integer statusBar = metadata.statBarHeight();
        Integer navBar = metadata.navBarHeight();
        Bitmap b = Screenshot.capture().getBitmap();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        String content = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

        Integer headerHeight = 0;
        Integer footerHeight = 0;
        List<Tile> tiles = new ArrayList<>();
        tiles.add(new Tile(content, statusBar, navBar, headerHeight, footerHeight, fullScreen));
        return tiles;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String screenshot(String name, String deviceName, Integer statusBarHeight, Integer navBarHeight,
                             String orientation, Boolean fullScreen) {
        return screenshot(name, deviceName, statusBarHeight, navBarHeight, orientation, fullScreen, null);
    }

    public String screenshot(String name, String deviceName, Integer statusBarHeight, Integer navBarHeight,
                             String orientation, Boolean fullScreen, String platformVersion) {

        this.metadata = new Metadata(deviceName, statusBarHeight, navBarHeight, orientation, platformVersion);
        JSONObject tag = getTag();
        List<Tile> tiles = captureTiles(fullScreen);
        return cliWrapper.postScreenshot(name, tag, tiles, null);
    }
}
