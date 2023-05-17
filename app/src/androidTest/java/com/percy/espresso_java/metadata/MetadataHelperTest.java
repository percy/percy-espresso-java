package com.percy.espresso_java.metadata;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.percy.espresso.metadata.MetadataHelper;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MetadataHelperTest {
    @Test
    public void testSanitizedString() {
        assertEquals(MetadataHelper.sanitizedString("SM-12#3## "), "SM-123");
    }

    @Test
    public void testDeviceNameFromCSVSamsung() {
        // brand + marketing name
        assertEquals(MetadataHelper.deviceNameFromCSV("SM-A145F"), "Samsung Galaxy A14");
    }

    @Test
    public void testDeviceNameFromCSVRedmi() {
        // marketing name
        assertEquals(MetadataHelper.deviceNameFromCSV("22041216UC"), "Redmi Note 11T Pro +");
    }
}
