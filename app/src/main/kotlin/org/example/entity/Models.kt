package org.example.entity

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

object AccountModel : Table("account") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val balance = decimal("balance", 19, 2)
    val currency = enumerationByName<Currency>("currency", 3)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object TransferModel : Table("transfer") {
    val id = uuid("id")
    val fromAccountId = uuid("from_account_id")
    val toAccountId = uuid("to_account_id")
    val amount = decimal("amount", 19, 4)
    val transferStatus = enumerationByName<TransferStatus>("transfer_status", 10)
    val createdAt = datetime("created_at")
    val completedAt = datetime("completed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object OutboxModel : Table("outbox") {
    val id = uuid("id")                                                             // Unique ID
    val aggregateType = varchar("aggregate_type", 255)                      // Type of aggregate that generate the event
    val aggregateId = uuid("aggregate_id")                                          // ID of aggregate that generate the event
    val eventType = varchar("event_type", 255)                              // Type of event 'TransferComplete' or 'AccountDebited'
    val payload = text("payload")                                                   // Content of event serialized
    val status = enumerationByName<OutboxStatus>("status", 10)     // Status of event
    val createdAt = datetime("created_at")                                          // Created datetime
    val processedAt = datetime("processed_at").nullable()                           // Updated datetime

    override val primaryKey = PrimaryKey(id)
}