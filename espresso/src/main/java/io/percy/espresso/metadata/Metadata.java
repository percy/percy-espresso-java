package io.percy.espresso.metadata;

import android.content.res.Resources;
import android.os.Build;

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
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public Integer statBarHeight() {
        if (statusBar != null) {
            return statusBar;
        }
        Integer idStatusBarHeight = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        return Resources.getSystem().getDimensionPixelSize(idStatusBarHeight);
    }

    public Integer navBarHeight() {
        if (navBar != null) {
            return navBar;
        }
        Integer navBarHeight = Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android");
        return Resources.getSystem().getDimensionPixelSize(navBarHeight);
    }

    public String deviceName() {
        if (deviceName != null) {
            return deviceName;
        }
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
           return model;
        } else {
           return manufacturer + " " + model;
        }
    }


}
