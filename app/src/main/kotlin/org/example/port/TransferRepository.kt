package org.example.port

import org.example.entity.Transfer

interface TransferRepository {
    suspend fun insert(transfer: Transfer)
}