package de.sparkarmy

import de.sparkarmy.data.enumSetOf
import de.sparkarmy.database.DatabaseConfig
import de.sparkarmy.jda.JdaConfig
import de.sparkarmy.jdui.JduiConfig
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.social.twitch.TwitchConfig
import de.sparkarmy.social.youtube.YouTubeConfig
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val database: DatabaseConfig,
    val discord: JdaConfig,
    val defaultGuildFeatures: List<String> = emptyList(),
    val jduiConfig: JduiConfig,
    val twitchConfig: TwitchConfig,
    val youTubeConfig: YouTubeConfig,

    ) {
    val defaultGuildFeaturesParsed by lazy {
        val features = enumSetOf<GuildFeature>()

        defaultGuildFeatures.map { name ->
            val feature = GuildFeature.entries.find { it.name == name }
                ?: throw IllegalStateException("No GuildFeature found with name $name")

            features += feature
        }

        features
    }
}