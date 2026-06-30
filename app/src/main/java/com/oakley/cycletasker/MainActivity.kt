package com.oakley.cycletasker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oakley.cycletasker.data.AppState
import com.oakley.cycletasker.data.AppSettings
import com.oakley.cycletasker.data.CustomTheme
import com.oakley.cycletasker.data.CycleDay
import com.oakley.cycletasker.data.CycleRoutine
import com.oakley.cycletasker.data.CycleTask
import com.oakley.cycletasker.data.CycleTaskerRepository
import com.oakley.cycletasker.data.IndividualTask
import com.oakley.cycletasker.data.newId
import com.oakley.cycletasker.domain.CalendarLabel
import com.oakley.cycletasker.domain.ScheduleEngine
import com.oakley.cycletasker.domain.TodayTask
import com.oakley.cycletasker.ui.theme.CycleTaskerTheme
import com.oakley.cycletasker.ui.theme.UiThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = CycleTaskerRepository(filesDir)
        setContent {
            CycleTaskerApp(repository)
        }
    }
}

private enum class Tab(val title: String, val iconText: String) {
    Today("Today", "T"),
    Calendar("Calendar", "C"),
    Individual("Individual", "I"),
    Cycles("Cycles", "R"),
    Settings("Settings", "S")
}

private enum class SettingsSection(val title: String) {
    General("General"),
    ThemeUi("Theme/UI")
}

private val TagPalette = listOf(
    0xFF64B5F6,
    0xFFA5D6A7,
    0xFFFFD166,
    0xFFFF8A80,
    0xFFB39DDB,
    0xFF80CBC4,
    0xFFFFAB91,
    0xFFE6EE9C
)

private data class ThemePreset(
    val key: String,
    val name: String,
    val colors: CustomTheme
)

private val ThemePresets = listOf(
    ThemePreset(
        key = "cycle_blue",
        name = "Cycle Blue",
        colors = CustomTheme(
            primary = 0xFF8AB4F8,
            secondary = 0xFF8AB4F8,
            tertiary = 0xFFE0C36A,
            background = 0xFF0B0C0E,
            surface = 0xFF15171A,
            surfaceVariant = 0xFF202329,
            outline = 0xFF3B4048
        )
    ),
    ThemePreset(
        key = "graphite",
        name = "Graphite",
        colors = CustomTheme(
            primary = 0xFFD1D5DB,
            secondary = 0xFF9CA3AF,
            tertiary = 0xFF93C5FD,
            background = 0xFF090A0B,
            surface = 0xFF151619,
            surfaceVariant = 0xFF24262B,
            outline = 0xFF444850
        )
    ),
    ThemePreset(
        key = "teal",
        name = "Teal",
        colors = CustomTheme(
            primary = 0xFF80CBC4,
            secondary = 0xFF8AB4F8,
            tertiary = 0xFFFFD166,
            background = 0xFF07100F,
            surface = 0xFF111B1A,
            surfaceVariant = 0xFF1D2B2A,
            outline = 0xFF35514E
        )
    ),
    ThemePreset(
        key = "amber",
        name = "Amber",
        colors = CustomTheme(
            primary = 0xFFFFD166,
            secondary = 0xFF8AB4F8,
            tertiary = 0xFFFFAB91,
            background = 0xFF0E0B06,
            surface = 0xFF18140D,
            surfaceVariant = 0xFF282115,
            outline = 0xFF4A3C25
        )
    ),
    ThemePreset(
        key = "rose",
        name = "Rose",
        colors = CustomTheme(
            primary = 0xFFFF8A80,
            secondary = 0xFF8AB4F8,
            tertiary = 0xFFB39DDB,
            background = 0xFF10090A,
            surface = 0xFF1B1113,
            surfaceVariant = 0xFF2B1C20,
            outline = 0xFF50353B
        )
    )
)

private val CustomColorChoices = listOf(
    0xFF8AB4F8,
    0xFF64B5F6,
    0xFF80CBC4,
    0xFFA5D6A7,
    0xFFFFD166,
    0xFFFFAB91,
    0xFFFF8A80,
    0xFFB39DDB,
    0xFFD1D5DB,
    0xFF0B0C0E,
    0xFF15171A,
    0xFF202329,
    0xFF3B4048
)

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK)
private val MonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)

