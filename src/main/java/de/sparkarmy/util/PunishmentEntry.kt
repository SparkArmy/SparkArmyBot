package de.sparkarmy.util

import de.sparkarmy.model.PunishmentType
import io.github.freya022.botcommands.api.core.entities.InputUser
import kotlin.time.Duration

class PunishmentEntry(
    type: PunishmentType,
    offender: InputUser,
    moderator: InputUser,
    reason: String,
    timeframe: Duration?
) {

}