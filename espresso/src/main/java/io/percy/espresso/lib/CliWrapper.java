package io.percy.espresso.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.percy.espresso.AppPercy;
import io.percy.espresso.Environment;

public class CliWrapper {
    // Maybe get the CLI server address
    public static String PERCY_SERVER_ADDRESS = "http://percy.cli:5338";

    // Environment information like Framework & SDK versions
    private Environment env;

    public CliWrapper() {
        this.env = new Environment();
    }

    /**
     * Checks to make sure the local Percy server is running. If not, disable Percy.
     * @return
     */
    public boolean healthcheck() {
        try {
            URL url = new URL(PERCY_SERVER_ADDRESS + "/percy/healthcheck");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Unexpected response code for health check: " + responseCode);
            }
            // success
            String version = con.getHeaderField("x-percy-core-version");

            if (!version.split("\\.")[0].equals("1")) {
                AppPercy.log("Unsupported Percy CLI version, " + version);
                return false;
            }
            return true;
        } catch (Exception e) {
            AppPercy.log("Percy is not running, disabling screenshots");
            AppPercy.log(e.toString(), "debug");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * POST the Screenshot taken from the app to the Percy CLI node process.
     *
     * @param name The human-readable name of the screenshot. Should be
     *             unique.
     */
    public String postScreenshot(String name, JSONObject tag, List<Tile> tiles, String externalDebugUrl) {
        try {
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("tag", tag);
            data.put("tiles", new JSONArray(Tile.getTilesAsJson(tiles)));
            data.put("clientInfo", env.getClientInfo());
            data.put("externalDebugUrl", externalDebugUrl);
            data.put("environmentInfo", env.getEnvironmentInfo());

            URL url = new URL(PERCY_SERVER_ADDRESS + "/percy/comparison");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            // For POST only - START
            con.setDoOutput(true);
            OutputStream stream = con.getOutputStream();
            stream.write(data.toString().getBytes());
            stream.flush();
            stream.close();
            // For POST only - END

            int responseCode = con.getResponseCode();

            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Unexpected response code for post Screenshot: " + responseCode);
            }
            //success
            String response = readBody(con);
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("link");
        } catch (Exception e) {
            AppPercy.log(e.toString(), "debug");
            AppPercy.log("Could not post screenshot " + name);
            e.printStackTrace();
        }
        return null;
    }

    private static String readBody(URLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

}
