package io.percy.espresso;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EnvironmentTest {

    @Test
    public void testGetClientInfo() {
        Environment environment = new Environment();
        assertEquals("espresso-java/" + Environment.SDK_VERSION, environment.getClientInfo());
    }

    @Test
    public void testGetEnvironmentInfo() {
        Environment environment = new Environment();
        assertEquals("espresso-java", environment.getEnvironmentInfo());
    }

    @Test
    public void testSdkVersionConstant() {
        assertEquals("1.0.4", Environment.SDK_VERSION);
    }
}
