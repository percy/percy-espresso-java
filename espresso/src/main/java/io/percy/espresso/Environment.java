package io.percy.espresso;

public class Environment {
    public static final String SDK_VERSION = "1.0.4";
    private static final String SDK_NAME = "espresso-java";

    public String getClientInfo() {
        return SDK_NAME + "/" + SDK_VERSION;
    }

    public String getEnvironmentInfo() {
        return "espresso-java";
    }

}
