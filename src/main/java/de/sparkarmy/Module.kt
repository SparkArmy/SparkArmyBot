package de.sparkarmy

import de.sparkarmy.config.Config
import de.sparkarmy.config.readConfig
import de.sparkarmy.data.database.DatabaseConfig
import de.sparkarmy.jda.JdaConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("de.sparkarmy")
class Module {
    @Single fun provideConfig(): Config = readConfig()
    @Single fun provideDatabaseConfig(config: Config): DatabaseConfig = config.database
    @Single fun provideJdaConfig(config: Config): JdaConfig = config.discord

}