package io.percy.espresso.metadata;

import android.content.res.Resources;
import android.os.Build;

public class Metadata {

    private final String platformVersion;
    private final Integer statusBar;
    private final Integer navBar;
    private final String deviceName;

    public Metadata(String deviceName, Integer statusBar, Integer navBar, String orientation,
            String platformVersion) {
        this.platformVersion = platformVersion;
        this.statusBar = statusBar;
        this.navBar = navBar;
        this.deviceName = deviceName;
    }

    public String osName() {
       return "Android";
    }

    public String platformVersion() {
        if (platformVersion != null) {
            return platformVersion;
        }
        return Build.VERSION.RELEASE;
    }

    public String orientation() {
        Integer orientation = Resources.getSystem().getConfiguration().orientation;
        if (orientation == 1) {
            return "portrait";
        }
        return "landscape";
    }

    public String getDeviceName() {
        return deviceName;
    }

    public Integer getNavBar() {
        return navBar;
    }

    public Integer getStatusBar() {
        return statusBar;
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
        Integer idStatusBarHeight = Resources.getSystem().getIdentifier( "status_bar_height", "dimen", "android");
        return Resources.getSystem().getDimensionPixelSize(idStatusBarHeight);
    }

    public Integer navBarHeight() {
        if (navBar != null) {
            return navBar;
        }
        Integer navBarHeight = Resources.getSystem().getIdentifier( "navigation_bar_height", "dimen", "android");
        return Resources.getSystem().getDimensionPixelSize(navBarHeight);
    }

    public String deviceName() {
        String deviceName = getDeviceName();
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
