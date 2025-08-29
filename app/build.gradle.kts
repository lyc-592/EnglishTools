plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.navigation.safe.args)
    id ("com.google.devtools.ksp") version "2.1.20-1.0.32"
}

android {
    namespace = "com.lyc.englishtools"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lyc.englishtools"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    buildFeatures {
        dataBinding = true  // ✅ 确保已启用
    }
    buildFeatures{
        viewBinding = true  // ✅ 确保已启用
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.appcompat.v161)
    implementation (libs.jackson.databind) //JSON 数据处理
    implementation (libs.gson) //JSON 序列化/反序列化
    implementation (libs.okhttp) //OkHttp 是一个高效的 HTTP 客户端库，用于发送 HTTP 请求和接收响应
    implementation (libs.androidx.room.runtime)
    ksp (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx) // Room KTX 扩展库，提供 Kotlin 协程支持
    testImplementation (libs.androidx.room.testing)
    implementation (libs.opencsv) // OpenCSV 用于处理 CSV 文件
    implementation (libs.glide)
    ksp ("com.github.bumptech.glide:ksp:4.14.2")
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

}