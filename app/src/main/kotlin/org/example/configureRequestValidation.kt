package org.example

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import org.example.application.CreateAccount
import org.example.application.TransferUseCase
import org.example.entity.Currency
import org.valiktor.ConstraintViolationException
import org.valiktor.functions.hasSize
import org.valiktor.functions.isIn
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotNull
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.i18n.mapToMessage
import org.valiktor.validate
import java.util.Locale

fun CreateAccount.Input.validate() {
    validate(this) {
        validate(CreateAccount.Input::name).isNotBlank().hasSize(min = 3, max = 20)
        validate(CreateAccount.Input::currency).isNotNull().isIn(Currency.entries.toSet())
        validate(CreateAccount.Input::initialBalance).isNotNull().isPositiveOrZero()
    }
}

fun TransferUseCase.Input.validate() {
    validate(this) {
        validate(TransferUseCase.Input::fromAccountId).isNotNull()
        validate(TransferUseCase.Input::toAccountId).isNotNull()
        validate(TransferUseCase.Input::amount).isNotNull().isPositiveOrZero()
    }
}

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<CreateAccount.Input> { request ->
            try {
                request.validate()
                ValidationResult.Valid
            } catch (e: ConstraintViolationException) {
                ValidationResult.Invalid(validationErrors(e))
            }
        }
        validate<TransferUseCase.Input> { request ->
            try {
                request.validate()
                ValidationResult.Valid
            } catch (e: ConstraintViolationException) {
                ValidationResult.Invalid(validationErrors(e))
            }
        }
    }
}

fun validationErrors(e: ConstraintViolationException): List<String> =
    e.constraintViolations
        .mapToMessage(baseName = "messages", locale = Locale.ENGLISH)
        .map { "${it.property}: ${it.message}" }