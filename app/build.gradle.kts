import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

// Load local.properties into project properties
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    val localProperties = Properties()
    localProperties.load(localPropertiesFile.inputStream())
    for ((key, value) in localProperties) {
        project.ext.set(key.toString(), value)
    }
}

android {
    namespace = "com.example.benassistant"
    compileSdk = 36

    fun getProperty(key: String, type: String = "property"): String =
        System.getenv(key)
            ?: project.findProperty(key) as? String
            ?: throw GradleException("Missing $type: $key")

    defaultConfig {
        applicationId = "com.example.benassistant"
        minSdk = 28
        targetSdk = 36
        versionCode = 29
        versionName = "v1.0.0"

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            '"' + getProperty("OPENAI_API_KEY", "config property") + '"'
        )

        buildConfigField(
            "String",
            "DEEPSEEK_API_KEY",
            '"' + getProperty("DEEPSEEK_API_KEY", "config property") + '"'
        )

        buildConfigField(
            "String",
            "FIREBASE_APP_ID",
            '"' + getProperty("FIREBASE_APP_ID", "config property") + '"'
        )

        buildConfigField(
            "String",
            "TELEGRAM_BOT_TOKEN",
            '"' + getProperty("TELEGRAM_BOT_TOKEN", "config property") + '"'
        )

        buildConfigField(
            "String",
            "TELEGRAM_DEV_CHAT_ID",
            '"' + getProperty("TELEGRAM_DEV_CHAT_ID", "config property") + '"'
        )

        buildConfigField(
            "String",
            "TELEGRAM_TESTER_CHAT_ID",
            '"' + getProperty("TELEGRAM_TESTER_CHAT_ID", "config property") + '"'
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (project.hasProperty("KEYSTORE_PASSWORD") || System.getenv("KEYSTORE_PASSWORD") != null) {
            create("release") {
                storeFile = file("ben-release-key.jks")
                storePassword = getProperty("KEYSTORE_PASSWORD", "signing property")
                keyAlias = getProperty("KEY_ALIAS", "signing property")
                keyPassword = getProperty("KEY_PASSWORD", "signing property")
            }
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.ktx)
    kapt("androidx.room:room-compiler:2.8.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
}
