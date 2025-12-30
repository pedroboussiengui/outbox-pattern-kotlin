package org.example.application

import io.ktor.server.plugins.*
import kotlinx.serialization.Serializable
import org.example.BigDecimalSerializer
import org.example.UUIDSerializer
import org.example.entity.Transfer
import org.example.port.AccountRepository
import org.example.port.TransactionManager
import org.example.port.TransferRepository
import java.math.BigDecimal
import java.util.*

class TransferUseCase(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository,
    private val transferManager: TransactionManager
) {
    suspend fun execute(input: Input): Output {
        return transferManager.run {
            val accounts = accountRepository.findByIdIn(listOf(input.fromAccountId, input.toAccountId).sorted())

            val fromAccount = accounts.find { it.id == input.fromAccountId }
                ?: throw NotFoundException("Account with ID ${input.fromAccountId} not found")

            val toAccount = accounts.find { it.id == input.toAccountId }
                ?: throw NotFoundException("Account with ID ${input.toAccountId} not found")

            if (fromAccount.balance < input.amount) {
                throw IllegalArgumentException("Insufficient funds in account ${input.fromAccountId}")
            }

            if (fromAccount.currency != toAccount.currency) {
                throw IllegalArgumentException("Cannot transfer between accounts with different currencies")
            }

            if (fromAccount.id == toAccount.id) {
                throw IllegalArgumentException("Cannot transfer to the same account")
            }

            fromAccount.debit(input.amount)
            toAccount.credit(input.amount)
            accountRepository.applyBalanceChange(fromAccount.id, input.amount.negate())
            accountRepository.applyBalanceChange(toAccount.id, input.amount)

            val transfer = Transfer.create(
                fromAccountId = fromAccount.id,
                toAccountId = toAccount.id,
                amount = input.amount,
                currency = fromAccount.currency
            )

            transferRepository.insert(transfer)

            Output(transferId = transfer.id)
        }
    }

    @Serializable
    data class Input(
        @Serializable(with = UUIDSerializer::class)
        val fromAccountId: UUID,
        @Serializable(with = UUIDSerializer::class)
        val toAccountId: UUID,
        @Serializable(with = BigDecimalSerializer::class)
        val amount: BigDecimal
    )

    @Serializable
    data class Output(
        @Serializable(with = UUIDSerializer::class)
        val transferId: UUID
    )
}