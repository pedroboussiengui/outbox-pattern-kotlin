package org.example.application

import io.ktor.server.plugins.NotFoundException
import kotlinx.serialization.Serializable
import org.example.BigDecimalSerializer
import org.example.UUIDSerializer
import org.example.entity.Currency
import org.example.port.AccountRepository
import org.example.port.TransactionManager
import java.math.BigDecimal
import java.util.*

class GetAccountById(
    private val accountRepository: AccountRepository,
    private val transactionManager: TransactionManager
) {
    suspend fun execute(input: Input): Output {
        return transactionManager.run {
            val account = accountRepository.findById(input.accountId)
                ?: throw NotFoundException("Account with ID ${input.accountId} not found")
            Output(
                id = account.id,
                name = account.name,
                balance = account.balance,
                currency = account.currency
            )
        }
    }

    data class Input(
        val accountId: UUID
    )

    @Serializable
    data class Output(
        @Serializable(with = UUIDSerializer::class)
        val id: UUID,
        val name: String,
        @Serializable(with = BigDecimalSerializer::class)
        var balance: BigDecimal,
        val currency: Currency
    )
}