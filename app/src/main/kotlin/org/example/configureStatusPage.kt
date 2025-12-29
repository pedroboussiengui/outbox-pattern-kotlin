package org.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Problem(
    val message: String,
    val status: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val errors: List<String>? = null
)

fun Application.configureStatusPage() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                Problem(
                    message = "Validation failed",
                    status = HttpStatusCode.BadRequest.value,
                    errors = cause.reasons
                )
            )
        }
        exception<BadRequestException> { call, cause ->
           call.respond(
                HttpStatusCode.BadRequest,
                Problem(
                    message = cause.message ?: "Bad Request",
                    status = HttpStatusCode.BadRequest.value
                )
            )
        }
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                Problem(
                    message = cause.message ?: "Entity not found",
                    status = HttpStatusCode.NotFound.value
                )
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.UnprocessableEntity,
                Problem(
                    message = cause.message ?: "Business rule violation",
                    status = HttpStatusCode.UnprocessableEntity.value
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error: ${cause.message}")
        }
    }
}
