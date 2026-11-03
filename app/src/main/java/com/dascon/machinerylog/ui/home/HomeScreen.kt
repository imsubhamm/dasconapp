package com.dascon.machinerylog.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dascon.machinerylog.R
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.api.MachineLogEntryDto
import com.dascon.machinerylog.data.api.ProjectDto
import com.dascon.machinerylog.data.local.Session
import com.dascon.machinerylog.data.local.SessionStore
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.ui.theme.DscplBlue
import com.dascon.machinerylog.ui.theme.DscplBlueDark
import com.dascon.machinerylog.ui.theme.DscplInk
import com.dascon.machinerylog.ui.theme.DscplLine
import com.dascon.machinerylog.ui.theme.DscplMist
import com.dascon.machinerylog.ui.theme.DscplMuted
import com.dascon.machinerylog.ui.theme.DscplRed
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
    var projectMenuOpen by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val projectId = session.projectId
    val projectName = session.projectName
    val loggedIds = dayLogs.mapNotNull { it.machineId }.toSet()

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
        containerColor = DscplMist,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DscplInk,
                    actionIconContentColor = DscplInk,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_dscpl),
                            contentDescription = "DSCPL",
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(36.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Column {
                            Text(
                                "Machine Log",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DscplInk,
                            )
                            Text(
                                session.displayName ?: session.email.orEmpty(),
                                fontSize = 12.sp,
                                color = DscplMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                actions = {
                    if (pending.isNotEmpty()) {
                        Text(
                            "${pending.size} offline",
                            color = DscplRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        val n = repository.syncPending()
                                        message = if (n > 0) "Synced $n offline log(s)" else "Nothing synced"
                                        reloadDay()
                                    }
                                }
                                .padding(horizontal = 8.dp),
                        )
                        Icon(Icons.Default.Sync, contentDescription = null, tint = DscplRed, modifier = Modifier.size(18.dp))
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
                        val target = machines.firstOrNull { it.id !in loggedIds } ?: machines.first()
                        onNewLog(projectId, projectName.orEmpty(), target, logDate)
                    },
                    containerColor = DscplBlue,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New log")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                // Big date control — easy to tap
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DscplBlue)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { logDate = formatYmd(parseYmd(logDate).minusDays(1)) }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            displayDate(logDate),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                        )
                        Text(
                            if (logDate == todayYmdIst()) "Today" else "Back date",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    IconButton(onClick = { logDate = formatYmd(parseYmd(logDate).plusDays(1)) }) {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            item {
                Text(
                    "${loggedIds.size} of ${machines.size} machines logged",
                    color = DscplInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }

            item {
                // Simple project picker — high contrast
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.5.dp, DscplLine, RoundedCornerShape(14.dp))
                            .clickable { projectMenuOpen = true }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("PROJECT", color = DscplMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                projectName ?: "Tap to select project",
                                color = DscplInk,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = DscplInk, modifier = Modifier.size(28.dp))
                    }
                    DropdownMenu(
                        expanded = projectMenuOpen,
                        onDismissRequest = { projectMenuOpen = false },
                    ) {
                        projects.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${p.projectCode.orEmpty()} — ${p.projectName.orEmpty()}",
                                        color = DscplInk,
                                        fontSize = 16.sp,
                                    )
                                },
                                onClick = {
                                    projectMenuOpen = false
                                    scope.launch {
                                        sessionStore.saveProject(p.id, "${p.projectCode} ${p.projectName}".trim())
                                    }
                                },
                            )
                        }
                    }
                }
            }

            if (message != null) {
                item { Text(message!!, color = DscplBlue, fontWeight = FontWeight.Medium) }
            }
            if (error != null) {
                item { Text(error!!, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium) }
            }
            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DscplBlue)
                    }
                }
            }

            item {
                Text(
                    "Machines",
                    color = DscplInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            items(machines, key = { it.id }) { machine ->
                val existing = dayLogs.filter { it.machineId == machine.id }
                val logged = existing.isNotEmpty()
                MachineCard(
                    machine = machine,
                    logged = logged,
                    summary = existing.firstOrNull()?.let {
                        val issue = it.hsdIssue ?: it.fuelConsumed
                        buildString {
                            if (!it.operatorName.isNullOrBlank()) append(it.operatorName)
                            if (issue != null) {
                                if (isNotEmpty()) append(" · ")
                                append("$issue L")
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

@Composable
private fun MachineCard(
    machine: MachineDto,
    logged: Boolean,
    summary: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = if (logged) DscplBlue.copy(alpha = 0.35f) else DscplLine,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                machine.machineCode ?: "Machine",
                color = DscplInk,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
            Text(
                machine.machineName ?: machine.machineType.orEmpty(),
                color = DscplMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!summary.isNullOrBlank()) {
                Text(
                    summary,
                    color = DscplBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (logged) DscplBlue else DscplRed)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                if (logged) "DONE" else "LOG",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}