@Composable
private fun CycleTaskerApp(repository: CycleTaskerRepository) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(Tab.Today) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var settingsMessage by remember { mutableStateOf<String?>(null) }
    var notificationPermissionGranted by remember {
        mutableStateOf(CycleTaskerNotifications.hasPermission(context))
    }
    var state by remember {
        val loaded = repository.load()
        val compacted = loaded.copy(
            completionHistory = ScheduleEngine.compactLockedHistory(
                loaded.completionHistory,
                LocalDate.now()
            )
        )
        if (compacted != loaded) {
            repository.saveState(compacted)
        }
        mutableStateOf(compacted)
    }

    val latestState by rememberUpdatedState(state)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
    }

    LaunchedEffect(state.settings.notificationsEnabled) {
        CycleTaskerNotifications.createChannel(context)
        if (
            state.settings.notificationsEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationPermissionGranted
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(state, notificationPermissionGranted, state.settings.notificationsEnabled) {
        if (state.settings.notificationsEnabled && notificationPermissionGranted) {
            CycleTaskerNotifications.startOrUpdate(context)
        } else {
            CycleTaskerNotifications.stop(context)
        }
    }

    fun saveState(newState: AppState) {
        val compacted = newState.copy(
            completionHistory = ScheduleEngine.compactLockedHistory(
                newState.completionHistory,
                LocalDate.now()
            )
        )
        repository.saveState(compacted)
        state = compacted
    }

    fun replaceCompletion(date: LocalDate, taskId: String, completed: Boolean) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        if (date != today && date != yesterday) return

        val currentTasks = ScheduleEngine.scheduledTasksForDate(
            state.individualTasks,
            state.cycleRoutines,
            state.completionHistory,
            date
        )
        val completedIds = currentTasks.filter { it.completed }.map { it.id }.toMutableSet()
        if (completed) {
            completedIds.add(taskId)
        } else {
            completedIds.remove(taskId)
        }

        val record = ScheduleEngine.completionRecordFor(
            date = date,
            tasks = currentTasks,
            completedIds = completedIds,
            compact = false
        )
        saveState(
            state.copy(
                completionHistory = state.completionHistory
                    .filterNot { it.date == date.toString() } + record
            )
        )
    }

    fun ensureLockedRecordsForMonth(month: YearMonth) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val existingDates = state.completionHistory.map { it.date }.toMutableSet()
        val records = state.completionHistory.toMutableList()
        var changed = false

        ScheduleEngine.monthDates(month)
            .filter { it.isBefore(yesterday) }
            .forEach { date ->
                if (existingDates.add(date.toString())) {
                    val tasks = ScheduleEngine.scheduledTasksForDate(
                        state.individualTasks,
                        state.cycleRoutines,
                        records,
                        date
                    )
                    records.add(
                        ScheduleEngine.completionRecordFor(
                            date = date,
                            tasks = tasks,
                            completedIds = emptySet(),
                            compact = true
                        )
                    )
                    changed = true
                }
            }

        if (changed) {
            saveState(state.copy(completionHistory = records))
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                writer.write(repository.exportBackup(latestState))
            }
            settingsMessage = "Backup exported"
        } catch (exception: Exception) {
            settingsMessage = "Export failed: ${exception.message ?: "unknown error"}"
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val contents = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            val imported = repository.importBackup(contents)
            val compacted = imported.copy(
                completionHistory = ScheduleEngine.compactLockedHistory(
                    imported.completionHistory,
                    LocalDate.now()
                )
            )
            repository.saveState(compacted)
            state = compacted
            settingsMessage = "Backup imported"
        } catch (exception: Exception) {
            settingsMessage = "Import failed: ${exception.message ?: "unknown error"}"
        }
    }

    CycleTaskerTheme(colors = state.settings.toThemeColors()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.title) },
                            icon = { Text(tab.iconText, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        Tab.Today -> TodayScreen(
                            state = state,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            onCheckedChange = ::replaceCompletion
                        )

                        Tab.Calendar -> CalendarScreen(
                            state = state,
                            onOpenEditableDate = { date ->
                                selectedDate = date
                                selectedTab = Tab.Today
                            },
                            onEnsureMonthRecords = ::ensureLockedRecordsForMonth
                        )

                        Tab.Individual -> IndividualScreen(
                            tasks = state.individualTasks,
                            onSaveTask = { task ->
                                val updated = state.individualTasks
                                    .filterNot { it.id == task.id } + task
                                saveState(state.copy(individualTasks = updated.sortedBy { it.title.lowercase() }))
                            },
                            onDeleteTask = { task ->
                                saveState(
                                    state.copy(
                                        individualTasks = state.individualTasks.filterNot { it.id == task.id }
                                    )
                                )
                            }
                        )

                        Tab.Cycles -> CyclesScreen(
                            routines = state.cycleRoutines,
                            onSaveRoutine = { routine ->
                                val updated = state.cycleRoutines
                                    .filterNot { it.id == routine.id } + routine
                                saveState(state.copy(cycleRoutines = updated.sortedBy { it.name.lowercase() }))
                            },
                            onDeleteRoutine = { routine ->
                                saveState(
                                    state.copy(
                                        cycleRoutines = state.cycleRoutines.filterNot { it.id == routine.id }
                                    )
                                )
                            }
                        )

                        Tab.Settings -> SettingsScreen(
                            settings = state.settings,
                            storageLocation = repository.storageDescription(),
                            message = settingsMessage,
                            notificationPermissionGranted = notificationPermissionGranted,
                            onSettingsChange = { settings ->
                                saveState(state.copy(settings = settings))
                            },
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationPermissionGranted = true
                                }
                            },
                            onExport = {
                                exportLauncher.launch("cycletasker-backup-${LocalDate.now()}.json")
                            },
                            onImport = {
                                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            onReset = {
                                state = repository.reset()
                                settingsMessage = "All data reset"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayScreen(
    state: AppState,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onCheckedChange: (LocalDate, String, Boolean) -> Unit
) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val activeDate = if (selectedDate == yesterday) yesterday else today
    val tasks = ScheduleEngine.scheduledTasksForDate(
        state.individualTasks,
        state.cycleRoutines,
        state.completionHistory,
        activeDate
    )
    val completed = tasks.count { it.completed }
    val total = tasks.size
    val percent = ScheduleEngine.completionPercent(tasks)
    val groupedTasks = tasks.groupBy { it.groupTitle }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeaderBlock(
                title = if (activeDate == today) "Today" else "Yesterday",
                subtitle = activeDate.format(DateFormatter)
            )
            DateToggle(
                selectedDate = activeDate,
                today = today,
                yesterday = yesterday,
                onDateSelected = onDateSelected
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (total == 0) "No tasks due" else "$percent% complete ($completed/$total)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        if (tasks.isEmpty()) {
            item {
                QuietCard {
                    Text(
                        "Nothing scheduled for this day.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            groupedTasks.forEach { (source, sourceTasks) ->
                item {
                    Text(
                        source.uppercase(Locale.UK),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(sourceTasks, key = { it.id }) { task ->
                    TodayTaskRow(
                        task = task,
                        editable = true,
                        onCheckedChange = { checked ->
                            onCheckedChange(activeDate, task.id, checked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateToggle(
    selectedDate: LocalDate,
    today: LocalDate,
    yesterday: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleButton(
            text = "Today",
            selected = selectedDate == today,
            modifier = Modifier.weight(1f),
            onClick = { onDateSelected(today) }
        )
        ToggleButton(
            text = "Yesterday",
            selected = selectedDate == yesterday,
            modifier = Modifier.weight(1f),
            onClick = { onDateSelected(yesterday) }
        )
    }
}

@Composable
private fun ToggleButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text)
    }
}

@Composable
private fun TodayTaskRow(
    task: TodayTask,
    editable: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    QuietCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge)
                TagPill(text = task.tag, color = task.tagColor)
            }
            Checkbox(
                checked = task.completed,
                enabled = editable,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun IndividualScreen(
    tasks: List<IndividualTask>,
    onSaveTask: (IndividualTask) -> Unit,
    onDeleteTask: (IndividualTask) -> Unit
) {
    var editingTask by remember { mutableStateOf<IndividualTask?>(null) }
    var creating by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<IndividualTask?>(null) }

    if (creating || editingTask != null) {
        IndividualTaskForm(
            initialTask = editingTask,
            onCancel = {
                creating = false
                editingTask = null
            },
            onSave = { task ->
                onSaveTask(task)
                creating = false
                editingTask = null
            }
        )
        return
    }

    DeleteDialog(
        title = "Delete task?",
        body = "Past days keep their completion percentage.",
        target = pendingDelete,
        onDismiss = { pendingDelete = null },
        onConfirm = {
            pendingDelete?.let(onDeleteTask)
            pendingDelete = null
        }
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeaderBlock(title = "Individual", subtitle = "Standalone repeating tasks")
            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Add individual task")
            }
        }

        if (tasks.isEmpty()) {
            item {
                QuietCard {
                    Text(
                        "No individual tasks yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                ManageableItemCard(
                    title = task.title,
                    subtitle = "Every ${task.repeatEveryDays} day${if (task.repeatEveryDays == 1) "" else "s"} from ${task.startDate}",
                    tag = task.tag,
                    tagColor = task.tagColor,
                    enabled = task.enabled,
                    onEnabledChange = { enabled -> onSaveTask(task.copy(enabled = enabled)) },
                    onEdit = { editingTask = task },
                    onDelete = { pendingDelete = task }
                )
            }
        }
    }
}

@Composable
private fun IndividualTaskForm(
    initialTask: IndividualTask?,
    onCancel: () -> Unit,
    onSave: (IndividualTask) -> Unit
) {
    val today = remember { LocalDate.now().toString() }
    var title by remember(initialTask?.id) { mutableStateOf(initialTask?.title.orEmpty()) }
    var tag by remember(initialTask?.id) { mutableStateOf(initialTask?.tag.orEmpty()) }
    var color by remember(initialTask?.id) { mutableStateOf(initialTask?.tagColor ?: TagPalette.first()) }
    var startDate by remember(initialTask?.id) { mutableStateOf(initialTask?.startDate ?: today) }
    var repeatEvery by remember(initialTask?.id) {
        mutableStateOf((initialTask?.repeatEveryDays ?: 1).toString())
    }
    var enabled by remember(initialTask?.id) { mutableStateOf(initialTask?.enabled ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    FormScaffold(
        title = if (initialTask == null) "New Task" else "Edit Task",
        onCancel = onCancel,
        onSave = {
            val parsedInterval = repeatEvery.toIntOrNull()?.coerceAtLeast(1)
            val parsedDate = parseIsoDate(startDate)
            when {
                title.isBlank() -> error = "Title is required"
                normalizeTag(tag).isBlank() -> error = "Tag is required"
                parsedDate == null -> error = "Start date must be yyyy-MM-dd"
                parsedInterval == null -> error = "Repeat interval must be a number"
                else -> {
                    error = null
                    onSave(
                        IndividualTask(
                            id = initialTask?.id ?: newId(),
                            title = title.trim(),
                            tag = normalizeTag(tag),
                            tagColor = color,
                            startDate = parsedDate.toString(),
                            repeatEveryDays = parsedInterval,
                            enabled = enabled
                        )
                    )
                }
            }
        }
    ) {
        TextFieldBlock(
            value = title,
            onValueChange = { title = it },
            label = "Title"
        )
        TextFieldBlock(
            value = tag,
            onValueChange = { tag = it },
            label = "Calendar tag"
        )
        ColorPicker(selected = color, onSelected = { color = it })
        DateFieldBlock(
            value = startDate,
            onValueChange = { startDate = it },
            label = "Start date"
        )
        TextFieldBlock(
            value = repeatEvery,
            onValueChange = { repeatEvery = it.filter(Char::isDigit) },
            label = "Repeat every days",
            keyboardType = KeyboardType.Number
        )
        EnabledRow(enabled = enabled, onEnabledChange = { enabled = it })
        error?.let { ErrorText(it) }
    }
}

@Composable
private fun CyclesScreen(
    routines: List<CycleRoutine>,
    onSaveRoutine: (CycleRoutine) -> Unit,
    onDeleteRoutine: (CycleRoutine) -> Unit
) {
    var editingRoutine by remember { mutableStateOf<CycleRoutine?>(null) }
    var creating by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CycleRoutine?>(null) }

    if (creating || editingRoutine != null) {
        CycleRoutineForm(
            initialRoutine = editingRoutine,
            onCancel = {
                creating = false
                editingRoutine = null
            },
            onSave = { routine ->
                onSaveRoutine(routine)
                creating = false
                editingRoutine = null
            }
        )
        return
    }

    DeleteDialog(
        title = "Delete routine?",
        body = "Past days keep their completion percentage.",
        target = pendingDelete,
        onDismiss = { pendingDelete = null },
        onConfirm = {
            pendingDelete?.let(onDeleteRoutine)
            pendingDelete = null
        }
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeaderBlock(title = "Cycles", subtitle = "Custom-length routines")
            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Add cycle routine")
            }
        }

        if (routines.isEmpty()) {
            item {
                QuietCard {
                    Text(
                        "No cycle routines yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(routines, key = { it.id }) { routine ->
                ManageableItemCard(
                    title = routine.name,
                    subtitle = "${routine.cycleLengthDays} day cycle from ${routine.startDate}",
                    tag = routine.tag,
                    tagColor = routine.tagColor,
                    enabled = routine.enabled,
                    onEnabledChange = { enabled -> onSaveRoutine(routine.copy(enabled = enabled)) },
                    onEdit = { editingRoutine = routine },
                    onDelete = { pendingDelete = routine }
                )
            }
        }
    }
}

@Composable
private fun CycleRoutineForm(
    initialRoutine: CycleRoutine?,
    onCancel: () -> Unit,
    onSave: (CycleRoutine) -> Unit
) {
    val today = remember { LocalDate.now().toString() }
    val defaultDays = remember {
        listOf(CycleDay(dayNumber = 1, title = "Day 1", tasks = emptyList()))
    }
    var name by remember(initialRoutine?.id) { mutableStateOf(initialRoutine?.name.orEmpty()) }
    var tag by remember(initialRoutine?.id) { mutableStateOf(initialRoutine?.tag.orEmpty()) }
    var color by remember(initialRoutine?.id) { mutableStateOf(initialRoutine?.tagColor ?: TagPalette[1]) }
    var startDate by remember(initialRoutine?.id) { mutableStateOf(initialRoutine?.startDate ?: today) }
    var enabled by remember(initialRoutine?.id) { mutableStateOf(initialRoutine?.enabled ?: true) }
    var days by remember(initialRoutine?.id) {
        mutableStateOf(initialRoutine?.days?.takeIf { it.isNotEmpty() } ?: defaultDays)
    }
    var error by remember { mutableStateOf<String?>(null) }

    fun updateDay(dayNumber: Int, transform: (CycleDay) -> CycleDay) {
        days = days.map { day ->
            if (day.dayNumber == dayNumber) transform(day) else day
        }
    }

    fun renumber(updated: List<CycleDay>): List<CycleDay> {
        return updated.mapIndexed { index, day ->
            day.copy(dayNumber = index + 1)
        }
    }

    FormScaffold(
        title = if (initialRoutine == null) "New Cycle" else "Edit Cycle",
        onCancel = onCancel,
        onSave = {
            val parsedDate = parseIsoDate(startDate)
            val cleanedDays = renumber(days).map { day ->
                day.copy(
                    title = day.title.ifBlank { "Day ${day.dayNumber}" },
                    tasks = day.tasks
                        .map { it.copy(title = it.title.trim()) }
                        .filter { it.title.isNotBlank() }
                )
            }

            when {
                name.isBlank() -> error = "Routine name is required"
                normalizeTag(tag).isBlank() -> error = "Tag is required"
                parsedDate == null -> error = "Start date must be yyyy-MM-dd"
                cleanedDays.isEmpty() -> error = "At least one cycle day is required"
                else -> {
                    error = null
                    onSave(
                        CycleRoutine(
                            id = initialRoutine?.id ?: newId(),
                            name = name.trim(),
                            tag = normalizeTag(tag),
                            tagColor = color,
                            startDate = parsedDate.toString(),
                            cycleLengthDays = cleanedDays.size,
                            days = cleanedDays,
                            enabled = enabled
                        )
                    )
                }
            }
        }
    ) {
        TextFieldBlock(
            value = name,
            onValueChange = { name = it },
            label = "Routine name"
        )
        TextFieldBlock(
            value = tag,
            onValueChange = { tag = it },
            label = "Calendar tag"
        )
        ColorPicker(selected = color, onSelected = { color = it })
        DateFieldBlock(
            value = startDate,
            onValueChange = { startDate = it },
            label = "Start date"
        )
        EnabledRow(enabled = enabled, onEnabledChange = { enabled = it })

        QuietCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cycle length", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${days.size} day${if (days.size == 1) "" else "s"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = days.size > 1,
                        onClick = { days = renumber(days.dropLast(1)) }
                    ) {
                        Text("-")
                    }
                    Button(
                        onClick = {
                            val next = days.size + 1
                            days = days + CycleDay(dayNumber = next, title = "Day $next")
                        }
                    ) {
                        Text("+")
                    }
                }
            }
        }

        days.forEach { day ->
            CycleDayEditor(
                day = day,
                onTitleChange = { title ->
                    updateDay(day.dayNumber) { it.copy(title = title) }
                },
                onTaskChange = { taskId, title ->
                    updateDay(day.dayNumber) { current ->
                        current.copy(
                            tasks = current.tasks.map { task ->
                                if (task.id == taskId) task.copy(title = title) else task
                            }
                        )
                    }
                },
                onAddTask = {
                    updateDay(day.dayNumber) { current ->
                        current.copy(tasks = current.tasks + CycleTask())
                    }
                },
                onDeleteTask = { taskId ->
                    updateDay(day.dayNumber) { current ->
                        current.copy(tasks = current.tasks.filterNot { it.id == taskId })
                    }
                }
            )
        }

        error?.let { ErrorText(it) }
    }
}

@Composable
private fun CycleDayEditor(
    day: CycleDay,
    onTitleChange: (String) -> Unit,
    onTaskChange: (String, String) -> Unit,
    onAddTask: () -> Unit,
    onDeleteTask: (String) -> Unit
) {
    QuietCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Day ${day.dayNumber}", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = day.title,
                onValueChange = onTitleChange,
                label = { Text("Day title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus(),
                singleLine = true
            )
            day.tasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = task.title,
                        onValueChange = { onTaskChange(task.id, it) },
                        label = { Text("Checklist item") },
                        modifier = Modifier
                            .weight(1f)
                            .bringIntoViewOnFocus(),
                        singleLine = true
                    )
                    TextButton(onClick = { onDeleteTask(task.id) }) {
                        Text("Delete")
                    }
                }
            }
            OutlinedButton(onClick = onAddTask, modifier = Modifier.fillMaxWidth()) {
                Text("Add checklist item")
            }
        }
    }
}

