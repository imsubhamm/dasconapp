package com.dascon.machinerylog.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.api.MachineLogEntryDto
import com.dascon.machinerylog.data.api.ProjectDto
import com.dascon.machinerylog.data.local.Session
import com.dascon.machinerylog.data.local.SessionStore
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.util.displayDate
import com.dascon.machinerylog.util.formatYmd
import com.dascon.machinerylog.util.parseYmd
import com.dascon.machinerylog.util.todayYmdIst
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sessionStore: SessionStore,
    repository: LogRepository,
    onLogout: () -> Unit,
    onNewLog: (projectId: Long, projectName: String, machine: MachineDto, logDate: String) -> Unit,
) {
    val session by sessionStore.session.collectAsState(initial = Session(null, null, null, null, null, null))
    val pending by repository.pendingLogs.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var logDate by remember { mutableStateOf(todayYmdIst()) }
    var projects by remember { mutableStateOf<List<ProjectDto>>(emptyList()) }
    var machines by remember { mutableStateOf<List<MachineDto>>(emptyList()) }
    var dayLogs by remember { mutableStateOf<List<MachineLogEntryDto>>(emptyList()) }
    var projectExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val projectId = session.projectId
    val projectName = session.projectName

    fun reloadDay() {
        val pid = projectId ?: return
        scope.launch {
            loading = true
            error = null
            runCatching {
                machines = repository.machines(pid)
                dayLogs = repository.logsForDay(pid, logDate)
            }.onFailure { error = it.message }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        runCatching { projects = repository.projects() }
            .onFailure { error = it.message }
    }

    LaunchedEffect(projectId, logDate) {
        if (projectId != null) reloadDay()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Machine Log", fontWeight = FontWeight.Bold)
                        Text(
                            session.displayName ?: session.email.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    if (pending.isNotEmpty()) {
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    val n = repository.syncPending()
                                    message = if (n > 0) "Synced $n offline log(s)" else "Nothing synced"
                                    reloadDay()
                                }
                            },
                            label = { Text("${pending.size} offline") },
                            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) },
                        )
                    }
                    IconButton(onClick = { reloadDay() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            repository.logout()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
            )
        },
        floatingActionButton = {
            if (projectId != null && machines.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        // Open first machine without log yet, or first machine
                        val loggedIds = dayLogs.mapNotNull { it.machineId }.toSet()
                        val target = machines.firstOrNull { it.id !in loggedIds } ?: machines.first()
                        onNewLog(projectId, projectName.orEmpty(), target, logDate)
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New log")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Date stepper — big and obvious for site use
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = {
                        logDate = formatYmd(parseYmd(logDate).minusDays(1))
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            displayDate(logDate),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (logDate == todayYmdIst()) "Today" else "Back date OK",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        )
                    }
                    IconButton(onClick = {
                        logDate = formatYmd(parseYmd(logDate).plusDays(1))
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next day", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = projectExpanded,
                onExpandedChange = { projectExpanded = it },
            ) {
                OutlinedTextField(
                    value = projectName ?: "Select project",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Project") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = projectExpanded,
                    onDismissRequest = { projectExpanded = false },
                ) {
                    projects.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.projectCode.orEmpty()} — ${p.projectName.orEmpty()}") },
                            onClick = {
                                projectExpanded = false
                                scope.launch {
                                    sessionStore.saveProject(p.id, "${p.projectCode} ${p.projectName}".trim())
                                }
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            if (message != null) {
                Text(message!!, color = MaterialTheme.colorScheme.primary)
            }
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Text(
                "Tap a machine to log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(machines, key = { it.id }) { machine ->
                    val existing = dayLogs.filter { it.machineId == machine.id }
                    MachineRow(
                        machine = machine,
                        logCount = existing.size,
                        summary = existing.firstOrNull()?.let {
                            val issue = it.hsdIssue ?: it.fuelConsumed
                            buildString {
                                if (it.operatorName != null) append(it.operatorName)
                                if (issue != null) {
                                    if (isNotEmpty()) append(" · ")
                                    append("${issue} L")
                                }
                            }
                        },
                        onClick = {
                            if (projectId != null) {
                                onNewLog(projectId, projectName.orEmpty(), machine, logDate)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineRow(
    machine: MachineDto,
    logCount: Int,
    summary: String?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    machine.machineCode ?: "Machine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text(machine.machineName ?: machine.machineType.orEmpty())
                if (!summary.isNullOrBlank()) {
                    Text(summary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                if (logCount > 0) "Logged ×$logCount" else "Add",
                color = if (logCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
