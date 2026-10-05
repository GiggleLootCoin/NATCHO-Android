plugins { id("com.android.application") }
android {
    namespace = "ai.natcho.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "ai.natcho.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release { isMinifyEnabled = false }
        debug { applicationIdSuffix = ".debug"; versionNameSuffix = "-debug" }
    }
}
