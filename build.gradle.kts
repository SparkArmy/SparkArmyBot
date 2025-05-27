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
    implementation("io.github.freya022:JDA:6dc8746825")
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("dev.xirado:jdui-core:0.1.1")

    // Database, Exposed & Cache
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.flywaydb:flyway-core:11.7.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.7.1")
    implementation("com.sksamuel.aedile:aedile-core:2.0.3")

    implementation("io.insert-koin:koin-core:4.0.4")
    implementation("io.insert-koin:koin-annotations:2.0.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-server-core:3.1.3")
    implementation("io.ktor:ktor-server-core:3.1.3")
    implementation("io.ktor:ktor-serialization-gson:3.1.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-server-core:3.1.3")
    ksp("io.insert-koin:koin-ksp-compiler:2.0.0")

    val exposedVersion = "0.61.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

    // Ktor
    val ktorVersion = "3.1.3"
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:${ktorVersion}")



    // Other Dependencies
    implementation("org.json:json:20250107")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.4.8")
    implementation("org.tomlj:tomlj:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
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
        localImageName.set("sparkarmybot")
        imageTag.set("0.0.1")
        portMappings.set(
            listOf(
                io.ktor.plugin.features.DockerPortMapping(
                    8080,
                    8080,
                    io.ktor.plugin.features.DockerPortMappingProtocol.TCP
                )
            )
        )
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
                appName = provider { "sparkarmybot" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
    }
}




