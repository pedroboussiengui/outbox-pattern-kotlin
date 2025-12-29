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

//    override suspend fun fetchBatch(limit: Int): List<Outbox> {
//        return transaction {
//            val events = mutableListOf<Outbox>()
//            val sql = """
//                SELECT *
//                FROM outbox
//                WHERE status = 'NEW'
//                ORDER BY created_at
//                FOR UPDATE SKIP LOCKED
//                UPD
//                LIMIT ?
//            """.trimIndent()
//            // Use exec with args to prevent SQL injection
//            exec(sql, args = listOf(IntegerColumnType() to limit)) { resultSet ->
//                while (resultSet.next()) {
//                    events.add(
//                        Outbox(
//                            id = UUID.fromString(resultSet.getString("id")),
//                            aggregateType = resultSet.getString("aggregate_type"),
//                            aggregateId = UUID.fromString(resultSet.getString("aggregate_id")),
//                            eventType = resultSet.getString("event_type"),
//                            payload = resultSet.getString("payload"),
//                            status = OutboxStatus.valueOf(resultSet.getString("status")),
//                            processedAt = resultSet.getTimestamp("processed_at")?.toLocalDateTime(),
//                            createdAt = resultSet.getTimestamp("created_at").toLocalDateTime()
//                        )
//                    )
//                }
//            }
//            if (events.isNotEmpty()) {
//                // Marcar como PROCESSING na mesma transaction
//                val ids = events.joinToString(",") { "'${it.id}'" }
//
//                val updateSql = """
//                    UPDATE outbox
//                    SET status = 'PROCESSING'
//                    WHERE id IN ($ids)
//                """.trimIndent()
//
//                exec(updateSql)
//            }
//            events
//        }
//    }

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

            rows.map { it.toOutbox() }
        }
    }

    private fun ResultRow.toOutbox() = Outbox(
        id = this[OutboxModel.id],
        aggregateType = this[OutboxModel.aggregateType],
        aggregateId = this[OutboxModel.aggregateId],
        eventType = this[OutboxModel.eventType],
        payload = this[OutboxModel.payload],
        status = this[OutboxModel.status],
        createdAt = this[OutboxModel.createdAt],
        processedAt = this[OutboxModel.processedAt]
    )

    override suspend fun update(outbox: Outbox) {
        transaction {
            OutboxModel.update({ OutboxModel.id eq outbox.id }) {
                it[status] = outbox.status
                it[processedAt] = outbox.processedAt
            }
        }
    }
}