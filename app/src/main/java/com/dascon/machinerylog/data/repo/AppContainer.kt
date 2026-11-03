package com.dascon.machinerylog.data.repo

import android.content.Context
import androidx.room.Room
import com.dascon.machinerylog.BuildConfig
import com.dascon.machinerylog.data.api.DasconApi
import com.dascon.machinerylog.data.local.AppDatabase
import com.dascon.machinerylog.data.local.SessionStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    val sessionStore = SessionStore(context)

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { sessionStore.session.first().token }
        val req = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(req)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            },
        )
        .build()

    val api: DasconApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(DasconApi::class.java)

    val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "dascon_machine_log.db",
    ).fallbackToDestructiveMigration().build()

    val repository = LogRepository(api, sessionStore, db.pendingLogDao(), moshi)
}
