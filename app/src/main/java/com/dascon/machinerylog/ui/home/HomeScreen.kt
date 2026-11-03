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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dascon.machinerylog.R
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.api.MachineLogEntryDto
import com.dascon.machinerylog.data.api.ProjectDto
import com.dascon.machinerylog.data.local.Session
import com.dascon.machinerylog.data.local.SessionStore
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.ui.theme.DscplBlue
import com.dascon.machinerylog.ui.theme.DscplBlueDark
import com.dascon.machinerylog.ui.theme.DscplBlueMid
import com.dascon.machinerylog.ui.theme.DscplMist
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
    var projectExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val projectId = session.projectId
    val projectName = session.projectName
    val loggedCount = dayLogs.mapNotNull { it.machineId }.toSet().size

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DscplBlueDark,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.logo_dscpl),
                            contentDescription = "DSCPL",
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(3.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Column {
                            Text("Machine Log", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                session.displayName ?: session.email.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
                            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.12f),
                                labelColor = Color.White,
                                leadingIconContentColor = Color.White,
                            ),
                            border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color.White.copy(alpha = 0.2f)),
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
                        val loggedIds = dayLogs.mapNotNull { it.machineId }.toSet()
                        val target = machines.firstOrNull { it.id !in loggedIds } ?: machines.first()
                        onNewLog(projectId, projectName.orEmpty(), target, logDate)
                    },
                    containerColor = DscplRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                DateHero(
                    logDate = logDate,
                    onPrev = { logDate = formatYmd(parseYmd(logDate).minusDays(1)) },
                    onNext = { logDate = formatYmd(parseYmd(logDate).plusDays(1)) },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatPill(
                        label = "Machines",
                        value = machines.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatPill(
                        label = "Logged today",
                        value = loggedCount.toString(),
                        accent = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = projectExpanded,
                    onExpandedChange = { projectExpanded = it },
                ) {
                    OutlinedTextField(
                        value = projectName ?: "Select project",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project site") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DscplBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
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
            }

            if (message != null) {
                item {
                    Text(message!!, color = DscplBlue, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (error != null) {
                item {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
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
                    "Fleet for this day",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "Tap a machine to open the daily log sheet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

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
                                append("${issue} L HSD")
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
private fun DateHero(
    logDate: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, spotColor = DscplBlueDark.copy(alpha = 0.25f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(DscplBlueDark, DscplBlue, DscplBlueMid),
                ),
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day", tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    displayDate(logDate),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                Text(
                    if (logDate == todayYmdIst()) "Today · IST" else "Back-dated entry",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day", tint = Color.White)
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (accent) DscplRed.copy(alpha = 0.08f) else Color.White)
            .border(
                1.dp,
                if (accent) DscplRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(18.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            color = if (accent) DscplRed else DscplBlueDark,
        )
    }
}

@Composable
private fun MachineRow(
    machine: MachineDto,
    logCount: Int,
    summary: String?,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, spotColor = Color.Black.copy(alpha = 0.08f))
            .clip(shape)
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (logCount > 0) DscplBlue.copy(alpha = 0.12f) else DscplMist,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                (machine.machineCode ?: "M").take(2).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = if (logCount > 0) DscplBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                machine.machineCode ?: "Machine",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                machine.machineName ?: machine.machineType.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!summary.isNullOrBlank()) {
                Text(
                    summary,
                    style = MaterialTheme.typography.labelMedium,
                    color = DscplBlue,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (logCount > 0) DscplBlue else DscplRed)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Text(
                if (logCount > 0) "Logged" else "Log now",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
}
