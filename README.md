# percy-espresso-java
[Percy](https://percy.io) visual testing for Espresso
## Installation

Add library to your gradle file:

```sh-session
  androidTestImplementation "io.percy:espresso-java:1.0.3"
```

Update app manifest:
- Add internet permission
  ```
    <uses-permission android:name="android.permission.INTERNET" />
  ```
- Add usesCleartextTraffic attribute
  ```
    <application android:usesCleartextTraffic="true">
  ```

## Usage

This is an example test using the `percyScreenshot` function.

Java
```java
import io.percy.espresso.AppPercy;
AppPercy percy = new AppPercy();
percy.screenshot("Screenshot");
```
Kotlin
```kotlin
import io.percy.espresso.AppPercy
val percy = AppPercy()
percy.screenshot("Screenshot")
```

## Configuration

Java
```java
ScreenshotOptions options = new ScreenshotOptions();
percy.screenshot("Screenshot", options);
```

Kotlin
```kotlin
val options = ScreenshotOptions()
percy.screenshot("Screenshot", options)
```

- `name` (**required**) - The screenshot name; must be unique to each screenshot
- `options object` (**optional**) 

| Setter Method  | Description |
| ------------- | ------------- |
| setDeviceName(String deviceNameParam)  | Device name on which screenshot is taken  |
| setStatusBarHeight(Integer statusBarHeightParam)  | Height of status bar for the device  |
| setNavBarHeight(Integer navBarHeightParam)  | Height of navigation bar for the device  |
| setOrientation(String orientationParam)  | ["portrait"/"landscape"] Orientation of the application [ Note: This is only for tagging purpose, does not change the orientation of the device ]  |
| setFullScreen(Boolean fullScreenParam)  | Indicate whether app is full screen; boolean  |
