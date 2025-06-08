package de.sparkarmy.database.entity

import de.sparkarmy.database.table.ContentCreators
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ContentCreator(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ContentCreator>(ContentCreators)

    var contentCreatorName by ContentCreators.name
    var contentCreatorPlatform by ContentCreators.platform
}