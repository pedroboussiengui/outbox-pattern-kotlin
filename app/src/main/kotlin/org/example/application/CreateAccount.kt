package org.example.application

import kotlinx.serialization.Serializable
import org.example.BigDecimalSerializer
import org.example.UUIDSerializer
import org.example.entity.Account
import org.example.entity.Currency
import org.example.port.AccountRepository
import java.math.BigDecimal
import java.util.UUID

class CreateAccount(
    private val accountRepository: AccountRepository
) {
    suspend fun execute(input: Input): Output {
        val account = Account.create(
            name = input.name,
            currency = input.currency,
            initialBalance = input.initialBalance
        )
        accountRepository.insert(account)
        return Output(accountId = account.id)
    }

    @Serializable
    data class Input(
        val name: String,
        val currency: Currency,
        @Serializable(with = BigDecimalSerializer::class)
        val initialBalance: BigDecimal
    )

    @Serializable
    data class Output(
        @Serializable(with = UUIDSerializer::class)
        val accountId: UUID
    )
}