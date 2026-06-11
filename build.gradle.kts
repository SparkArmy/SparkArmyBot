plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}



repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.jda)
    runtimeOnly(libs.jda.bc.typesafe.runtime)
    implementation(libs.bundles.data)
    implementation(libs.bundles.logging)

    implementation(platform(libs.data.exposed.bom))
    implementation(libs.bundles.data.exposed)
}





