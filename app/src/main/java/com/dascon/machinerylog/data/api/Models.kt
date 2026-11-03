package com.dascon.machinerylog.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val status: Int? = null,
    val success: Boolean? = null,
    val message: String? = null,
    val data: T? = null,
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String? = null,
    val refreshToken: String? = null,
    val id: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String? = null,
    val message: String? = null,
)

@JsonClass(generateAdapter = true)
data class ProjectDto(
    val id: Long,
    val projectCode: String? = null,
    val projectName: String? = null,
    val isActive: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class MachineDto(
    val id: Long,
    val machineCode: String? = null,
    val machineName: String? = null,
    val machineType: String? = null,
    val projectId: Long? = null,
    val projectName: String? = null,
    val meterReading: Double? = null,
    val meterUnit: String? = null,
    val isActive: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class MachineLogEntryDto(
    val id: Long? = null,
    val machineId: Long? = null,
    val machineCode: String? = null,
    val machineName: String? = null,
    val projectId: Long? = null,
    val projectName: String? = null,
    val logDate: String? = null,
    val shift: String? = null,
    val operatorName: String? = null,
    val startMeterReading: Double? = null,
    val endMeterReading: Double? = null,
    val fuelConsumed: Double? = null,
    val hsdIssue: Double? = null,
    val hsdConsumption: Double? = null,
    val fuelOpeningBalance: Double? = null,
    val fuelClosingBalance: Double? = null,
    val fuelTankCapacity: Double? = null,
    val workDescription: String? = null,
    val remarks: String? = null,
)

@JsonClass(generateAdapter = true)
data class MachineLogFormPayload(
    val machineId: Long,
    val projectId: Long,
    val logDate: String,
    val shift: String? = null,
    val operatorName: String? = null,
    val startMeterReading: Double? = null,
    val endMeterReading: Double? = null,
    val fuelTankCapacity: Double? = null,
    val fuelOpeningBalance: Double? = null,
    val hsdIssue: Double? = null,
    val hsdConsumption: Double? = null,
    val fuelClosingBalance: Double? = null,
    val priorHsdConsumption: Double? = null,
    val workDescription: String? = null,
    val remarks: String? = null,
)

@JsonClass(generateAdapter = true)
data class PriorFuelDto(
    val suggestedOpeningBalance: Double? = null,
    val suggestedTankCapacity: Double? = null,
    val priorHsdConsumption: Double? = null,
    val priorClosingBalance: Double? = null,
    val priorNeedsConsumption: Boolean? = null,
)