@Composable
private fun CalendarScreen(
    state: AppState,
    onOpenEditableDate: (LocalDate) -> Unit,
    onEnsureMonthRecords: (YearMonth) -> Unit
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedHistoryDate by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    LaunchedEffect(month, state.individualTasks, state.cycleRoutines) {
        onEnsureMonthRecords(month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderBlock(title = "Calendar", subtitle = month.format(MonthFormatter))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = { month = month.minusMonths(1) }) {
                Text("Previous")
            }
            Text(month.format(MonthFormatter), fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = { month = month.plusMonths(1) }) {
                Text("Next")
            }
        }
        WeekdayHeader()
        ScheduleEngine.monthDates(month).chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { date ->
                    val labels = ScheduleEngine.labelsForDate(
                        state.individualTasks,
                        state.cycleRoutines,
                        date
                    )
                    val record = state.completionHistory.firstOrNull { it.date == date.toString() }
                    CalendarDayCell(
                        date = date,
                        month = month,
                        today = today,
                        labels = labels,
                        percent = record?.percentComplete,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (date == today || date == yesterday) {
                                onOpenEditableDate(date)
                            } else {
                                selectedHistoryDate = date
                            }
                        }
                    )
                }
            }
        }
        selectedHistoryDate?.let { date ->
            DayHistoryCard(
                date = date,
                today = today,
                state = state
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Text(
                day,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    month: YearMonth,
    today: LocalDate,
    labels: List<CalendarLabel>,
    percent: Int?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val yesterday = today.minusDays(1)
    val isLocked = date.isBefore(yesterday)
    val inMonth = date.month == month.month
    val isToday = date == today
    val shownLabels = labels.take(2)
    val extraCount = labels.size - shownLabels.size

    Box(
        modifier = modifier
            .aspectRatio(0.78f)
            .alpha(if (inMonth) 1f else 0.42f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                date.dayOfMonth.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (isLocked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            shownLabels.forEach { label ->
                MiniLabel(label)
            }
            if (extraCount > 0) {
                Text(
                    "+$extraCount",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            if (isLocked) {
                Text(
                    "${percent ?: 100}%",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayHistoryCard(
    date: LocalDate,
    today: LocalDate,
    state: AppState
) {
    val yesterday = today.minusDays(1)
    val isLocked = date.isBefore(yesterday)
    val record = state.completionHistory.firstOrNull { it.date == date.toString() }
    val labels = ScheduleEngine.labelsForDate(state.individualTasks, state.cycleRoutines, date)

    QuietCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(date.format(DateFormatter), fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                labels.take(6).forEach { label -> TagPill(label.text, label.color) }
            }
            if (isLocked) {
                Text(
                    "${record?.percentComplete ?: 100}% recorded",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Locked day. Completion percentage is kept; task details are not kept.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (date.isAfter(today)) {
                Text(
                    "Scheduled ahead",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Open from Today to edit.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: AppSettings,
    storageLocation: String,
    message: String?,
    notificationPermissionGranted: Boolean,
    onSettingsChange: (AppSettings) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onReset: () -> Unit
) {
    var confirmingReset by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf(SettingsSection.General) }

    if (confirmingReset) {
        AlertDialog(
            onDismissRequest = { confirmingReset = false },
            title = { Text("Reset all data?") },
            text = { Text("This clears tasks, routines, settings, and completion history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingReset = false
                        onReset()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingReset = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderBlock(title = "Settings", subtitle = "Local JSON only")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSection.entries.forEach { section ->
                ToggleButton(
                    text = section.title,
                    selected = selectedSection == section,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedSection = section }
                )
            }
        }
        message?.let {
            QuietCard {
                Text(it, color = MaterialTheme.colorScheme.secondary)
            }
        }

        when (selectedSection) {
            SettingsSection.General -> GeneralSettingsSection(
                settings = settings,
                storageLocation = storageLocation,
                notificationPermissionGranted = notificationPermissionGranted,
                onSettingsChange = onSettingsChange,
                onRequestNotificationPermission = onRequestNotificationPermission,
                onExport = onExport,
                onImport = onImport,
                onResetClick = { confirmingReset = true }
            )

            SettingsSection.ThemeUi -> ThemeUiSettingsSection(
                settings = settings,
                onSettingsChange = onSettingsChange
            )
        }
    }
}

@Composable
private fun GeneralSettingsSection(
    settings: AppSettings,
    storageLocation: String,
    notificationPermissionGranted: Boolean,
    onSettingsChange: (AppSettings) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onResetClick: () -> Unit
) {
    QuietCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text("Today's completion notification", fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        !settings.notificationsEnabled -> "Off"
                        notificationPermissionGranted -> "On"
                        else -> "Permission needed"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = settings.notificationsEnabled,
                onCheckedChange = { enabled ->
                    onSettingsChange(settings.copy(notificationsEnabled = enabled))
                    if (enabled && !notificationPermissionGranted) {
                        onRequestNotificationPermission()
                    }
                }
            )
        }
        if (settings.notificationsEnabled && !notificationPermissionGranted) {
            OutlinedButton(
                onClick = onRequestNotificationPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Allow notifications")
            }
        }
    }

    Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
        Text("Export JSON backup")
    }
    OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
        Text("Import JSON backup")
    }
    OutlinedButton(
        onClick = onResetClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80))
    ) {
        Text("Reset all data")
    }
    QuietCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Storage", fontWeight = FontWeight.SemiBold)
            Text(
                storageLocation,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Backups contain individual_tasks, cycle_routines, completion_history, and settings.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    QuietCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("App", fontWeight = FontWeight.SemiBold)
            Text("CycleTasker 1.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Offline. No accounts. No cloud. No internet permission.")
        }
    }
}

@Composable
private fun ThemeUiSettingsSection(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit
) {
    QuietCard {
        Text("Presets", fontWeight = FontWeight.SemiBold)
        ThemePresets.forEach { preset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onSettingsChange(
                            settings.copy(
                                themePreset = preset.key,
                                customTheme = preset.colors
                            )
                        )
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorDot(preset.colors.primary)
                    ColorDot(preset.colors.surface)
                    Text(preset.name)
                }
                if (settings.themePreset == preset.key) {
                    Text("Selected", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        OutlinedButton(
            onClick = {
                onSettingsChange(settings.copy(themePreset = "custom"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Custom")
        }
    }

    if (settings.themePreset == "custom") {
        QuietCard {
            Text("Custom colours", fontWeight = FontWeight.SemiBold)
            ThemeColorPicker(
                label = "Primary",
                value = settings.customTheme.primary,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(primary = it)))
                }
            )
            ThemeColorPicker(
                label = "Secondary",
                value = settings.customTheme.secondary,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(secondary = it)))
                }
            )
            ThemeColorPicker(
                label = "Tertiary",
                value = settings.customTheme.tertiary,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(tertiary = it)))
                }
            )
            ThemeColorPicker(
                label = "Background",
                value = settings.customTheme.background,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(background = it)))
                }
            )
            ThemeColorPicker(
                label = "Surface",
                value = settings.customTheme.surface,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(surface = it)))
                }
            )
            ThemeColorPicker(
                label = "Surface alt",
                value = settings.customTheme.surfaceVariant,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(surfaceVariant = it)))
                }
            )
            ThemeColorPicker(
                label = "Outline",
                value = settings.customTheme.outline,
                onSelected = {
                    onSettingsChange(settings.copy(customTheme = settings.customTheme.copy(outline = it)))
                }
            )
        }
    }
}

