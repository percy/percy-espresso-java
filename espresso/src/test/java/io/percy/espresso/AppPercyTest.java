package io.percy.espresso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import io.percy.espresso.lib.CliWrapper;
import io.percy.espresso.lib.ScreenshotOptions;
import io.percy.espresso.providers.GenericProvider;

/**
 * Robolectric is used because AppPercy.screenshot() ultimately touches android.*
 * (Build / Bitmap) via GenericProvider. CliWrapper and GenericProvider are
 * mock-constructed so isPercyEnabled and the screenshot outcome are fully
 * deterministic on the JVM (the real Screenshot.capture() needs an emulator).
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AppPercyTest {

    private String originalAddress;
    private boolean originalIgnoreErrors;
    private String originalLogLevel;

    @Before
    public void setUp() {
        originalAddress = CliWrapper.PERCY_SERVER_ADDRESS;
        originalIgnoreErrors = AppPercy.ignoreErrors;
        originalLogLevel = AppPercy.PERCY_LOGLEVEL;
    }

    @After
    public void tearDown() {
        CliWrapper.PERCY_SERVER_ADDRESS = originalAddress;
        AppPercy.ignoreErrors = originalIgnoreErrors;
        AppPercy.PERCY_LOGLEVEL = originalLogLevel;
    }

    @Test
    public void testScreenshotDisabledIsNoOp() {
        // healthcheck false -> isPercyEnabled false -> early return (line 55).
        try (MockedConstruction<CliWrapper> cli = mockConstruction(CliWrapper.class,
                (mock, ctx) -> when(mock.healthcheck()).thenReturn(false))) {
            AppPercy percy = new AppPercy();
            percy.screenshot("disabled");
            percy.screenshot("disabled", new ScreenshotOptions());
        }
    }

    @Test
    public void testScreenshotEnabledSuccess() {
        // Enabled + provider.screenshot returns normally -> covers the happy line 62.
        try (MockedConstruction<CliWrapper> cli = mockConstruction(CliWrapper.class,
                     (mock, ctx) -> when(mock.healthcheck()).thenReturn(true));
             MockedConstruction<GenericProvider> provider = mockConstruction(GenericProvider.class)) {
            AppPercy percy = new AppPercy();
            percy.screenshot("ok", new ScreenshotOptions());
        }
    }

    @Test
    public void testScreenshotEnabledNullOptionsSuccess() {
        // Enabled + options == null branch (lines 59-60) + happy path.
        try (MockedConstruction<CliWrapper> cli = mockConstruction(CliWrapper.class,
                     (mock, ctx) -> when(mock.healthcheck()).thenReturn(true));
             MockedConstruction<GenericProvider> provider = mockConstruction(GenericProvider.class)) {
            AppPercy percy = new AppPercy();
            percy.screenshot("ok-null-options");
        }
    }

    @Test
    public void testScreenshotEnabledSwallowsError() throws Exception {
        // Enabled + provider.screenshot throws + ignoreErrors true -> catch falls
        // through to the end (line 69) without rethrowing.
        AppPercy.ignoreErrors = true;
        try (MockedConstruction<CliWrapper> cli = mockConstruction(CliWrapper.class,
                     (mock, ctx) -> when(mock.healthcheck()).thenReturn(true));
             MockedConstruction<GenericProvider> provider = mockConstruction(GenericProvider.class,
                     (mock, ctx) -> doThrow(new RuntimeException("capture failed"))
                             .when(mock).screenshot(anyString(), any(ScreenshotOptions.class)))) {
            AppPercy percy = new AppPercy();
            percy.screenshot("swallow", new ScreenshotOptions());
        }
    }

    @Test
    public void testScreenshotRethrowsWhenIgnoreErrorsFalse() throws Exception {
        AppPercy.ignoreErrors = false;
        try (MockedConstruction<CliWrapper> cli = mockConstruction(CliWrapper.class,
                     (mock, ctx) -> when(mock.healthcheck()).thenReturn(true));
             MockedConstruction<GenericProvider> provider = mockConstruction(GenericProvider.class,
                     (mock, ctx) -> doThrow(new RuntimeException("capture failed"))
                             .when(mock).screenshot(anyString(), any(ScreenshotOptions.class)))) {
            AppPercy percy = new AppPercy();
            try {
                percy.screenshot("boom", new ScreenshotOptions());
                fail("expected RuntimeException when ignoreErrors == false");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Error taking screenshot boom"));
            }
        }
    }

    @Test
    public void testLogInfoDefault() {
        // Single-arg log delegates to info level.
        AppPercy.log("hello");
        AppPercy.log("hello", "info");
    }

    @Test
    public void testLogUnknownLevelIsSilent() {
        // Neither the debug nor info branch matches.
        AppPercy.log("nothing printed", "warn");
    }

    @Test
    public void testLogDebugBranchWhenDebugEnabled() throws Exception {
        // PERCY_DEBUG is computed at class load from PERCY_LOGLEVEL. Flip it via
        // reflection (it is a non-final private static field) so the debug
        // println branch is reachable on the JVM. This is a test-only seam; it
        // does not change production behavior.
        Field debugField = AppPercy.class.getDeclaredField("PERCY_DEBUG");
        debugField.setAccessible(true);
        boolean original = debugField.getBoolean(null);
        try {
            debugField.setBoolean(null, true);
            AppPercy.log("debug message", "debug");
        } finally {
            debugField.setBoolean(null, original);
        }
    }

    @Test
    public void testLogDebugBranchWhenDebugDisabled() {
        // debug level but PERCY_DEBUG false -> no output, exercises the false edge.
        AppPercy.log("debug message", "debug");
    }

    @Test
    public void testStaticDefaults() {
        assertEquals("info", originalLogLevel);
    }
}
