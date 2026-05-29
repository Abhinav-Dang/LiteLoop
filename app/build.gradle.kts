plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

fun getGitVersionName(): String {
    return try {
        val process = Runtime.getRuntime().exec("git describe --tags --abbrev=0")
        val reader = process.inputStream.bufferedReader()
        val tag = reader.readLine()?.trim() ?: "1.1.0"
        tag.replace(Regex("^v"), "").split("-")[0]
    } catch (e: Exception) {
        "1.1.0"
    }
}

fun getGitVersionCode(): Int {
    return try {
        val process = Runtime.getRuntime().exec("git rev-list --count HEAD")
        val reader = process.inputStream.bufferedReader()
        val count = reader.readLine()?.trim()?.toInt() ?: 1
        count
    } catch (e: Exception) {
        1
    }
}

android {
    namespace = "com.example.liteloop"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.liteloop"
        minSdk = 30
        targetSdk = 36
        versionCode = getGitVersionCode()
        versionName = getGitVersionName()
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val type = if (variant.name.contains("debug")) "dbg" else "rel"
            val verName = variant.outputs.first().versionName.get()
            val fileName = "LiteLoop_${verName}_$type.apk"
            output.outputFileName.set(fileName)
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)
    implementation(libs.core.splashscreen)
    implementation(libs.play.services.wearable)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.wear.tooling.preview)
    implementation(libs.compose.icons.extended)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Lifecycle & Navigation
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)
    implementation(libs.wear.compose.navigation)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    debugImplementation(libs.ui.test.manifest)
    debugImplementation(libs.ui.tooling)

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        setForkEvery(100)
    }
}
