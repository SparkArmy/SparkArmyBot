import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.commons.io.FileUtils


plugins {
    java
    application
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("de.sparkarmy.de.sparkarmy.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
repositories {
    mavenCentral()
}

dependencies {
    // JDA and Webhook
    implementation("net.dv8tion:JDA:5.1.0")
    implementation("club.minnced:discord-webhooks:0.8.4")

    // Database
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Twitch and YouTube
    implementation("com.github.twitch4j:twitch4j:1.21.0")
    implementation("com.github.philippheuer.events4j:events4j-handler-reactor:0.12.1")
    implementation("com.google.apis:google-api-services-youtube:v3-rev20240514-2.0.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")

    // Ktor
    val ktorVersion = "2.3.12"

    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")

    // Exposed
    val exposedVersion = "0.54.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    // Other Dependencies
    implementation("org.json:json:20240303")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20")

}

tasks.register("clearResources") {
    FileUtils.deleteDirectory(FileUtils.getFile(project.rootDir, "build/resources/main"))
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("SparkArmyBot.jar")
        mergeServiceFiles()
    }
}



