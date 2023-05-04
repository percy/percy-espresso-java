package io.percy.espresso;

import io.percy.espresso.lib.CliWrapper;
import io.percy.espresso.lib.ScreenshotOptions;
import io.percy.espresso.providers.GenericProvider;
/**
 * Percy client for visual testing.
 */
public class AppPercy {

    private CliWrapper cliWrapper;

    /**
     * Determine if we're debug logging
     */
    private static boolean PERCY_DEBUG = System.getenv().getOrDefault("PERCY_LOGLEVEL", "info").equals("debug");

    /**
     * for logging
     */
    private static String LABEL = "[\u001b[35m" + (PERCY_DEBUG ? "percy:java" : "percy") + "\u001b[39m]";

    /**
     * Is the Percy server running or not
     */
    private boolean isPercyEnabled;

    private String sessionId;

    public static Boolean ignoreErrors = true;

    public AppPercy() {
        this.cliWrapper = new CliWrapper();
        this.isPercyEnabled = cliWrapper.healthcheck();
        this.sessionId = "123";
    }

    /**
     * Take a screenshot and upload it to Percy.
     *
     * @param name The human-readable name of the screenshot. Should be unique.
     *
     */
    public void screenshot(String name) {
        screenshot(name, false, null);
    }

    /**
     * Take a screenshot and upload it to Percy.
     *
     * @param name       The human-readable name of the screenshot. Should be
     *                   unique.
     * @param fullScreen It indicates if the app is a full screen
     */
    public void screenshot(String name, Boolean fullScreen) {
        screenshot(name, fullScreen, null);
    }

    /**
     * Take a screenshot and upload it to Percy.
     *
     * @param name    The human-readable name of the screenshot. Should be
     *                unique.
     * @param options Optional screenshot params
     */
    public void screenshot(String name, ScreenshotOptions options) {
        screenshot(name, false, options);
    }

    /**
     * Take a screenshot and upload it to Percy.
     *
     * @param name       The human-readable name of the screenshot. Should be
     *                   unique.
     * @param fullScreen It indicates if the app is a full screen
     * @param options    Optional screenshot params
     */
    public void screenshot(String name, Boolean fullScreen, ScreenshotOptions options) {
        if (!isPercyEnabled) {
            return;
        }
        try {
            GenericProvider provider = new GenericProvider();
            if (options != null) {
                provider.screenshot(name, options.getDeviceName(), options.getStatusBarHeight(),
                        options.getNavBarHeight(), options.getOrientation(), fullScreen);
            } else {
                provider.screenshot(name, null, null, null, null,
                        fullScreen);
            }
        } catch (Exception e) {
            log("Error taking screenshot " + name);
            log(e.toString(), "debug");
            if (!ignoreErrors) {
                throw new RuntimeException("Error taking screenshot " + name, e);
            }
        }
    }

    public static void log(String message) {
        log(message, "info");
    }

    public static void log(String message, String logLevel) {
        if (logLevel == "debug" && PERCY_DEBUG) {
            System.out.println(LABEL + " " + message);
        } else if (logLevel == "info") {
            System.out.println(LABEL + " " + message);
        }
    }

}

