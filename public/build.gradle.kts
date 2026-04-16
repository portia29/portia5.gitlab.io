plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets["main"].kotlin {
    srcDir("src-main-kotlin")
}
sourceSets["main"].resources {
    srcDir("src-main-txt")
    srcDir("src-main-gen")
    srcDir("src-main-raw")
    srcDir("src-main-res")
}

sourceSets["test"].kotlin {
    srcDir("src-test-kotlin")
}
sourceSets["test"].resources {
    srcDir("src-test-res")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    // https://github.com/AdrianKuta/Tree-Data-Structure
    testImplementation("com.github.adriankuta:tree-structure:3.0.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}