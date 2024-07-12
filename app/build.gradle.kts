plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.navigation.safeargs.kotlin)
}

android {
    namespace = "blackorbs.dev.moviefinder"
    compileSdk = 34

    defaultConfig {
        applicationId = "blackorbs.dev.moviefinder"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "blackorbs.dev.moviefinder.CustomHiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        )
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.timber)

    //Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    //Networking
    implementation(libs.coil)
    implementation(libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.coroutines.android)

    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    testImplementation(libs.androidx.core.ktx)
    testImplementation (libs.robolectric)
    testImplementation(libs.androidx.junit.ktx)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.junit)

    //Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.fragment.testing.manifest)
    androidTestImplementation(libs.androidx.fragment.testing)
    androidTestImplementation(libs.okhttp3.idling.resource)
    androidTestImplementation(libs.coil.test)
    androidTestImplementation(libs.androidx.espresso.contrib)
}