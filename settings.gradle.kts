pluginManagement {
    plugins {
        kotlin("jvm") version "2.3.20"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "We"
if (file("local.properties").exists()) {
    include("public", "private")
} else {
    include("public")
}