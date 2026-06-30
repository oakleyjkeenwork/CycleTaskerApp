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
    val enabled: Boolean = true
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
    val title: String = ""
)

@Serializable
data class CompletionRecord(
    val date: String,
    val completedTaskIds: List<String> = emptyList(),
    val totalTaskIds: List<String> = emptyList(),
    val percentComplete: Int = 100
)

@Serializable
data class AppSettings(
    val schemaVersion: Int = 1
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
