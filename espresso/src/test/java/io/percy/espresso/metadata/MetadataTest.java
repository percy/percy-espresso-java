package io.percy.espresso.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.percy.espresso.lib.Cache;
import io.percy.espresso.lib.ScreenshotOptions;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MetadataTest {

    @Before
    public void clearCache() {
        Cache.CACHE_MAP.clear();
    }

    private Metadata metadataWith(ScreenshotOptions options) {
        return new Metadata(options);
    }

    private void setSystemOrientation(int orientation) {
        Configuration config = Resources.getSystem().getConfiguration();
        config.orientation = orientation;
        Resources.getSystem().updateConfiguration(config, Resources.getSystem().getDisplayMetrics());
    }

    @Test
    public void testOsName() {
        assertEquals("Android", metadataWith(new ScreenshotOptions()).osName());
    }

    @Test
    public void testPlatformVersion() {
        assertEquals(Build.VERSION.RELEASE, metadataWith(new ScreenshotOptions()).platformVersion());
    }

    @Test
    public void testOrientationExplicitPortrait() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setOrientation("Portrait");
        assertEquals("portrait", metadataWith(options).orientation());
    }

    @Test
    public void testOrientationExplicitLandscape() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setOrientation("LANDSCAPE");
        assertEquals("landscape", metadataWith(options).orientation());
    }

    @Test
    public void testOrientationUnknownDefaultsToPortrait() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setOrientation("sideways");
        assertEquals("portrait", metadataWith(options).orientation());
    }

    @Test
    public void testOrientationAutoPortrait() {
        setSystemOrientation(1); // Configuration.ORIENTATION_PORTRAIT
        ScreenshotOptions options = new ScreenshotOptions();
        options.setOrientation("auto");
        assertEquals("portrait", metadataWith(options).orientation());
    }

    @Test
    public void testOrientationAutoLandscape() {
        setSystemOrientation(2); // Configuration.ORIENTATION_LANDSCAPE
        ScreenshotOptions options = new ScreenshotOptions();
        options.setOrientation("auto");
        assertEquals("landscape", metadataWith(options).orientation());
    }

    @Test
    public void testOrientationNullUsesSystemPortrait() {
        setSystemOrientation(1);
        assertEquals("portrait", metadataWith(new ScreenshotOptions()).orientation());
    }

    @Test
    public void testOrientationNullUsesSystemLandscape() {
        setSystemOrientation(2);
        assertEquals("landscape", metadataWith(new ScreenshotOptions()).orientation());
    }

    @Test
    public void testDeviceScreenWidth() {
        assertEquals(Resources.getSystem().getDisplayMetrics().widthPixels,
                metadataWith(new ScreenshotOptions()).deviceScreenWidth().intValue());
    }

    @Test
    public void testDeviceScreenHeightFromStaticInfo() {
        // Device with a deviceHeight entry -> returns that value directly.
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("samsung galaxy s22");
        assertEquals(2340, metadataWith(options).deviceScreenHeight().intValue());
    }

    @Test
    public void testDeviceScreenHeightFallsBackToResources() {
        // Device with no deviceHeight entry -> displayMetrics + nav + status bars.
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("redmi 10x 4g"); // only statusBarHeight present, no deviceHeight
        options.setStatusBarHeight(10);
        options.setNavBarHeight(20);
        Metadata metadata = metadataWith(options);
        int expected = Resources.getSystem().getDisplayMetrics().heightPixels + 20 + 10;
        assertEquals(expected, metadata.deviceScreenHeight().intValue());
    }

    @Test
    public void testStatBarHeightExternallyProvided() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setStatusBarHeight(100);
        assertEquals(100, metadataWith(options).statBarHeight().intValue());
    }

    @Test
    public void testStatBarHeightFromStaticInfo() {
        // "redmi 10x 4g" has statusBarHeight 100 in deviceInfo.json.
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("redmi 10x 4g");
        assertEquals(100, metadataWith(options).statBarHeight().intValue());
    }

    @Test
    public void testStatBarHeightFromResourcesWhenNotInStaticInfo() {
        // Device not in deviceInfo.json -> static lookup returns 0 -> Resources path.
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("unknown device xyz");
        Integer height = metadataWith(options).statBarHeight();
        assertNotNull(height);
        int expected = Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
        assertEquals(expected, height.intValue());
    }

    @Test
    public void testNavBarHeightExternallyProvided() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setNavBarHeight(200);
        assertEquals(200, metadataWith(options).navBarHeight().intValue());
    }

    @Test
    public void testNavBarHeightFromStaticInfo() {
        // "oppo a78 5g" has navBarHeight 88 in deviceInfo.json.
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("oppo a78 5g");
        assertEquals(88, metadataWith(options).navBarHeight().intValue());
    }

    @Test
    public void testNavBarHeightFromResourcesWhenNotInStaticInfo() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("unknown device xyz");
        Integer height = metadataWith(options).navBarHeight();
        assertNotNull(height);
        int expected = Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android"));
        assertEquals(expected, height.intValue());
    }

    @Test
    public void testDeviceNameExternallyProvided() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("Custom Device");
        assertEquals("Custom Device", metadataWith(options).deviceName());
    }

    @Test
    public void testDeviceNameResolvedFromCsvAndCached() {
        Cache.CACHE_MAP.clear();
        Metadata metadata = metadataWith(new ScreenshotOptions());
        // First call populates the cache from the CSV (Build.MODEL fallback).
        String first = metadata.deviceName();
        assertEquals(Build.MANUFACTURER + " " + Build.MODEL, first);
        // Second call hits the cache branch and returns the same value.
        assertEquals(first, metadata.deviceName());
        assertEquals(first, Cache.CACHE_MAP.get("deviceName"));
    }
}
