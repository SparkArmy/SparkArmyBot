plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.javacpp)
    alias(libs.plugins.ktor)
    application
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

application {
    mainClass.set("de.sparkarmy.MainKt")
}



repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.bundles.jda)

    // Database, Exposed & Cache
    implementation(libs.data.postgresql)
    implementation(libs.data.hikari)
    implementation(libs.bundles.data.flyway)
    implementation(libs.data.aedile)

    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)

    implementation(platform(libs.data.exposed.bom))
    implementation(libs.bundles.data.exposed)

    // Logging
    implementation(libs.bundles.logging)

    // Ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.bundles.ktor)

    ksp(libs.koin.ksp.compiler)

    // Other Dependencies
    implementation(libs.json)
    implementation(libs.okhttp)
    implementation(libs.annotations)

    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(platform(libs.kotlinx.bom))
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.stacktrace.decoroutinator.jvm)
    implementation(libs.tomlj)
    implementation(libs.jackson.databind)
    implementation(libs.bundles.twitch4j)
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




