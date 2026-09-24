CI/CD and Release notes
=======================

- Every push builds the debug APK, runs lint, and checks the signing lineage
  (`.github/workflows/ci.yml`).
- Pushing a `v*` tag builds a signed APK and Play bundle and puts them on a
  draft GitHub release (`.github/workflows/release.yml`):

      git tag v1.1.0 && git push origin v1.1.0

  Bump `versionCode` and `versionName` in `app/build.gradle.kts` first.

Signing
-------

The key comes from the repository secrets `KEYSTORE_BASE64`,
`KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`. Without them the release
still runs, but the artifacts are unsigned.

The app was first released with a 1024-bit key from 2009, and releases now use
a stronger key. `signing/lineage` is the proof of rotation between them.

- **Play bundle:** the current key (the upload key), signed by Gradle.
- **APK:** the current key plus the lineage, signed by `signing/sign-apk.sh`,
  so it updates over installs signed by the old key. The rotation is in the
  v3.0 block (`--rotation-min-sdk-version 28`), which Android 9+ reads; that's
  why `minSdk` is 28, and why the old key is never needed.

To sign locally, set `KEYSTORE_FILE` (a path) and the same passwords and alias,
then run `./gradlew assembleRelease bundleRelease` (current key only), or
esigning/sign-apk.sh sign <unsigned.apk> <signed.apk>` for the lineage.
