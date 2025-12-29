package org.example.port

import org.example.entity.Outbox

interface OutboxRepository {
    suspend fun fetchBatch(limit: Int): List<Outbox>
    suspend fun update(outbox: Outbox)
}