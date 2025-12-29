package org.example.port

import org.example.entity.Account
import java.util.UUID

interface AccountRepository {
    suspend fun insert(account: Account)
    suspend fun findById(id: UUID): Account?
    suspend fun update(account: Account)
}