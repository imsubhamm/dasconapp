package com.dascon.machinerylog.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("dascon_machine_log_session")

class SessionStore(private val context: Context) {
    private object Keys {
        val token = stringPreferencesKey("token")
        val email = stringPreferencesKey("email")
        val displayName = stringPreferencesKey("display_name")
        val projectId = longPreferencesKey("project_id")
        val projectName = stringPreferencesKey("project_name")
        val operatorName = stringPreferencesKey("operator_name")
    }

    val session: Flow<Session> = context.dataStore.data.map { prefs ->
        Session(
            token = prefs[Keys.token],
            email = prefs[Keys.email],
            displayName = prefs[Keys.displayName],
            projectId = prefs[Keys.projectId],
            projectName = prefs[Keys.projectName],
            operatorName = prefs[Keys.operatorName],
        )
    }

    suspend fun saveLogin(token: String, email: String, displayName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.token] = token
            prefs[Keys.email] = email
            prefs[Keys.displayName] = displayName
        }
    }

    suspend fun saveProject(projectId: Long, projectName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.projectId] = projectId
            prefs[Keys.projectName] = projectName
        }
    }

    suspend fun saveOperatorName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.operatorName] = name
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

data class Session(
    val token: String?,
    val email: String?,
    val displayName: String?,
    val projectId: Long?,
    val projectName: String?,
    val operatorName: String?,
) {
    val isLoggedIn: Boolean get() = !token.isNullOrBlank()
}
