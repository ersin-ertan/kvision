plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("maven-publish")
}

repositories()

// Versions
val kotlinVersion: String by System.getProperties()
val serializationVersion: String by project
val coroutinesVersion: String by project
val springBootVersion: String by project
val springDataRelationalVersion: String by project
val jacksonModuleKotlinVersion: String by project

kotlin {
    kotlinJsTargets()
    kotlinJvmTargets()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":kvision-modules:kvision-common-annotations"))
                api(project(":kvision-modules:kvision-common-types"))
                api(project(":kvision-modules:kvision-common-remote"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk7"))
                implementation(kotlin("stdlib-jdk8"))
                implementation(kotlin("reflect"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
                api("org.springframework.boot:spring-boot-starter:$springBootVersion")
                api("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
                api("org.springframework.boot:spring-boot-starter-security:$springBootVersion")
                api("org.springframework.data:spring-data-relational:$springDataRelationalVersion")
                api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        if (name == "kotlinMultiplatform") artifactId = "kvision-server-spring-boot"
        pom {
            defaultPom()
        }
    }
}

setupPublication()
