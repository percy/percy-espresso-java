package com.percy.espresso_java;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.percy.espresso.Environment;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EnvironmentTest {

    Environment environment;

    @Before
    public void setup() {
        environment = new Environment();
    }

    @Test
    public void testGetClientInfo() {
        Assert.assertEquals(environment.getClientInfo(), "percy-espresso-java/" + Environment.SDK_VERSION);
    }

    @Test
    public void testGetEnvironmentInfo() {
        Assert.assertEquals(environment.getEnvironmentInfo(), "espresso-java");
    }

}