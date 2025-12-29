package org.example.events

import kotlinx.serialization.Serializable
import org.example.BigDecimalSerializer
import org.example.entity.Currency
import org.example.LocalDateTimeSerializer
import org.example.entity.Transfer
import org.example.UUIDSerializer
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class TransferCompleted(
    @Serializable(with = UUIDSerializer::class)
    override val eventId: UUID = UUID.randomUUID(),
    @Serializable(with = LocalDateTimeSerializer::class)
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = UUIDSerializer::class)
    val transferId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val fromAccountId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val toAccountId: UUID,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val currency: Currency
) : DomainEvent {
    override val eventType = "TransferCompleted"
}

fun Transfer.toEvent() = TransferCompleted(
    transferId = id,
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    amount = amount,
    currency = currency
)