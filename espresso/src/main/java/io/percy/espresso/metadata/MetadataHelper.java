package io.percy.espresso.metadata;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import io.percy.espresso.lib.Cache;

public class MetadataHelper {
    public static String deviceNameFromCSV() {
        return deviceNameFromCSV(Build.MODEL);
    }
    public static String deviceNameFromCSV(String model)  {
        return new MetadataHelper().resolveDeviceNameFromCSV(model);
    }

    /**
     * Instance form of {@link #deviceNameFromCSV(String)}. The static entry
     * points delegate here through a default {@code MetadataHelper} instance so
     * production behavior is byte-for-byte identical: the local bundled CSV is
     * tried first and, only when it yields no match, the remote Google Play
     * device CSV is consulted. The reader creation for each source is funneled
     * through {@link #openLocalCsvReader()} / {@link #openRemoteCsvReader()} so
     * tests can drive the otherwise network-only fallback and its IOException
     * handling without changing any of this control flow.
     */
    protected String resolveDeviceNameFromCSV(String model) {
        try
        {
            String device = localLookup(model);
            if (device == null) {
                BufferedReader bufferedReaderNew = openRemoteCsvReader();
                device = parseBufferReader(bufferedReaderNew, model);
            }
            return device;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Resolves {@code model} against the bundled {@code /devices.csv}. Returns
     * exactly what {@code parseBufferReader(openLocalCsvReader(), model)}
     * returned inline before extraction, so production behavior is unchanged.
     * Exposed as an overridable seam so a test can force a local miss (return
     * {@code null}) and thereby exercise the remote-CSV fallback branch.
     */
    protected String localLookup(String model) {
        BufferedReader bufferedReader = openLocalCsvReader();
        return parseBufferReader(bufferedReader, model);
    }

    /**
     * Opens a reader over the bundled {@code /devices.csv} resource. Extracted
     * as an overridable seam; the body is exactly what previously sat inline in
     * {@code deviceNameFromCSV}.
     */
    protected BufferedReader openLocalCsvReader() {
        InputStream inputStream = Metadata.class.getResourceAsStream("/devices.csv");
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    /**
     * Opens a reader over the remote Google Play supported-devices CSV. Extracted
     * as an overridable seam; the body is exactly what previously sat inline in
     * the {@code device == null} fallback of {@code deviceNameFromCSV}. The URL
     * itself comes from {@link #remoteCsvUrl()} so a test can point the very same
     * open/stream logic at a loopback stub instead of the real network endpoint.
     */
    protected BufferedReader openRemoteCsvReader() throws IOException {
        URL url = new URL(remoteCsvUrl());
        return new BufferedReader(new InputStreamReader(url.openStream()));
    }

    /**
     * The remote supported-devices CSV endpoint. Unchanged production constant;
     * isolated as an overridable seam purely so tests can redirect
     * {@link #openRemoteCsvReader()} at a local stub server.
     */
    protected String remoteCsvUrl() {
        return "https://storage.googleapis.com/play_public/supported_devices.csv";
    }

    public static String sanitizedString(String string) {
        return string.replaceAll("[^ a-zA-Z0-9()._+-]", "").trim();
    }

    public static String parseBufferReader(BufferedReader reader, String model) {
        String line = "";
        try {
            while ((line = reader.readLine()) != null)
            {
                String[] rowItems = line.split(",");
                if (rowItems.length == 4) {
                    if (sanitizedString(rowItems[3]).equals(model)) {
                        String brand = sanitizedString(rowItems[0]);
                        String marketingName = sanitizedString(rowItems[1]);
                        if (marketingName.startsWith(brand)) {
                            return marketingName;
                        } else {
                            return brand + " " + marketingName;
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static Integer valueFromStaticDevicesInfo(String key, String deviceName) {
        try {
            JSONObject object = getDevicesJson().getJSONObject(deviceName);
            return object.getInt(key);
        } catch (JSONException | IOException e) {
            return 0;
        }
    }

    public static JSONObject getDevicesJson() throws IOException, JSONException {
        if (Cache.CACHE_MAP.get("getDevicesJson") == null) {
            InputStream inputStream = MetadataHelper.class.getResourceAsStream("/deviceInfo.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }

            String jsonString = jsonStringBuilder.toString();
            JSONTokener tokener = new JSONTokener(jsonString);
            JSONObject devicesJsonObject = new JSONObject(tokener);
            Cache.CACHE_MAP.put("getDevicesJson", devicesJsonObject);
        }
        return (JSONObject) Cache.CACHE_MAP.get("getDevicesJson");
    }
}
