plugins {
    alias(libs.plugins.android.application)
}

// Release signing, driven by the environment so the keystore never has to live in the repository.
// Set these when building a release (the GitHub Actions release workflow does it from secrets):
//
//   KEYSTORE_FILE      path to the keystore
//   KEYSTORE_PASSWORD  keystore password
//   KEY_ALIAS          key alias within the keystore
//   KEY_PASSWORD       password for that key
//
// Without them the release build is simply left unsigned.
val keystoreFile: String? = System.getenv("KEYSTORE_FILE")

android {
    namespace = "info.staticfree.android.robotfindskitten"
    compileSdk = 36

    defaultConfig {
        applicationId = "info.staticfree.android.robotfindskitten"
        // Android 9+, the first version that recognizes a rotated signing key, so sideloaded
        // updates don't need the original key (see signing/sign-apk.sh).
        minSdk = 28
        targetSdk = 36
        versionCode = 8
        versionName = "1.1.0"
    }

    signingConfigs {
        if (!keystoreFile.isNullOrEmpty()) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
