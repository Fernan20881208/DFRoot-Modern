import java.util.Properties

plugins {
    id("com.android.application")
}

val signingProps = Properties()
val signingPropsFile = rootProject.file("signing.properties")
if (signingPropsFile.exists()) signingProps.load(signingPropsFile.inputStream())
val hasReleaseSigning = listOf("KEYSTORE_FILE", "KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
    .all { !signingProps.getProperty(it).isNullOrBlank() }

android {
    namespace = "df.root"
    compileSdk = 36

    // Required by DFRoot v4.5 ExploitRunner (BuildConfig.VERSION_NAME).
    buildFeatures { buildConfig = true }

    defaultConfig {
        // Native v4.5 code hardcodes /data/user_de/0/df.root. Do not add a suffix.
        applicationId = "df.root"
        // Android 12 (API 31); CI checks the packaged manifest and API compatibility.
        minSdk = 31
        targetSdk = 36
        versionCode = 40501
        versionName = "4.5-modern.1"
        ndk { abiFilters += listOf("arm64-v8a") }
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(signingProps.getProperty("KEYSTORE_FILE"))
                storePassword = signingProps.getProperty("KEYSTORE_PASSWORD")
                keyAlias = signingProps.getProperty("KEY_ALIAS")
                keyPassword = signingProps.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            // Keep the upstream package so native bootstrap paths remain correct.
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint { checkReleaseBuilds = false }

    externalNativeBuild {
        cmake { path("src/main/jni/CMakeLists.txt") }
    }

    packaging { jniLibs { useLegacyPackaging = true } }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set(output.versionName.map { vn -> "DFRoot_${vn}.apk" })
        }
    }
}

dependencies { }
