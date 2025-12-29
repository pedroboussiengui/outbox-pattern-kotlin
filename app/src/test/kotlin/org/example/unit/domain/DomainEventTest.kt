package org.example.unit.domain

import kotlinx.serialization.json.Json
import org.example.entity.Currency
import org.example.entity.Transfer
import org.example.events.TransferCompleted
import org.example.events.toEvent
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DomainEventTest {

    @Test
    fun `when toEvent transfer should generate a transferCompleted event successfully`() {
        // Arrange
        val transfer = Transfer.create(
            fromAccountId = UUID.randomUUID(),
            toAccountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.BRL
        )
        // Act
        val event = transfer.toEvent()
        // Assert
        assertNotNull(event.eventId)
        assertNotNull(event.occurredAt)
        assertEquals("TransferCompleted", event.eventType)
        assertEquals(transfer.id, event.transferId)
        assertEquals(transfer.fromAccountId, event.fromAccountId)
        assertEquals(transfer.toAccountId, event.toAccountId)
        assertEquals(transfer.amount, event.amount)
        assertEquals(transfer.currency, event.currency)
    }

    @Test
    fun `should serialize event correctly`() {
        // Arrange
        val transfer = Transfer.create(
            fromAccountId = UUID.randomUUID(),
            toAccountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.BRL
        )
        val event = transfer.toEvent()
        // Act
        val encodedEvent = Json.encodeToString(event)
        val decodedEvent = Json.decodeFromString(TransferCompleted.serializer(), encodedEvent)
        // Assert
        assertEquals(event.eventId, decodedEvent.eventId)
        assertEquals(event.occurredAt, decodedEvent.occurredAt)
        assertEquals(event.eventType, decodedEvent.eventType)
        assertEquals(event.transferId, decodedEvent.transferId)
        assertEquals(event.fromAccountId, decodedEvent.fromAccountId)
        assertEquals(event.toAccountId, decodedEvent.toAccountId)
        assertEquals(event.amount, decodedEvent.amount)
        assertEquals(event.currency, decodedEvent.currency)
    }
}