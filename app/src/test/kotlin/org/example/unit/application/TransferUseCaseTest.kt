package org.example.unit.application

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.application.TransferUseCase
import org.example.entity.Account
import org.example.entity.Currency
import org.example.port.AccountRepository
import org.example.port.TransferRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TransferUseCaseTest {

    @Test
    fun `should transfer money between accounts successfully`() = runTest {
        //arrange
        val mockAccountRepository = mockk<AccountRepository>()
        val mockTransferRepository = mockk<TransferRepository>()

        coEvery { mockAccountRepository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")) }.returns(
            Account(
                id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                name = "Test Account 1",
                balance = BigDecimal("1000.00"),
                currency = Currency.BRL,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        )
        coEvery { mockAccountRepository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")) }.returns(
            Account(
                id = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
                name = "Test Account 2",
                balance = BigDecimal("1000.00"),
                currency = Currency.BRL,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        )
        coEvery { mockAccountRepository.update(any()) } returns Unit
        coEvery { mockTransferRepository.insert(any()) } returns Unit

        val transferUseCase = TransferUseCase(mockAccountRepository, mockTransferRepository)

        val input = TransferUseCase.Input(
            fromAccountId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            toAccountId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            amount = BigDecimal("100.00")
        )

        //act
        val output = transferUseCase.execute(input)

        //assert
        assertNotNull(output.transferId)
        coVerify(exactly = 2) { mockAccountRepository.findById(any()) }
        coVerify(exactly = 2) { mockAccountRepository.update(any()) }
        coVerify(exactly = 1) { mockTransferRepository.insert(any()) }
    }

    @Test
    fun `when transfer money between account if fromAccount has insufficient funds should throw an exception`() = runTest {
        //arrange
        val mockAccountRepository = mockk<AccountRepository>()
        val mockTransferRepository = mockk<TransferRepository>()

        coEvery { mockAccountRepository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")) }.returns(
            Account(
                id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                name = "Test Account 1",
                balance = BigDecimal("50.00"),
                currency = Currency.BRL,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        )
        coEvery { mockAccountRepository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")) }.returns(
            Account(
                id = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
                name = "Test Account 2",
                balance = BigDecimal("1000.00"),
                currency = Currency.BRL,
                createdAt = LocalDateTime.now(),
                updatedAt = null
            )
        )

        val transferUseCase = TransferUseCase(mockAccountRepository, mockTransferRepository)

        val input = TransferUseCase.Input(
            fromAccountId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            toAccountId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            amount = BigDecimal("100.00")
        )

        //act
        val exception = assertFailsWith<IllegalArgumentException> {
            transferUseCase.execute(input)
        }

        //assert
        assertEquals("Insufficient funds in account 123e4567-e89b-12d3-a456-426614174000", exception.message)
        coVerify(exactly = 2) { mockAccountRepository.findById(any()) }
        coVerify(exactly = 0) { mockAccountRepository.update(any()) }
        coVerify(exactly = 0) { mockTransferRepository.insert(any()) }
    }

    @Test
    fun `when transfer money between account if fromAccount not found should throw an exception`() = runTest {
        //arrange
        val mockAccountRepository = mockk<AccountRepository>()
        val mockTransferRepository = mockk<TransferRepository>()

        coEvery { mockAccountRepository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")) }.returns(null)
        coEvery { mockAccountRepository.findById(UUID.fromString("123e4567-e89b-12d3-a456-426614174001")) }.returns(mockk())

        val transferUseCase = TransferUseCase(mockAccountRepository, mockTransferRepository)

        val input = TransferUseCase.Input(
            fromAccountId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            toAccountId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001"),
            amount = BigDecimal("100.00")
        )

        //act
        val exception = assertFailsWith<IllegalArgumentException> {
            transferUseCase.execute(input)
        }

        //assert
        assertEquals("Account with id 123e4567-e89b-12d3-a456-426614174000 not found", exception.message)
        coVerify(exactly = 2) { mockAccountRepository.findById(any()) }
        coVerify(exactly = 0) { mockAccountRepository.update(any()) }
        coVerify(exactly = 0) { mockTransferRepository.insert(any()) }
    }
}