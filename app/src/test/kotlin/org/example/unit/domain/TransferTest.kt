package org.example.unit.domain

import org.example.entity.Currency
import org.example.entity.Transfer
import org.example.entity.TransferStatus
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TransferTest {

    @Test
    fun `should create a transfer successfully`() {
        val transfer = Transfer.create(
            fromAccountId = UUID.randomUUID(),
            toAccountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.BRL
        )
        assertNotNull(transfer.id)
        assertNotNull(transfer.fromAccountId)
        assertNotNull(transfer.toAccountId)
        assertEquals(BigDecimal("100.00"), transfer.amount)
        assertEquals(Currency.BRL, transfer.currency)
        assertNotNull(transfer.createdAt)
        assertEquals(TransferStatus.CREATED, transfer.transferStatus)
        assertNull(transfer.completedAt)
    }

    @Test
    fun `should complete a transfer`() {
        val transfer = Transfer.create(
            fromAccountId = UUID.randomUUID(),
            toAccountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.BRL
        )
        transfer.completeTransfer()
        assertEquals(TransferStatus.COMPLETED, transfer.transferStatus)
        assertNotNull(transfer.completedAt)
    }

    @Test
    fun `should fail a transfer`() {
        val transfer = Transfer.create(
            fromAccountId = UUID.randomUUID(),
            toAccountId = UUID.randomUUID(),
            amount = BigDecimal("100.00"),
            currency = Currency.BRL
        )
        transfer.failTransfer()
        assertEquals(TransferStatus.FAILED, transfer.transferStatus)
        assertNotNull(transfer.completedAt)
    }
}