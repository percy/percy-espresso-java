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
import java.nio.charset.StandardCharsets;

import io.percy.espresso.lib.Cache;

public class MetadataHelper {
    public static String deviceNameFromCSV() {
        return deviceNameFromCSV(Build.MODEL);
    }
    public static String deviceNameFromCSV(String model)  {
        try
        {
            InputStream inputStream = Metadata.class.getResourceAsStream("/devices.csv");
            // The bundled devices.csv is encoded UTF-16 LE with a byte-order mark.
            // Use the UTF-16 charset so the BOM selects the byte order and is stripped,
            // instead of relying on the platform-default charset (UTF-8), which left
            // interleaved NUL bytes and a stray BOM that only worked by accident.
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_16));
            String device = parseBufferReader(bufferedReader, model);
            if (device == null) {
                URL url = new URL("https://storage.googleapis.com/play_public/supported_devices.csv");
                // The remote Google Play devices list is UTF-8; read it explicitly.
                BufferedReader bufferedReaderNew = new BufferedReader(
                    new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
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
