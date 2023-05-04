package com.percy.espresso_java;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;

import net.minidev.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.percy.espresso.AppPercy;
import io.percy.espresso.lib.CliWrapper;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AppPercyTest {

    WireMockServer server = new WireMockServer(5338);

    @Before
    public void setup() {
        CliWrapper.PERCY_SERVER_ADDRESS = "http://127.0.0.1:5338";
        ResponseDefinitionBuilder mockResponse = new ResponseDefinitionBuilder();
        mockResponse.withHeader("x-percy-core-version", "1.2");
        mockResponse.withStatus(200);
        server.start();
        WireMock.configureFor("127.0.0.1", 5338);
        WireMock.stubFor(
                WireMock.get("/percy/healthcheck")
                        .willReturn(mockResponse)
        );
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("link", "dummy");
        WireMock.stubFor(
                WireMock.post("/percy/comparison")
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withBody(String.valueOf(jsonObject))
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