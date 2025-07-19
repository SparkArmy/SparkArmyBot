package de.sparkarmy.database.entity

import de.sparkarmy.database.table.ContentCreators
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class ContentCreator(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ContentCreator>(ContentCreators)

    var contentCreatorName by ContentCreators.name
    var contentCreatorPlatform by ContentCreators.platform
}