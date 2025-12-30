package org.example.database

import org.example.entity.Account
import org.example.entity.AccountModel
import org.example.port.AccountRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class AccountRepositoryImpl : AccountRepository {

    override suspend fun insert(account: Account) {
        AccountModel.insert {
            it[id] = account.id
            it[name] = account.name
            it[balance] = account.balance
            it[currency] = account.currency
            it[createdAt] = account.createdAt
            it[updatedAt] = account.updatedAt
        }
    }

    override suspend fun findById(id: UUID): Account? {
        return AccountModel.selectAll().where { AccountModel.id eq id }
            .map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun findByIdIn(ids: List<UUID>): List<Account> {
        return AccountModel.selectAll().where { AccountModel.id inList ids }
            .forUpdate()
            .orderBy(AccountModel.id to SortOrder.ASC)
            .map { it.toDomain() }
    }

    override suspend fun update(account: Account) {
        AccountModel.update({ AccountModel.id eq account.id }) {
            it[name] = account.name
            it[balance] = account.balance
            it[currency] = account.currency
            it[createdAt] = account.createdAt
            it[updatedAt] = account.updatedAt
        }
    }

    override suspend fun applyBalanceChange(accountId: UUID, deltaAmount: BigDecimal) {
        AccountModel.update({ AccountModel.id eq accountId }) {
            with(SqlExpressionBuilder) {
                it.update(balance, balance + deltaAmount)
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }

    private fun ResultRow.toDomain(): Account {
        return Account(
            id = this[AccountModel.id],
            name = this[AccountModel.name],
            balance = this[AccountModel.balance],
            currency = this[AccountModel.currency],
            createdAt = this[AccountModel.createdAt],
            updatedAt = this[AccountModel.updatedAt]
        )
    }
}