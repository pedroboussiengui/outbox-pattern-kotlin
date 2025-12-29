package org.example.integration

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.example.application.CreateAccount
import org.example.application.GetAccountById
import org.example.application.TransferUseCase
import org.example.entity.Currency
import org.example.module
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransferMoneyApplicationTest {

    val postgresContainer = PostgreSQLContainer("postgres:16-alpine").apply {
        withDatabaseName("test")
        withUsername("test")
        withPassword("test")
        start()
    }

    @Test
    fun `should transfer money between two accounts successfully`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.jdbcUrl" to postgresContainer.jdbcUrl,
                "database.user" to postgresContainer.username,
                "database.password" to postgresContainer.password
            )
        }
        application {
            module()
        }
        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        // creating from account
        val responseBodyAccount1 = client.post("/accounts") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateAccount.Input(
                    name = "John Doe",
                    currency = Currency.USD,
                    initialBalance = BigDecimal("1000.00")
                )
            )
        }.body<CreateAccount.Output>()
        // creating to account
        val responseBodyAccount2 = client.post("/accounts") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateAccount.Input(
                    name = "Jane Doe",
                    currency = Currency.USD,
                    initialBalance = BigDecimal("1000.00")
                )
            )
        }.body<CreateAccount.Output>()
        // transfer from account1 to account2
        val response = client.post("/transfers") {
            contentType(ContentType.Application.Json)
            setBody(
                TransferUseCase.Input(
                    fromAccountId = responseBodyAccount1.accountId,
                    toAccountId = responseBodyAccount2.accountId,
                    amount = BigDecimal("100.00")
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.body<TransferUseCase.Output>()
        assertNotNull(responseBody.transferId)
        // get account 1 and check balance
        val account1 = client.get("/accounts/${responseBodyAccount1.accountId}").body<GetAccountById.Output>()
        assertEquals(BigDecimal("900.00"), account1.balance)
        // get account 2 and check balance
        val account2 = client.get("/accounts/${responseBodyAccount2.accountId}").body<GetAccountById.Output>()
        assertEquals(BigDecimal("1100.00"), account2.balance)
    }
}