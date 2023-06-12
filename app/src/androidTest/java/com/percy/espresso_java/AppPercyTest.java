package com.percy.espresso_java;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import net.minidev.json.JSONObject;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.percy.espresso.AppPercy;
import io.percy.espresso.lib.CliWrapper;
import io.percy.espresso.lib.ScreenshotOptions;
import io.percy.espresso.metadata.Metadata;
import io.percy.espresso.providers.GenericProvider;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AppPercyTest {

    WireMockServer server = new WireMockServer(5338);
    GenericProvider genericProvider = new GenericProvider();

    @Before
    public void setup() throws JSONException {
        CliWrapper.PERCY_SERVER_ADDRESS = "http://127.0.0.1:5338";
        genericProvider.setMetadata(new Metadata(new ScreenshotOptions()));
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder();
        mockResponse.withHeader("x-percy-core-version", "1.23.0");
        mockResponse.withStatus(200);
        server.start();
        WireMock.configureFor("127.0.0.1", 5338);
        WireMock.stubFor(
                WireMock.get("/percy/healthcheck")
                        .willReturn(mockResponse)
        );
        JSONObject responseJsonObject = new JSONObject();
        responseJsonObject.put("link", "dummy");

        WireMock.stubFor(
                WireMock.post("/percy/comparison")
                        .withRequestBody(matchingJsonPath("$.name"))
                        .withRequestBody(matchingJsonPath("$.tag"))
                        .withRequestBody(matchingJsonPath("$.tiles"))
                        .withRequestBody(matchingJsonPath("$.clientInfo"))
                        .withRequestBody(matchingJsonPath("$.environmentInfo"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(String.valueOf(responseJsonObject))
                        )
        );
    }

    @Test
    public void testScreenshot() {
        AppPercy percy = new AppPercy();
        percy.screenshot("Screenshot");
    }

    @After
    public void tearDown() {
        if(server != null && server.isRunning()) {
            server.shutdownServer();
        }
    }

}
