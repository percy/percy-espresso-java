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
        try
        {
            InputStream inputStream = Metadata.class.getResourceAsStream("/devices.csv");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String device = parseBufferReader(bufferedReader, model);
            if (device == null) {
                URL url = new URL("https://storage.googleapis.com/play_public/supported_devices.csv");
                BufferedReader bufferedReaderNew = new BufferedReader(new InputStreamReader(url.openStream()));
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
