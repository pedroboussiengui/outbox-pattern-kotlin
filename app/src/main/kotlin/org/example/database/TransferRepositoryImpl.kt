package org.example.database

import kotlinx.serialization.json.Json
import org.example.entity.OutboxModel
import org.example.entity.OutboxStatus
import org.example.entity.Transfer
import org.example.entity.TransferModel
import org.example.events.toEvent
import org.example.port.TransferRepository
import org.jetbrains.exposed.v1.jdbc.insert

class TransferRepositoryImpl : TransferRepository {

    override suspend fun insert(transfer: Transfer) {
        TransferModel.insert {
            it[id] = transfer.id
            it[fromAccountId] = transfer.fromAccountId
            it[toAccountId] = transfer.toAccountId
            it[amount] = transfer.amount
            it[transferStatus] = transfer.transferStatus
            it[createdAt] = transfer.createdAt
            it[completedAt] = transfer.completedAt
        }
        val event = transfer.toEvent()
        OutboxModel.insert {
            it[id] = event.eventId
            it[aggregateType] = "Transfer"
            it[aggregateId] = event.transferId
            it[eventType] = event.eventType
            it[payload] = Json.encodeToString(event)
            it[status] = OutboxStatus.NEW
            it[createdAt] = event.occurredAt
            it[processedAt] = null
        }
    }
}