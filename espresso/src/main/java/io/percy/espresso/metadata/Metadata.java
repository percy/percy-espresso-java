package io.percy.espresso.metadata;

import android.content.res.Resources;
import android.os.Build;

import io.percy.espresso.lib.Cache;
import io.percy.espresso.lib.ScreenshotOptions;

public class Metadata {

    private final Integer statusBar;
    private final Integer navBar;
    private final String deviceName;
    private final String orientation;

    public Metadata(ScreenshotOptions options) {
        this.statusBar = options.getStatusBarHeight();
        this.navBar = options.getNavBarHeight();
        this.deviceName = options.getDeviceName();
        this.orientation = options.getOrientation();
    }

    public String osName() {
        return "Android";
    }

    public String platformVersion() {
        return Build.VERSION.RELEASE;
    }

    public String orientation() {
        if (orientation != null) {
            if (orientation.toLowerCase().equals("portrait") || orientation.toLowerCase().equals("landscape")) {
                return orientation.toLowerCase();
            } else if (orientation.toLowerCase().equals("auto")) {
                Integer orientationInteger = Resources.getSystem().getConfiguration().orientation;
                if (orientationInteger == 1) {
                    return "portrait";
                }
                return "landscape";
            } else {
                return "portrait";
            }
        } else {
            Integer orientationInteger = Resources.getSystem().getConfiguration().orientation;
            if (orientationInteger == 1) {
                return "portrait";
            }
            return "landscape";
        }
    }

    public Integer deviceScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public Integer deviceScreenHeight() {
        Integer deviceScreenHeight = MetadataHelper.valueFromStaticDevicesInfo("deviceHeight",
                this.deviceName().toLowerCase());
        if (deviceScreenHeight == 0) {
            // We have seen that for older device the height = viewport + nav_bar
            return Resources.getSystem().getDisplayMetrics().heightPixels + this.navBarHeight(); 
        }
        return deviceScreenHeight;
    }

    public Integer statBarHeight() {
        if (statusBar != null) {
            return statusBar;
        }
        Integer calStatusBarHeight = MetadataHelper.valueFromStaticDevicesInfo("statusBarHeight",
                this.deviceName().toLowerCase());
        if (calStatusBarHeight == 0) {
            Integer idStatusBarHeight = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
            return Resources.getSystem().getDimensionPixelSize(idStatusBarHeight); 
        }
        return calStatusBarHeight;
    }

    public Integer navBarHeight() {
        if (navBar != null) {
            return navBar;
        }
        Integer calNavBarHeight = MetadataHelper.valueFromStaticDevicesInfo("navBarHeight",
                this.deviceName().toLowerCase());
        if (calNavBarHeight == 0) {
            Integer navBarHeight = Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android");
            return Resources.getSystem().getDimensionPixelSize(navBarHeight);
        }
        return calNavBarHeight;
    }

    public String deviceName() {
        if (deviceName != null) {
            return deviceName;
        }
        if (Cache.CACHE_MAP.get("deviceName") == null) {
            String deviceName = MetadataHelper.deviceNameFromCSV();
            Cache.CACHE_MAP.put("deviceName", deviceName);
        }
        return (String) Cache.CACHE_MAP.get("deviceName");
    }
 }
