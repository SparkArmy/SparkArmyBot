plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.google.cloud.tools.jib") version "3.4.2"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.10"
    application
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}



repositories {
    mavenCentral()
    maven("https://maven.xirado.dev/releases")
    maven("https://maven.xirado.dev/jitpack")
}

dependencies {
    // JDA and Webhook
    implementation("io.github.JDA-Fork:JDA:ef41a9445d")
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("at.xirado:JDUI:0.4.5")

    // Database, Exposed & Cache
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:10.17.3")
    implementation("org.flywaydb:flyway-database-postgresql:10.17.3")
    implementation("com.sksamuel.aedile:aedile-core:1.3.1")

    implementation("io.insert-koin:koin-core:4.0.0-RC1")
    implementation("io.insert-koin:koin-annotations:1.4.0-RC4")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")

    val exposedVersion = "0.54.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

    // Ktor
    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")



    // Other Dependencies
    implementation("org.json:json:20240303")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.4.4")
    implementation("org.tomlj:tomlj:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.4.4")
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}




