package io.percy.espresso.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ScreenshotOptionsTest {

    @Test
    public void testDefaultsAreNull() {
        ScreenshotOptions options = new ScreenshotOptions();
        assertNull(options.getDeviceName());
        assertNull(options.getStatusBarHeight());
        assertNull(options.getNavBarHeight());
        assertNull(options.getOrientation());
        assertNull(options.getTestCase());
        assertNull(options.getLabels());
        assertNull(options.getFullScreen());
    }

    @Test
    public void testSettersAndGetters() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setDeviceName("Pixel 6");
        options.setStatusBarHeight(100);
        options.setNavBarHeight(120);
        options.setOrientation("portrait");
        options.setTestCase("loginFlow");
        options.setLabels("smoke,regression");
        options.setFullScreen(true);

        assertEquals("Pixel 6", options.getDeviceName());
        assertEquals(Integer.valueOf(100), options.getStatusBarHeight());
        assertEquals(Integer.valueOf(120), options.getNavBarHeight());
        assertEquals("portrait", options.getOrientation());
        assertEquals("loginFlow", options.getTestCase());
        assertEquals("smoke,regression", options.getLabels());
        assertEquals(Boolean.TRUE, options.getFullScreen());
    }

    @Test
    public void testFullScreenFalse() {
        ScreenshotOptions options = new ScreenshotOptions();
        options.setFullScreen(false);
        assertEquals(Boolean.FALSE, options.getFullScreen());
    }
}
