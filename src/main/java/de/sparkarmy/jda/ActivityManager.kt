package de.sparkarmy.jda

import de.sparkarmy.coroutines.newCoroutineScope
import de.sparkarmy.coroutines.timer
import de.sparkarmy.coroutines.virtualDispatcher
import de.sparkarmy.database.entity.BotStatus
import de.sparkarmy.model.BotActivity
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.annotations.BEventListener.RunMode
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger { }

@BService()
class ActivityManager {

    @BEventListener(mode = RunMode.ASYNC)
    fun activityProvider(event: GuildReadyEvent) {
        val scope = newCoroutineScope<ActivityManager>(virtualDispatcher)
        scope.timer(10.toDuration(DurationUnit.MINUTES), 0.toDuration(DurationUnit.MINUTES)) {
            suspendTransaction {
                val entries = BotStatus.all()
                val count = entries.count()
                val randomNumber = when (count > 0) {
                    true -> Random.nextLong(0, count)
                    else -> 0
                }

                logger.info { randomNumber }

                val randomEntry = BotStatus.findById(randomNumber) ?: BotStatus.findById(0) ?: BotStatus.new(0) {
                    status = "Music"
                    activity += EnumSet.of(BotActivity.LISTENING)
                }

                val activity = randomEntry.activity.first()

                val botActivity = Activity.of(Activity.ActivityType.fromKey(activity.offset - 1), randomEntry.status)
                event.jda.shardManager?.setActivity(botActivity)
            }
        }
    }
}