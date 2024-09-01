package de.sparkarmy.data.database.table

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.math.BigDecimal

object DiscordChannels : IdTable<BigDecimal>("bot.table_channel") {
    override val id = decimal("pk_cnl_id", 50, 0).entityId()
    val guildId = optReference(
        "fk_cnl_guild_id",
        DiscordGuilds,
        ReferenceOption.CASCADE,
        ReferenceOption.CASCADE,
        "table_channel_table_guild_pk_gld_id_fk"
    )
    val webhookUrl = varchar("cnl_webhook_url", 20000).nullable()
    val values = long("cnl_value").default(0)

}