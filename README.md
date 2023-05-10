# percy-espresso-java
[Percy](https://percy.io) visual testing for Espresso
## Installation

Add library to your gradle file:

```sh-session
  androidTestImplementation "io.percy.espresso:percy-espresso-java:1.0.0"
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
  - `fullscreen`: if the app is currently in fullscreen
  - `deviceName`: custom device name to override SDK fetched name
  - `orientation`: "portrait"/"landscape" tell SDK which orientation app is in [ Note: This is only for tagging purpose, does not change the orientation of the device ]
  - `statusBarHeight`: In px if you want to override SDK
  - `navigationBarHeight`: In px if you want to override SDK
