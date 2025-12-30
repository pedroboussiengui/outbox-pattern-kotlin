package org.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.example.application.CreateAccount
import org.example.application.GetAccountById
import org.example.application.TransferUseCase
import org.example.database.AccountRepositoryImpl
import org.example.database.ExposedTransactionManager
import org.example.database.OutboxRepositoryImpl
import org.example.database.TransferRepositoryImpl
import org.example.entity.AccountModel
import org.example.entity.Outbox
import org.example.entity.OutboxModel
import org.example.entity.TransferModel
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val config = HikariConfig().apply {
        jdbcUrl = environment.config.property("database.jdbcUrl").getString()
        driverClassName = "org.postgresql.Driver"
        username = environment.config.property("database.user").getString()
        password = environment.config.property("database.password").getString()
        maximumPoolSize = 20
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_READ_COMMITTED" // assure forupdate
        validate()
    }
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    transaction {
        SchemaUtils.drop(AccountModel, TransferModel, OutboxModel)
        SchemaUtils.create(AccountModel, TransferModel, OutboxModel)
    }
    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
    }
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
        })
    }
    configureRequestValidation()
    configureStatusPage()
    configureRouting()
    startOutboxWorker()
}

fun Application.configureRouting() {
    val accountRepository = AccountRepositoryImpl()
    val transferRepository = TransferRepositoryImpl()
    val transactionManager = ExposedTransactionManager()
    val transferUseCase = TransferUseCase(accountRepository, transferRepository, transactionManager)
    val createAccount = CreateAccount(accountRepository, transactionManager)
    val getAccountById = GetAccountById(accountRepository, transactionManager)

    routing {
        route("/accounts") {
            post {
                val request = call.receive<CreateAccount.Input>()
                val response = createAccount.execute(request)
                call.respond(HttpStatusCode.Created, response)
            }
            get("/{accountId}") {
                val accountId = call.parameters["accountId"]
                    ?: throw IllegalArgumentException("Account ID not found")
                val request = GetAccountById.Input(UUID.fromString(accountId))
                val response = getAccountById.execute(request)
                call.respond(HttpStatusCode.OK, response)
            }
        }
        route("/transfers") {
            post {
                val request = call.receive<TransferUseCase.Input>()
                val response = transferUseCase.execute(request)
                call.respond(HttpStatusCode.Created, response)
            }
        }
    }
}

fun Application.startOutboxWorker() {
    val outboxRepository = OutboxRepositoryImpl()
    val publisher = EventPublisherImpl()

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    scope.launch {
        while (true) {
            val outboxEvents = outboxRepository.fetchBatch(50)
            outboxEvents.forEach { event ->
                try {
                    publisher.publish(event)
                    val updatedMessage = event.markAsSent()
                    outboxRepository.update(updatedMessage)
                } catch (e: Exception) {
                    val updatedMessage = event.markAsFailed()
                    outboxRepository.update(updatedMessage)
                }
            }
            delay(500)
        }
    }
}

interface EventPublisher {
    fun publish(event: Outbox) {
        println("Publish event ${event.id}")
    }
}

class EventPublisherImpl : EventPublisher {
    override fun publish(event: Outbox) {
        println("Publishing event with ID: ${event.id}, Type: ${event.eventType}, Payload: ${event.payload}")
    }
}