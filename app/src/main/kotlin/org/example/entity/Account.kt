package org.example.entity

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID


class Account(
    val id: UUID,
    val name: String,
    var balance: BigDecimal,
    val currency: Currency,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime?
) {
    companion object {
        fun create(
            name: String,
            currency: Currency,
            initialBalance: BigDecimal
        ): Account {
            return Account(
                id = UUID.randomUUID(),
                name = name,
                balance = initialBalance,
                currency = currency,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        }
    }

    fun credit(amount: BigDecimal) {
        this.balance = this.balance.add(amount).setScale(2)
        this.updatedAt = LocalDateTime.now()
    }

    fun debit(amount: BigDecimal) {
        if (this.balance < amount) {
            throw IllegalArgumentException("Insufficient funds")
        }
        this.balance = this.balance.subtract(amount).setScale(2)
        this.updatedAt = LocalDateTime.now()
    }
}

enum class Currency {
    BRL, USD, EUR
}