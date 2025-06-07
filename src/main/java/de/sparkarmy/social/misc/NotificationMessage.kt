package de.sparkarmy.social.misc

import de.sparkarmy.database.entity.GuildNotificationChannel
import de.sparkarmy.database.table.GuildNotificationChannels
import de.sparkarmy.jda.JDAService
import de.sparkarmy.util.roleMention
import dev.minn.jda.ktx.coroutines.await
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.RestAction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

suspend fun createNotificationMessage(
    jdaService: JDAService,
    contentCreatorId: String,
    notificationLink: String,
    lastPublishing: Instant,
) {
    val notificationChannelList = suspendTransaction {

        GuildNotificationChannel.find {
            GuildNotificationChannels.contentCreator eq contentCreatorId
        }.toList()
    }

    val restActions = suspendTransaction {
        notificationChannelList.stream()
            .map { notificationChannel ->
                val time = notificationChannel.lastTime
                if (time == lastPublishing) return@map null
                val rolesMentions = notificationChannel.pingRoles.stream()
                    .map { roleMention(it) }
                    .toList().toString()
                    .removeSurrounding("[", "]")
                val message = notificationChannel.pingMessage

                // TODO Change to webhook handling

                val channel = jdaService.shardManager.getGuildChannelById(notificationChannel.channel.value)
                if (channel == null) {
                    return@map null
                }
                val type = channel.type

                val messageString = "$rolesMentions $message \n $notificationLink"
                notificationChannel.lastTime = lastPublishing
                restAction(type, channel, messageString)

            }
            .filter { it != null }
            .toList()
    }

    if (restActions.isEmpty()) return

    RestAction.allOf(restActions).await()
}

private fun restAction(
    type: ChannelType,
    channel: GuildChannel,
    messageString: String
): RestAction<out Any?> = when (type) {
    ChannelType.TEXT -> {
        channel as TextChannel
        channel.sendMessage(messageString)
    }

    ChannelType.NEWS -> {
        channel as NewsChannel
        channel.sendMessage(messageString)
    }

    ChannelType.GUILD_NEWS_THREAD, ChannelType.GUILD_PUBLIC_THREAD -> {
        channel as ThreadChannel
        channel.sendMessage(messageString)
    }

    else -> {
        channel.manager
    }
}