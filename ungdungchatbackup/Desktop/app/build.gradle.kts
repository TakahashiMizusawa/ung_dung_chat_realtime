plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ungdungchatrealtime"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ungdungchatrealtime"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- Kết nối API & Chat ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.microsoft.signalr:signalr:8.0.0")
    implementation("org.slf4j:slf4j-jdk14:1.7.25")

    // --- THÊM MỚI: Thư viện hiển thị ảnh (Glide) ---
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")


    // --- THÊM MỚI: Thư viện bo tròn ảnh (Tùy chọn nhưng nên có cho Avatar) ---
    implementation("de.hdodenhof:circleimageview:3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}