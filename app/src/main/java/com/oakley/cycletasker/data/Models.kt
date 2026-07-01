package com.oakley.cycletasker.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class IndividualTask(
    val id: String = newId(),
    val title: String = "",
    val tag: String = "",
    val tagColor: Long = 0xFF64B5F6,
    val startDate: String = "",
    val repeatEveryDays: Int = 1,
    val enabled: Boolean = true,
    val addOns: List<TaskAddOn> = emptyList()
)

@Serializable
data class CycleRoutine(
    val id: String = newId(),
    val name: String = "",
    val tag: String = "",
    val tagColor: Long = 0xFFA5D6A7,
    val startDate: String = "",
    val cycleLengthDays: Int = 1,
    val days: List<CycleDay> = emptyList(),
    val enabled: Boolean = true
)

@Serializable
data class CycleDay(
    val dayNumber: Int,
    val title: String = "",
    val tasks: List<CycleTask> = emptyList()
)

@Serializable
data class CycleTask(
    val id: String = newId(),
    val title: String = "",
    val addOns: List<TaskAddOn> = emptyList()
)

@Serializable
enum class TaskAddOn {
    BodyWeight
}

@Serializable
data class CompletionRecord(
    val date: String,
    val completedTaskIds: List<String> = emptyList(),
    val totalTaskIds: List<String> = emptyList(),
    val percentComplete: Int = 100,
    val bodyWeightEntries: List<BodyWeightEntry> = emptyList()
)

@Serializable
data class BodyWeightEntry(
    val taskId: String,
    val taskOrderKey: String,
    val taskTitle: String,
    val startWeight: Double? = null,
    val finishWeight: Double? = null
)

@Serializable
data class AppSettings(
    val schemaVersion: Int = 1,
    val notificationsEnabled: Boolean = true,
    val themePreset: String = "cycle_blue",
    val customTheme: CustomTheme = CustomTheme(),
    val taskOrder: List<TaskOrderEntry> = emptyList()
)

@Serializable
data class TaskOrderEntry(
    val taskOrderKey: String,
    val order: Int
)

@Serializable
data class CustomTheme(
    val primary: Long = 0xFF8AB4F8,
    val secondary: Long = 0xFF8AB4F8,
    val tertiary: Long = 0xFFE0C36A,
    val background: Long = 0xFF0B0C0E,
    val surface: Long = 0xFF15171A,
    val surfaceVariant: Long = 0xFF202329,
    val outline: Long = 0xFF3B4048
)

@Serializable
data class AppBackup(
    val individualTasks: List<IndividualTask> = emptyList(),
    val cycleRoutines: List<CycleRoutine> = emptyList(),
    val completionHistory: List<CompletionRecord> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val exportedAt: String = ""
)

data class AppState(
    val individualTasks: List<IndividualTask> = emptyList(),
    val cycleRoutines: List<CycleRoutine> = emptyList(),
    val completionHistory: List<CompletionRecord> = emptyList(),
    val settings: AppSettings = AppSettings()
)

fun newId(): String = UUID.randomUUID().toString()
