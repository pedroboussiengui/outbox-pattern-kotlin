package org.example.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.entity.Account
import org.example.entity.AccountModel
import org.example.port.AccountRepository
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.util.UUID

class AccountRepositoryImpl : AccountRepository {
    override suspend fun insert(account: Account) {
        withContext(Dispatchers.IO) {
            transaction {
                AccountModel.insert {
                    it[id] = account.id
                    it[name] = account.name
                    it[balance] = account.balance
                    it[currency] = account.currency
                    it[createdAt] = account.createdAt
                    it[updatedAt] = account.updatedAt
                }
            }
        }
    }

    override suspend fun findById(id: UUID): Account? {
        return withContext(Dispatchers.IO) {
            transaction {
                AccountModel.selectAll().where { AccountModel.id eq id }
                    .map {
                        Account(
                            id = it[AccountModel.id],
                            name = it[AccountModel.name],
                            balance = it[AccountModel.balance],
                            currency = it[AccountModel.currency],
                            createdAt = it[AccountModel.createdAt],
                            updatedAt = it[AccountModel.updatedAt]
                        )
                    }
                    .singleOrNull()
            }
        }
    }

    override suspend fun update(account: Account) {
        withContext(Dispatchers.IO) {
            transaction {
                AccountModel.update({ AccountModel.id eq account.id }) {
                    it[name] = account.name
                    it[balance] = account.balance
                    it[currency] = account.currency
                    it[createdAt] = account.createdAt
                    it[updatedAt] = account.updatedAt
                }
            }
        }
    }
}