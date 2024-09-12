package de.sparkarmy.data.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

abstract class AbstractBaseLongIdTable(name: String): IdTable<Long>(name)
abstract class AbstractBaseLongEntity(id: EntityID<Long>) : Entity<Long>(id)
abstract class AbstractBaseLongEntityClass<out E : AbstractBaseLongEntity>(table: AbstractBaseLongIdTable) : EntityClass<Long,E>(table)
