package com.dascon.machinerylog.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DasconApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("projects")
    suspend fun listProjects(): ApiResponse<List<ProjectDto>>

    @GET("machinery/machines")
    suspend fun listMachines(
        @Query("projectId") projectId: Long? = null,
        @Query("activeOnly") activeOnly: Boolean = true,
    ): ApiResponse<List<MachineDto>>

    @GET("machinery/log-book")
    suspend fun listLogs(
        @Query("projectId") projectId: Long? = null,
        @Query("machineId") machineId: Long? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
    ): ApiResponse<List<MachineLogEntryDto>>

    @GET("machinery/log-book/prior-fuel")
    suspend fun priorFuel(
        @Query("machineId") machineId: Long,
        @Query("logDate") logDate: String,
        @Query("shift") shift: String? = null,
    ): ApiResponse<PriorFuelDto>

    @POST("machinery/log-book")
    suspend fun createLog(@Body body: MachineLogFormPayload): ApiResponse<MachineLogEntryDto>

    @PUT("machinery/log-book/{id}")
    suspend fun updateLog(
        @Path("id") id: Long,
        @Body body: MachineLogFormPayload,
    ): ApiResponse<MachineLogEntryDto>
}
