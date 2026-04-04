plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group   = "com.winwithpickr"
version = "0.4.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        moduleName = "engine"
        browser()
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

tasks.register<JavaExec>("verifyCli") {
    group = "application"
    description = "Verify a giveaway pick result from the command line"
    mainClass.set("com.winwithpickr.core.VerifyCliKt")
    val jvmMain = kotlin.jvm().compilations["main"]
    classpath = files(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
}

tasks.register<Jar>("verifyJar") {
    group = "build"
    description = "Build standalone pickr-verify.jar"
    archiveBaseName.set("pickr-verify")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest { attributes["Main-Class"] = "com.winwithpickr.core.VerifyCliKt" }
    val jvmMain = kotlin.jvm().compilations["main"]
    from(jvmMain.output.allOutputs)
    from(configurations.named("jvmRuntimeClasspath").map { config ->
        config.map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Copy>("assembleNpm") {
    group = "build"
    description = "Assemble npm package into build/npm-package/"
    dependsOn("jsBrowserProductionWebpack")
    from(layout.buildDirectory.file("kotlin-webpack/js/productionExecutable/engine.js")) {
        into("lib")
    }
    from(layout.projectDirectory.dir("packages/npm")) {
        include("package.json", "bin/**")
    }
    into(layout.buildDirectory.dir("npm-package"))
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("pickr-engine")
            description.set("Platform-agnostic verifiable random selection engine")
            url.set("https://github.com/winwithpickr/pickr-engine")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
        }
    }
}
