package org.example.events

import java.time.LocalDateTime
import java.util.UUID

interface DomainEvent {
    val eventId: UUID
    val occurredAt: LocalDateTime
    val eventType: String
}