@Composable
private fun ManageableItemCard(
    title: String,
    subtitle: String,
    tag: String,
    tagColor: Long,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    QuietCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TagPill(tag, tagColor)
                }
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun FormScaffold(
    title: String,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
        content()
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderBlock(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuietCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun TextFieldBlock(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewOnFocus(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFieldBlock(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .weight(1f)
                .bringIntoViewOnFocus(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedButton(onClick = { showPicker = true }) {
            Text("Calendar")
        }
    }

    if (showPicker) {
        val initialDate = parseIsoDate(value) ?: LocalDate.now()
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialDate.toPickerMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            onValueChange(selected.toLocalDateFromPicker().toString())
                        }
                        showPicker = false
                    }
                ) {
                    Text("Use date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun EnabledRow(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    QuietCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Enabled", fontWeight = FontWeight.SemiBold)
                Text(
                    if (enabled) "Appears when scheduled" else "Hidden from Today",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
    }
}

@Composable
private fun ColorPicker(selected: Long, onSelected: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tag colour", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TagPalette.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(toColor(color))
                        .border(
                            width = if (selected == color) 3.dp else 1.dp,
                            color = if (selected == color) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ThemeColorPicker(
    label: String,
    value: Long,
    onSelected: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            ColorDot(value)
        }
        CustomColorChoices.chunked(7).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(toColor(color))
                            .border(
                                width = if (value == color) 3.dp else 1.dp,
                                color = if (value == color) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { onSelected(color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorDot(color: Long) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(toColor(color))
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}

@Composable
private fun TagPill(text: String, color: Long) {
    if (text.isBlank()) return
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(toColor(color))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            color = Color(0xFF111111),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MiniLabel(label: CalendarLabel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(toColor(label.color))
            .padding(horizontal = 3.dp, vertical = 1.dp)
    ) {
        Text(
            label.text,
            color = Color(0xFF111111),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, color = Color(0xFFFF8A80), fontWeight = FontWeight.SemiBold)
}

@Composable
private fun <T> DeleteDialog(
    title: String,
    body: String,
    target: T?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (target == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun parseIsoDate(value: String): LocalDate? {
    return try {
        LocalDate.parse(value.trim())
    } catch (_: Exception) {
        null
    }
}

private fun normalizeTag(value: String): String {
    return value.trim().uppercase(Locale.UK).take(6)
}

private fun toColor(value: Long): Color {
    return Color(value)
}

private fun AppSettings.toThemeColors(): UiThemeColors {
    val source = if (themePreset == "custom") {
        customTheme
    } else {
        ThemePresets.firstOrNull { it.key == themePreset }?.colors ?: ThemePresets.first().colors
    }
    return UiThemeColors(
        primary = source.primary,
        secondary = source.secondary,
        tertiary = source.tertiary,
        background = source.background,
        surface = source.surface,
        surfaceVariant = source.surfaceVariant,
        outline = source.outline
    )
}

private fun LocalDate.toPickerMillis(): Long {
    return atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun Long.toLocalDateFromPicker(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Modifier.bringIntoViewOnFocus(): Modifier {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    return bringIntoViewRequester(requester)
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scope.launch {
                    delay(250)
                    requester.bringIntoView()
                }
            }
        }
}
