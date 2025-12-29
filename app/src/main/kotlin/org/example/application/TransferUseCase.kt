package org.example.application

import io.ktor.server.plugins.NotFoundException
import kotlinx.serialization.Serializable
import org.example.BigDecimalSerializer
import org.example.UUIDSerializer
import org.example.entity.Transfer
import org.example.port.AccountRepository
import org.example.port.TransferRepository
import java.math.BigDecimal
import java.util.UUID

class TransferUseCase(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository
) {
    suspend fun execute(input: Input): Output {
        val fromAccount = accountRepository.findById(input.fromAccountId)
        val toAccount = accountRepository.findById(input.toAccountId)

        if (fromAccount == null) {
            throw NotFoundException("Account with id ${input.fromAccountId} not found")
        }

        if (toAccount == null) {
            throw NotFoundException("Account with id ${input.toAccountId} not found")
        }

        if (fromAccount.balance < input.amount) {
            throw IllegalArgumentException("Insufficient funds in account ${input.fromAccountId}")
        }

        if (fromAccount.currency != toAccount.currency) {
            throw IllegalArgumentException("Cannot transfer between accounts with different currencies")
        }

        if (fromAccount.id == toAccount.id) {
            throw IllegalArgumentException("Cannot transfer to the same account")
        }

        // TODO: Update account balances
         fromAccount.debit(input.amount)
         toAccount.credit(input.amount)
         accountRepository.update(fromAccount)
         accountRepository.update(toAccount)

        val transfer = Transfer.create(
            fromAccountId = input.fromAccountId,
            toAccountId = input.toAccountId,
            amount = input.amount,
            currency = fromAccount.currency
        )

        transferRepository.insert(transfer)

        return Output(transferId = transfer.id)
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