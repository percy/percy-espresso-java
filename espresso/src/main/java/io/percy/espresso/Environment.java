package io.percy.espresso;

public class Environment {
    public static final String SDK_VERSION = "0.0.1";
    private static final String SDK_NAME = "percy-espresso-java";

    public String getClientInfo() {
        return SDK_NAME + "/" + SDK_VERSION;
    }

    public String getEnvironmentInfo() {
        return "espresso-java";
    }

}
