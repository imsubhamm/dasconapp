package com.dascon.machinerylog.ui.logform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.api.MachineLogFormPayload
import com.dascon.machinerylog.data.local.SessionStore
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.util.displayDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val SHIFTS = listOf(
    "DAY" to "Day 6–2",
    "EVENING" to "Eve 2–10",
    "NIGHT" to "Night 10–6",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFormScreen(
    repository: LogRepository,
    sessionStore: SessionStore,
    projectId: Long,
    machine: MachineDto,
    logDate: String,
    onDone: (String) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var operator by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("DAY") }
    var startMeter by remember { mutableStateOf(machine.meterReading?.toPlain() ?: "") }
    var endMeter by remember { mutableStateOf("") }
    var opening by remember { mutableStateOf("") }
    var hsdIssue by remember { mutableStateOf("") }
    var hsdConsumption by remember { mutableStateOf("") }
    var work by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(machine.id, logDate, shift) {
        val savedOp = sessionStore.session.first().operatorName
        if (operator.isBlank() && !savedOp.isNullOrBlank()) operator = savedOp
        val prior = repository.priorFuel(machine.id, logDate, shift)
        if (prior != null) {
            val suggestedOpen = prior.suggestedOpeningBalance ?: prior.priorClosingBalance
            if (opening.isBlank() && suggestedOpen != null) {
                opening = suggestedOpen.toPlain()
            }
        }
    }

    val closingPreview = remember(opening, hsdIssue, hsdConsumption) {
        val o = opening.toDoubleOrNull() ?: 0.0
        val i = hsdIssue.toDoubleOrNull() ?: 0.0
        val c = hsdConsumption.toDoubleOrNull() ?: 0.0
        (o + i - c).let { if (it.isFinite()) "%.2f".format(it) else "" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(machine.machineCode ?: "Machine", fontWeight = FontWeight.Bold)
                        Text("${displayDate(logDate)} · ${machine.machineName.orEmpty()}")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Shift", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SHIFTS.forEach { (code, label) ->
                    FilterChip(
                        selected = shift == code,
                        onClick = { shift = code },
                        label = { Text(label) },
                    )
                }
            }

            BigField(
                label = "Operator name",
                value = operator,
                onChange = { operator = it },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BigField(
                    label = "Start meter",
                    value = startMeter,
                    onChange = { startMeter = it.filterNumeric() },
                    keyboard = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                BigField(
                    label = "End meter",
                    value = endMeter,
                    onChange = { endMeter = it.filterNumeric() },
                    keyboard = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BigField(
                    label = "Fuel open (L)",
                    value = opening,
                    onChange = { opening = it.filterNumeric() },
                    keyboard = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                BigField(
                    label = "HSD issue (L)",
                    value = hsdIssue,
                    onChange = { hsdIssue = it.filterNumeric() },
                    keyboard = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
            }
            BigField(
                label = "HSD consumption (L)",
                value = hsdConsumption,
                onChange = { hsdConsumption = it.filterNumeric() },
                keyboard = KeyboardType.Decimal,
            )
            Text("Closing ≈ $closingPreview L", style = MaterialTheme.typography.bodyMedium)
            BigField(
                label = "Work done",
                value = work,
                onChange = { work = it },
                singleLine = false,
            )
            BigField(
                label = "Remarks",
                value = remarks,
                onChange = { remarks = it },
                singleLine = false,
            )

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (operator.isBlank()) {
                        error = "Enter operator name"
                        return@Button
                    }
                    loading = true
                    error = null
                    scope.launch {
                        sessionStore.saveOperatorName(operator.trim())
                        val o = opening.toDoubleOrNull()
                        val issue = hsdIssue.toDoubleOrNull()
                        val cons = hsdConsumption.toDoubleOrNull()
                        val closing = if (o != null || issue != null || cons != null) {
                            (o ?: 0.0) + (issue ?: 0.0) - (cons ?: 0.0)
                        } else {
                            null
                        }
                        val payload = MachineLogFormPayload(
                            machineId = machine.id,
                            projectId = projectId,
                            logDate = logDate,
                            shift = shift,
                            operatorName = operator.trim(),
                            startMeterReading = startMeter.toDoubleOrNull(),
                            endMeterReading = endMeter.toDoubleOrNull(),
                            fuelOpeningBalance = o,
                            hsdIssue = issue,
                            hsdConsumption = cons,
                            fuelClosingBalance = closing,
                            workDescription = work.ifBlank { null },
                            remarks = remarks.ifBlank { null },
                        )
                        val label = machine.machineCode ?: machine.machineName ?: "Machine"
                        runCatching {
                            repository.saveLog(payload, label)
                        }.onSuccess { (online, msg) ->
                            loading = false
                            onDone(msg + if (online) "" else "")
                        }.onFailure {
                            loading = false
                            error = it.message ?: "Save failed"
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(24.dp),
                    )
                } else {
                    Text("Save log", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BigField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = modifier,
        textStyle = MaterialTheme.typography.titleMedium,
    )
}

private fun String.filterNumeric(): String =
    filterIndexed { index, c -> c.isDigit() || (c == '.' && !take(index).contains('.')) }

private fun Double.toPlain(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
