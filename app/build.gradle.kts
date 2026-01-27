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

// Helpers to read config safely
fun getOptional(key: String): String? =
    System.getenv(key) ?: project.findProperty(key) as? String

fun requireProp(key: String, type: String = "config property"): String =
    getOptional(key) ?: throw GradleException("Missing $type: $key")

fun quoted(value: String?): String = '"' + (value ?: "") + '"'

android {
    namespace = "com.example.benassistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.benassistant"
        minSdk = 28
        targetSdk = 36
        versionCode = 29
        versionName = "v1.0.0"

        // Use optional values for CI/test builds to avoid configuration-time failures
        buildConfigField("String", "OPENAI_API_KEY", quoted(getOptional("OPENAI_API_KEY")))
        buildConfigField("String", "DEEPSEEK_API_KEY", quoted(getOptional("DEEPSEEK_API_KEY")))
        buildConfigField("String", "FIREBASE_APP_ID_DEV", quoted(getOptional("FIREBASE_APP_ID_DEV")))
        buildConfigField("String", "FIREBASE_APP_ID_STAGING", quoted(getOptional("FIREBASE_APP_ID_STAGING")))
        buildConfigField("String", "FIREBASE_APP_ID_PROD", quoted(getOptional("FIREBASE_APP_ID_PROD")))
        buildConfigField("String", "TELEGRAM_BOT_TOKEN", quoted(getOptional("TELEGRAM_BOT_TOKEN")))
        buildConfigField("String", "TELEGRAM_DEV_CHAT_ID", quoted(getOptional("TELEGRAM_DEV_CHAT_ID")))
        buildConfigField("String", "TELEGRAM_TESTER_CHAT_ID", quoted(getOptional("TELEGRAM_TESTER_CHAT_ID")))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions.add("env")
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"api-dev.vn\"")
        }
        create("staging") {
            dimension = "env"
            applicationIdSuffix = ".stg"
            versionNameSuffix = "-stg"
            buildConfigField("String", "BASE_URL", "\"api-stg.vn\"")
        }
        create("prod") {
            dimension = "env"
            // Production keeps the base package without suffix
            buildConfigField("String", "BASE_URL", "\"api.vn\"")
        }
    }

    signingConfigs {
        // Only configure release signing when credentials are present
        if (project.hasProperty("KEYSTORE_PASSWORD") || System.getenv("KEYSTORE_PASSWORD") != null) {
            create("release") {
                storeFile = file("ben-release-key.jks")
                storePassword = requireProp("KEYSTORE_PASSWORD", "signing property")
                keyAlias = requireProp("KEY_ALIAS", "signing property")
                keyPassword = requireProp("KEY_PASSWORD", "signing property")
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

    // Bump Java toolchain to 17 to match AGP requirements on CI
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
