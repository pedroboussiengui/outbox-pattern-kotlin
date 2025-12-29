package org.example.unit.domain

import org.example.entity.Outbox
import org.example.entity.OutboxStatus
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OutboxTest {

    @Test
    fun `should create an outbox message`() {
        val outbox = Outbox.create(
            "Transfer",
            UUID.randomUUID(),
            "TransferCompleted",
            "{\"id\":\"123\"}"
        )
        assertNotNull(outbox.id)
        assertNotNull(outbox.aggregateId)
        assertEquals("Transfer", outbox.aggregateType)
        assertEquals("TransferCompleted", outbox.eventType)
        assertEquals("{\"id\":\"123\"}", outbox.payload)
        assertEquals(OutboxStatus.NEW, outbox.status)
        assertNotNull(outbox.createdAt)
        assertNull(outbox.processedAt)
    }

    @Test
    fun `should mark outbox message as sent`() {
        // Given
        val outbox = Outbox.create(
            "Transfer",
            UUID.randomUUID(),
            "TransferCreated",
            "{\"id\":\"123\"}"
        )
        // When
        val sentOutbox = outbox.markAsSent()
        // Then
        assertEquals(OutboxStatus.SENT, sentOutbox.status)
        assertNotNull(sentOutbox.processedAt)
    }

    @Test
    fun `should mark outbox message as failed`() {
        // Given
        val outbox = Outbox.create(
            "Transfer",
            UUID.randomUUID(),
            "TransferCreated",
            "{\"id\":\"123\"}"
        )
        // When
        val failedOutbox = outbox.markAsFailed()
        // Then
        assertEquals(OutboxStatus.FAILED, failedOutbox.status)
        assertNotNull(failedOutbox.processedAt)
    }
}