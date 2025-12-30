package org.example.port

import org.example.entity.Account
import java.math.BigDecimal
import java.util.UUID

interface AccountRepository {
    suspend fun insert(account: Account)
    suspend fun findById(id: UUID): Account?
    suspend fun findByIdIn(ids: List<UUID>): List<Account>
    suspend fun update(account: Account)
    suspend fun applyBalanceChange(accountId: UUID, deltaAmount: BigDecimal)
}