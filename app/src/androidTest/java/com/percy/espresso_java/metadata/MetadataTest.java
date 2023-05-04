package com.percy.espresso_java.metadata;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import io.percy.espresso.metadata.Metadata;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MetadataTest {
    Metadata metadata;

    @Before
    public void setUp() {
        metadata = new Metadata(null,null,null,null,null);
    }

    @Test
    public void testUseAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.percy.espresso_java", appContext.getPackageName());
    }

    @Test
    public void testOsName() {
        assertEquals(metadata.osName(), "Android");
    }

    @Test
    public void testPlatformVersion() {
        assertEquals(metadata.platformVersion(), Build.VERSION.RELEASE);
    }

    @Test
    public void testPlatformVersionExternallyProvided() {
        metadata = new Metadata(null,null,null,null,"12");
        assertEquals(metadata.platformVersion(), "12");
    }

    @Test
    public void testDeviceScreenWidth() {
        assertEquals(metadata.deviceScreenWidth().intValue(), Resources.getSystem().getDisplayMetrics().widthPixels);
    }

    @Test
    public void testDeviceScreenHeight() {
        assertEquals(metadata.deviceScreenHeight().intValue(), Resources.getSystem().getDisplayMetrics().heightPixels);
    }

    @Test
    public void testStatBarHeight() {
        assertEquals(metadata.statBarHeight().intValue(), Resources.getSystem().getDimensionPixelSize(Resources.getSystem().getIdentifier( "status_bar_height", "dimen", "android")));
    }

    @Test
    public void testStatBarHeightExternallyProvided() {
        metadata = new Metadata(null,100,null,null,null);
        assertEquals(metadata.statBarHeight().intValue(), 100);
    }

    @Test
    public void testNavBarHeight() {
        assertEquals(metadata.navBarHeight().intValue(), Resources.getSystem().getDimensionPixelSize(Resources.getSystem().getIdentifier( "navigation_bar_height", "dimen", "android")));
    }

    @Test
    public void testNavBarHeightExternallyProvided() {
        metadata = new Metadata(null,null,200,null,null);
        assertEquals(metadata.navBarHeight().intValue(), 200);
    }

    @Test
    public void testDeviceName() {
        assertEquals(metadata.deviceName(), Build.MANUFACTURER + " " + Build.MODEL);
    }

    @Test
    public void testDeviceNameExternallyProvided() {
        metadata = new Metadata("Device",null,null,null,null);
        assertEquals(metadata.deviceName(), "Device");
    }
}
