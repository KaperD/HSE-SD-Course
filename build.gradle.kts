import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    application
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

group = "ru.hse"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    jvmTarget = "11"
    setSource(files("src/main/kotlin"))
    buildUponDefaultConfig = true
    autoCorrect = true
    config.setFrom(files("$rootDir/config/detekt/config.yml"))

    reports {
        xml.enabled = false
        html.enabled = true
    }
}
