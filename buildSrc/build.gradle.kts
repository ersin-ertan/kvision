plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.71")
    implementation(gradleApi())
}
