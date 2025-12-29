package org.example.integration

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.example.application.CreateAccount
import org.example.entity.Currency
import org.example.module
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CreateAccountApplicationTest {

    val postgresContainer = PostgreSQLContainer("postgres:16-alpine").apply {
        withDatabaseName("test")
        withUsername("test")
        withPassword("test")
        start()
    }

    @Test
    fun `should create an account successfully`() = testApplication {
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
        val response = client.post("/accounts") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateAccount.Input(
                    name = "John Doe",
                    currency = Currency.USD,
                    initialBalance = BigDecimal("100.00")
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.body<CreateAccount.Output>()
        assertNotNull(responseBody.accountId)
    }
}