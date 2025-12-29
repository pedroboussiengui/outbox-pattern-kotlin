package org.example.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Transfer(
    val id: UUID,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val amount: BigDecimal,
    val currency: Currency,
    var transferStatus: TransferStatus,
    val createdAt: LocalDateTime,
    var completedAt: LocalDateTime?
) {
    companion object {
        fun create(
            fromAccountId: UUID,
            toAccountId: UUID,
            amount: BigDecimal,
            currency: Currency
        ): Transfer {
            return Transfer(
                id = UUID.randomUUID(),
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amount = amount,
                currency = currency,
                transferStatus = TransferStatus.CREATED,
                createdAt = LocalDateTime.now(),
                completedAt = null
            )
        }
    }

    fun completeTransfer() {
        this.completedAt = LocalDateTime.now()
        this.transferStatus = TransferStatus.COMPLETED
    }

    fun failTransfer() {
        this.completedAt = LocalDateTime.now()
        this.transferStatus = TransferStatus.FAILED
    }
}

enum class TransferStatus {
    CREATED, COMPLETED, FAILED
}