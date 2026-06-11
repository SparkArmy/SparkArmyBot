package de.sparkarmy.config

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object Environment {
    /**
     * The folder where the data and configuration directories reside.
     */
    val folder: Path = Path("configs")

    /**
     * The mode is determined by checking if the
     * `dev-config` directory exists in the current directory.
     */
    val isDev: Boolean = folder.resolve("dev").exists()

    val configFolder: Path =
        folder.resolve(if (isDev) "dev" else "config")
    val logbackConfigPath: Path = configFolder.resolve(if (isDev) "logback-test.xml" else "logback.xml")
}