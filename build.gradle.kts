plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group   = "dev.pickrtweet"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "pickr-parser.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("pickr")
            description.set("Verifiable random giveaway winner selection engine")
            url.set("https://github.com/bmcreations/pickr")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
        }
    }
}
