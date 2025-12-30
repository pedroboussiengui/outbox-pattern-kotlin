package org.example.database

import org.example.entity.Outbox
import org.example.entity.OutboxModel
import org.example.entity.OutboxStatus
import org.example.port.OutboxRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.vendors.ForUpdateOption
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class OutboxRepositoryImpl: OutboxRepository {

    override suspend fun fetchBatch(limit: Int): List<Outbox> {
        return transaction {
            val rows = OutboxModel
                .selectAll().where { OutboxModel.status eq OutboxStatus.NEW }
                .orderBy(OutboxModel.createdAt)
                .limit(limit)
                .forUpdate(ForUpdateOption.PostgreSQL.ForUpdate(ForUpdateOption.PostgreSQL.MODE.SKIP_LOCKED))
                .toList()

            if (rows.isNotEmpty()) {
                OutboxModel.update({ OutboxModel.id inList rows.map { it[OutboxModel.id] }}) {
                    it[status] = OutboxStatus.PROCESSING
                }
            }

            rows.map { it.toDomain() }
        }
    }

    override suspend fun update(outbox: Outbox) {
        transaction {
            OutboxModel.update({ OutboxModel.id eq outbox.id }) {
                it[status] = outbox.status
                it[processedAt] = outbox.processedAt
            }
        }
    }

    private fun ResultRow.toDomain() = Outbox(
        id = this[OutboxModel.id],
        aggregateType = this[OutboxModel.aggregateType],
        aggregateId = this[OutboxModel.aggregateId],
        eventType = this[OutboxModel.eventType],
        payload = this[OutboxModel.payload],
        status = this[OutboxModel.status],
        createdAt = this[OutboxModel.createdAt],
        processedAt = this[OutboxModel.processedAt]
    )
}