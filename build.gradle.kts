plugins {
    id("com.android.application") version "8.1.0"
    kotlin("android") version "1.9.0"
}

android {
    namespace = "com.example.torrentstreamer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.torrentstreamer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Per i torrent
    implementation("org.libtorrent4j:libtorrent4j:2.0.5")
    
    // Per il parsing HTML
    implementation("org.jsoup:jsoup:1.15.3")
    
    // Per le richieste HTTP
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // Per il video player
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")
}
