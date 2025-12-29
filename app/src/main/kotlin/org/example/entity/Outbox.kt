package org.example.entity

import java.time.LocalDateTime
import java.util.UUID

data class Outbox(
    val id: UUID,
    val aggregateType: String,
    val aggregateId: UUID,
    val eventType: String,
    val payload: String,
    val status: OutboxStatus,
    val createdAt: LocalDateTime,
    val processedAt: LocalDateTime?
) {
    companion object {
        fun create(
            aggregateType: String,
            aggregateId: UUID,
            eventType: String,
            payload: String
        ): Outbox {
            return Outbox(
                id = UUID.randomUUID(),
                aggregateType = aggregateType,
                aggregateId = aggregateId,
                eventType = eventType,
                payload = payload,
                status = OutboxStatus.NEW,
                createdAt = LocalDateTime.now(),
                processedAt = null
            )
        }
    }

    fun markAsSent(): Outbox {
        return this.copy(status = OutboxStatus.SENT, processedAt = LocalDateTime.now())
    }

    fun markAsFailed(): Outbox {
        return this.copy(status = OutboxStatus.FAILED, processedAt = LocalDateTime.now())
    }

}


enum class OutboxStatus {
    NEW, PROCESSING, SENT, FAILED
}