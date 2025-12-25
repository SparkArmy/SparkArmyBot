plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    id("com.google.devtools.ksp") version "2.1.20-2.0.0"
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.10"
    id("io.ktor.plugin") version "3.1.3"
    application
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

application {
    mainClass.set("de.sparkarmy.Main")
}



repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // JDA and Webhook
    implementation("net.dv8tion:JDA:6.2.0")
    implementation("club.minnced:jda-ktx:0.13.0")
    implementation("dev.xirado:jdui-core:0.2.2")

    // Database, Exposed & Cache
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.flywaydb:flyway-core:11.20.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.20.0")
    implementation("com.sksamuel.aedile:aedile-core:2.1.2") // TODO Update to 3.0.1

    implementation("io.insert-koin:koin-core:4.1.1")
    implementation("io.insert-koin:koin-annotations:2.3.1")
    ksp("io.insert-koin:koin-ksp-compiler:2.3.1")

    val exposedVersion = "1.0.0-rc-4"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.23")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

    // Ktor
    val ktorVersion = "3.3.3"
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")


    // Other Dependencies
    implementation("org.json:json:20251224")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.6.0")
    implementation("org.tomlj:tomlj:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.github.twitch4j:twitch4j:1.25.0")
    implementation("com.github.twitch4j:twitch4j-kotlin:1.25.0")
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

ktor {
    fatJar {
        archiveFileName.set("SparkArmyBot.jar")
    }
    docker {
        jreVersion.set(JavaVersion.VERSION_21)
        imageTag.set("0.0.1")
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
                appName = provider { "sparkarmybot" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
    }
}




