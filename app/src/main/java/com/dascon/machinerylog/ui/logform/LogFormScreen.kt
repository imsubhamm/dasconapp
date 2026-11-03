package com.dascon.machinerylog.ui.logform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dascon.machinerylog.data.api.MachineDto
import com.dascon.machinerylog.data.api.MachineLogFormPayload
import com.dascon.machinerylog.data.local.SessionStore
import com.dascon.machinerylog.data.repo.LogRepository
import com.dascon.machinerylog.ui.theme.DscplBlue
import com.dascon.machinerylog.ui.theme.DscplBlueDark
import com.dascon.machinerylog.ui.theme.DscplRed
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

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DscplBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = DscplBlue,
        cursorColor = DscplBlue,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DscplBlueDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
                title = {
                    Column {
                        Text(machine.machineCode ?: "Machine", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            "${displayDate(logDate)} · ${machine.machineName.orEmpty()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FormSection(title = "Shift") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SHIFTS.forEach { (code, label) ->
                        FilterChip(
                            selected = shift == code,
                            onClick = { shift = code },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DscplBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                            ),
                        )
                    }
                }
            }

            FormSection(title = "Operator & meters") {
                BigField("Operator name", operator, { operator = it }, colors = fieldColors)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigField(
                        "Start meter",
                        startMeter,
                        { startMeter = it.filterNumeric() },
                        KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors,
                    )
                    BigField(
                        "End meter",
                        endMeter,
                        { endMeter = it.filterNumeric() },
                        KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors,
                    )
                }
            }

            FormSection(title = "HSD fuel") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigField(
                        "Fuel open (L)",
                        opening,
                        { opening = it.filterNumeric() },
                        KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors,
                    )
                    BigField(
                        "HSD issue (L)",
                        hsdIssue,
                        { hsdIssue = it.filterNumeric() },
                        KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors,
                    )
                }
                Spacer(Modifier.height(10.dp))
                BigField(
                    "HSD consumption (L)",
                    hsdConsumption,
                    { hsdConsumption = it.filterNumeric() },
                    KeyboardType.Decimal,
                    colors = fieldColors,
                )
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DscplBlue.copy(alpha = 0.08f))
                        .border(1.dp, DscplBlue.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Text("Estimated closing", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$closingPreview L", style = MaterialTheme.typography.headlineMedium, color = DscplBlueDark)
                }
            }

            FormSection(title = "Work") {
                BigField("Work done", work, { work = it }, singleLine = false, colors = fieldColors)
                Spacer(Modifier.height(10.dp))
                BigField("Remarks", remarks, { remarks = it }, singleLine = false, colors = fieldColors)
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

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
                        }.onSuccess { (_, msg) ->
                            loading = false
                            onDone(msg)
                        }.onFailure {
                            loading = false
                            error = it.message ?: "Save failed"
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DscplRed,
                    contentColor = Color.White,
                ),
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(22.dp))
                } else {
                    Text("Save to Log Book", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        content()
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
    colors: androidx.compose.material3.TextFieldColors,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = colors,
    )
}

private fun String.filterNumeric(): String =
    filterIndexed { index, c -> c.isDigit() || (c == '.' && !take(index).contains('.')) }

private fun Double.toPlain(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()
