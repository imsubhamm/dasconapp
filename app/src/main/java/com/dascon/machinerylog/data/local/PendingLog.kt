package com.dascon.machinerylog.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pending_logs")
data class PendingLogEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val payloadJson: String,
    val machineLabel: String,
    val logDate: String,
    val createdAtEpochMs: Long,
    val lastError: String? = null,
)

@Dao
interface PendingLogDao {
    @Query("SELECT * FROM pending_logs ORDER BY createdAtEpochMs ASC")
    fun observeAll(): Flow<List<PendingLogEntity>>

    @Query("SELECT * FROM pending_logs ORDER BY createdAtEpochMs ASC")
    suspend fun getAll(): List<PendingLogEntity>

    @Insert
    suspend fun insert(entity: PendingLogEntity): Long

    @Query("DELETE FROM pending_logs WHERE localId = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE pending_logs SET lastError = :error WHERE localId = :id")
    suspend fun markError(id: Long, error: String)
}

@Database(entities = [PendingLogEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingLogDao(): PendingLogDao
}
