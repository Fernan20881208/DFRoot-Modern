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

    defaultConfig {
        // Independent installation for ARMv7-compatible UI.
        applicationId = "df.root.arm32.tv"
        // Android TV 11+ (API 30), 32-bit ARM user space; root engine unavailable.
        minSdk = 30
        targetSdk = 36
        versionCode = 40005
        versionName = "4.0-modern.tv5.4-arm32.1"
        ndk { abiFilters += listOf("armeabi-v7a") }
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
            // Distinct TV package; no original native bootstrap is built.
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
        cmake { path("src/main/jni/arm32/CMakeLists.txt") }
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
