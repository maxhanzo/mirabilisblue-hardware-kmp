plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.library)
    alias(libs.plugins.native.coroutines)
    alias(libs.plugins.kmmbridge)
    alias(libs.plugins.ksp)
    `maven-publish`
}

kotlin {
    androidTarget {
        publishAllLibraryVariants()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../testapps/ios/Podfile")
        framework {
            baseName = "hardware"
            isStatic = true
        }

        // pod("KMPNativeCoroutinesAsync","1.0.0-ALPHA-39")
        // pod("KMPNativeCoroutinesCombine","1.0.0-ALPHA-39")
    }


    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.kable)
            implementation(libs.napier)
            implementation(libs.com.ionspin.kotlin.bignum)
        }
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    namespace = "com.mirabilisblue.hardware"
}

addGithubPackagesRepository()

kmmbridge {
    //frameworkName.set("hardware")
    mavenPublishArtifacts()
    //spm(useCustomPackageFile = true, spmDirectory = "./../MirabilisBluePackage")
    spm()
}