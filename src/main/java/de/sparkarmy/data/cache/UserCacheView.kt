package de.sparkarmy.data.cache

import de.sparkarmy.data.database.DBContext
import de.sparkarmy.data.database.entity.User
import org.koin.core.annotation.Single
import net.dv8tion.jda.api.entities.User as JDAUser

private val populate: User.(JDAUser) -> Unit = {
    username = it.name
    displayName = it.globalName
}

@Single
class UserCacheView(
    private val db: DBContext
) : CacheView<Long, User>(1000) {
    suspend fun save(jdaUser: JDAUser, edit: User.() -> Unit = {}): User = db.doTransaction {
        val id = jdaUser.idLong

        val user = getById(id)?.apply { updateMetadata(jdaUser); edit(this) }
            ?: User.new(id) { setMetadata(jdaUser); edit(this) }

        if (id !in this@UserCacheView)
            put(id, user)

        user
    }

    private fun User.setMetadata(jdaUser: JDAUser) {
        username = jdaUser.name
        displayName = jdaUser.globalName
    }

    private fun User.updateMetadata(jdaUser: JDAUser) {
        if (username != jdaUser.name)
            username = jdaUser.name
        if (displayName != jdaUser.globalName)
            displayName = jdaUser.globalName
    }

    override suspend fun load(key: Long): User? = db.doTransaction {
        User.findById(key)
    }
}