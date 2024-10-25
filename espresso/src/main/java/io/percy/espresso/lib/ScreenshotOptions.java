package io.percy.espresso.lib;

public class ScreenshotOptions {
  private String deviceName = null;
  private Integer statusBarHeight = null;
  private Integer navBarHeight = null;
  private String orientation = null;
  private Boolean fullScreen = null;
  private String testCase = null;
  private String labels = null;

  public String getDeviceName() {
    return deviceName;
  }

  public Integer getStatusBarHeight() {
      return statusBarHeight;
  }

  public Integer getNavBarHeight() {
      return navBarHeight;
  }

  public String getOrientation() {
      return orientation;
  }

  public String getTestCase() {
      return testCase;
  }

  public String getLabels() {
      return labels;
  }

  public Boolean getFullScreen() {
      return fullScreen;
  }

  public void setDeviceName(String deviceNameParam) {
      deviceName = deviceNameParam;
  }

  public void setStatusBarHeight(Integer statusBarHeightParam) {
      statusBarHeight = statusBarHeightParam;
  }

  public void setNavBarHeight(Integer navBarHeightParam) {
      navBarHeight = navBarHeightParam;
  }

  public void setOrientation(String orientationParam) {
      orientation = orientationParam;
  }

  public void setTestCase(String testCaseParam) {
      testCase = testCaseParam;
  }

  public void setLabels(String labelsParam) {
      labels = labelsParam;
  }

  public void setFullScreen(Boolean fullScreenParam) {
      fullScreen = fullScreenParam;
  }

}
