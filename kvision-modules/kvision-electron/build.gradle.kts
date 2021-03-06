plugins {
    kotlin("js")
    id("maven-publish")
}

repositories()

// Versions
val nodeJsVersion: String by project

kotlin {
    js {
        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
            }
        }
        nodejs {
            testTask {
                useKarma()
            }
        }
    }
}

dependencies {
    api(rootProject)
    api("org.jetbrains.kotlinx:kotlinx-nodejs:$nodeJsVersion")
    implementation(npm("electron", "^9.2.1"))
    testImplementation(kotlin("test-js"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                defaultPom()
            }
        }
    }
}

setupPublication()
