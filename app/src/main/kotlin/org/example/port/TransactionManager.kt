package org.example.port

interface TransactionManager {
    suspend fun <T> run(block: suspend () -> T): T
}