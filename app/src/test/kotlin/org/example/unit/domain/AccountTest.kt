package org.example.unit.domain

import org.example.entity.Account
import org.example.entity.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class AccountTest {

    @Test
    fun `should create an account successfully`() {
        val account = Account.create(
            name = "Test Account",
            currency = Currency.BRL,
            initialBalance = BigDecimal("100.00")
        )
        assertNotNull(account.id)
        assertEquals("Test Account", account.name)
        assertEquals(Currency.BRL, account.currency)
        assertEquals(BigDecimal("100.00"), account.balance)
        assertNotNull(account.createdAt)
        assertNull(account.updatedAt)
    }

    @Test
    fun `should debit from account correctly`() {
        val account = Account.create(
            name = "Test Account",
            currency = Currency.BRL,
            initialBalance = BigDecimal("100.00")
        )
        account.debit(BigDecimal("50.00"))
        assertEquals(BigDecimal("50.00"), account.balance)
        assertNotNull(account.updatedAt)
    }

    @Test
    fun `when debit from should launch an exception`() {
        val account = Account.create(
            name = "Test Account",
            currency = Currency.BRL,
            initialBalance = BigDecimal("100.00")
        )
        val exception = assertThrows<IllegalArgumentException> {
            account.debit(BigDecimal("150.00"))
        }
        assertEquals("Insufficient funds", exception.message)
    }

    @Test
    fun `should credit to account correctly`() {
        val account = Account.create(
            name = "Test Account",
            currency = Currency.BRL,
            initialBalance = BigDecimal("100.00")
        )
        account.credit(BigDecimal("50.00"))
        assertEquals(BigDecimal("150.00"), account.balance)
        assertNotNull(account.updatedAt)
    }
}