package org.example.database

import kotlinx.coroutines.Dispatchers
import org.example.port.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction


class ExposedTransactionManager : TransactionManager {

    override suspend fun <T> run(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
    }
}