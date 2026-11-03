package com.dascon.machinerylog.data.repo

import com.dascon.machinerylog.data.api.DasconApi
import com.dascon.machinerylog.data.api.LoginRequest
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.api.MachineLogEntryDto
import com.dascon.machinerylog.data.api.MachineLogFormPayload
import com.dascon.machinerylog.data.api.PriorFuelDto
import com.dascon.machinerylog.data.api.ProjectDto
import com.dascon.machinerylog.data.local.PendingLogDao
import com.dascon.machinerylog.data.local.PendingLogEntity
import com.dascon.machinerylog.data.local.SessionStore
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class LogRepository(
    private val api: DasconApi,
    private val sessionStore: SessionStore,
    private val pendingDao: PendingLogDao,
    moshi: Moshi,
) {
    private val payloadAdapter = moshi.adapter(MachineLogFormPayload::class.java)

    val pendingLogs: Flow<List<PendingLogEntity>> = pendingDao.observeAll()

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val res = api.login(LoginRequest(email.trim(), password))
        val token = res.token?.takeIf { it.isNotBlank() }
            ?: error(res.message ?: "Login failed")
        val name = listOfNotNull(res.firstName, res.lastName)
            .joinToString(" ")
            .ifBlank { res.email ?: email }
        sessionStore.saveLogin(token, res.email ?: email, name)
    }

    suspend fun logout() {
        sessionStore.clear()
    }

    suspend fun projects(): List<ProjectDto> {
        val res = api.listProjects()
        if (res.success == false) error(res.message ?: "Could not load projects")
        return (res.data.orEmpty()).filter { it.isActive != false }
            .sortedBy { it.projectName ?: it.projectCode ?: "" }
    }

    suspend fun machines(projectId: Long): List<MachineDto> {
        val res = api.listMachines(projectId = projectId, activeOnly = true)
        if (res.success == false) error(res.message ?: "Could not load machines")
        return res.data.orEmpty().sortedBy { it.machineCode ?: it.machineName ?: "" }
    }

    suspend fun logsForDay(projectId: Long, date: String): List<MachineLogEntryDto> {
        val res = api.listLogs(projectId = projectId, fromDate = date, toDate = date)
        if (res.success == false) error(res.message ?: "Could not load logs")
        return res.data.orEmpty().sortedBy { it.machineCode ?: it.machineName }
    }

    suspend fun priorFuel(machineId: Long, logDate: String, shift: String?): PriorFuelDto? {
        return runCatching {
            val res = api.priorFuel(machineId, logDate, shift)
            if (res.success == false) null else res.data
        }.getOrNull()
    }

    /**
     * Tries online create; on network failure queues locally for later sync.
     * @return Pair(syncedOnline, message)
     */
    suspend fun saveLog(
        payload: MachineLogFormPayload,
        machineLabel: String,
        forceQueue: Boolean = false,
    ): Pair<Boolean, String> {
        if (!forceQueue) {
            try {
                val res = api.createLog(payload)
                if (res.success == false) {
                    error(res.message ?: "Save failed")
                }
                return true to "Saved to Log Book"
            } catch (e: IOException) {
                // fall through to queue
            } catch (e: Exception) {
                // Non-network API errors should surface
                if (e.message?.contains("Unable to resolve host", true) == true ||
                    e.message?.contains("failed to connect", true) == true ||
                    e.message?.contains("timeout", true) == true
                ) {
                    // queue
                } else {
                    throw e
                }
            }
        }

        pendingDao.insert(
            PendingLogEntity(
                payloadJson = payloadAdapter.toJson(payload),
                machineLabel = machineLabel,
                logDate = payload.logDate,
                createdAtEpochMs = System.currentTimeMillis(),
            ),
        )
        return false to "Saved offline — will sync when online"
    }

    suspend fun syncPending(): Int {
        var synced = 0
        for (item in pendingDao.getAll()) {
            val payload = payloadAdapter.fromJson(item.payloadJson) ?: continue
            try {
                val res = api.createLog(payload)
                if (res.success == false) {
                    pendingDao.markError(item.localId, res.message ?: "Sync failed")
                    continue
                }
                pendingDao.delete(item.localId)
                synced++
            } catch (e: Exception) {
                pendingDao.markError(item.localId, e.message ?: "Sync failed")
            }
        }
        return synced
    }
}
