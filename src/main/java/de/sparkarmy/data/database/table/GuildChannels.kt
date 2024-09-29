package de.sparkarmy.data.database.table

import net.dv8tion.jda.api.entities.channel.ChannelType
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GuildChannels: IdTable<Long>("table_guild_channel") {
    override val id = long("pk_cnl_id").entityId()
    val guild = reference("fk_cnl_guild_id", Guilds, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val name = varchar("cnl_name",100)
    val type = enumeration<ChannelType>("cnl_type").default(ChannelType.TEXT)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
