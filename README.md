# robotfindskitten

This is an Android implementation of
[robotfindskitten](https://robotfindskitten.org).

## Download

  * [robotfindskitten (Google Play)](https://play.google.com/store/apps/details?id=info.staticfree.android.robotfindskitten)
  * [GitHub releases](https://github.com/xxv/robotfindskitten/releases) (signed APKs for sideloading)

## Changes

  * 2026-09-23: 1.1.701 - Modernized build (Gradle); targets current Android versions; now requires Android 9 or newer
  * 2012-12-21: 1.0.701 - Mayan Apocalypse Edition; a slew of new NKI, tablet support, new old-style theme, updated icon
  * 0.9.406 - Fixed non-shown messages; updated icon
  * 0.8.406 - fixed multi-density scaling; minor visual improvements
  * 0.7.406 - added QVGA support
  * 0.6.406 - added touchscreen support; moved toward RFK RFC compliance.

## Screenshots

  * [![screenshot 01](sshot01.thumb.png)](sshot01.png)
  * [![screenshot 02](sshot02.thumb.png)](sshot02.png)
  * [![screenshot 03](sshot03.thumb.png)](sshot03.png)

## Source

The source for Android robotfindskitten is available via the [Android
robotfindskitten git
repository](https://github.com/xxv/robotfindskitten). You can grab a
copy for yourself by running:

    git clone https://github.com/xxv/robotfindskitten.git

### Building

The app builds with Gradle. You'll need JDK 17 or newer (the one bundled with
Android Studio works) and the Android SDK. Either open the project in Android
Studio, or run:

    ./gradlew assembleDebug

Releases are built and signed by GitHub Actions when a version tag is pushed;
see [cicd_notes.md](cicd_notes.md). To sign a release build locally, set
`KEYSTORE_FILE` (the keystore's path), `KEYSTORE_PASSWORD`, `KEY_ALIAS` and
`KEY_PASSWORD` in the environment, then run:

    ./gradlew assembleRelease bundleRelease

The bundle ends up in `app/build/outputs/bundle/release/app-release.aab`.

### License

The code is made available under the GPL v3


### Credits

  * Icon artwork by [Sarah Morrison](http://tashari.org/)

### Privacy Policy

This app doesn't collect any information about its usage and has no Internet connectivity. Enjoy!
See the full [privacy policy](PRIVACY.md) for details.

