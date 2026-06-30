package com.oakley.cycletasker.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

class CycleTaskerRepository(private val filesDir: File) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val individualFile = "individual_tasks.json"
    private val cyclesFile = "cycle_routines.json"
    private val historyFile = "completion_history.json"
    private val settingsFile = "settings.json"

    fun load(): AppState {
        return AppState(
            individualTasks = readValue(individualFile, emptyList<IndividualTask>()),
            cycleRoutines = readValue(cyclesFile, emptyList<CycleRoutine>()),
            completionHistory = readValue(historyFile, emptyList<CompletionRecord>()),
            settings = readValue(settingsFile, AppSettings())
        )
    }

    fun saveState(state: AppState) {
        writeValue(individualFile, state.individualTasks)
        writeValue(cyclesFile, state.cycleRoutines)
        writeValue(historyFile, state.completionHistory)
        writeValue(settingsFile, state.settings)
    }

    fun exportBackup(state: AppState): String {
        val backup = AppBackup(
            individualTasks = state.individualTasks,
            cycleRoutines = state.cycleRoutines,
            completionHistory = state.completionHistory,
            settings = state.settings,
            exportedAt = Instant.now().toString()
        )
        return json.encodeToString(backup)
    }

    fun importBackup(contents: String): AppState {
        val backup = json.decodeFromString<AppBackup>(contents)
        val imported = AppState(
            individualTasks = backup.individualTasks,
            cycleRoutines = backup.cycleRoutines,
            completionHistory = backup.completionHistory,
            settings = backup.settings
        )
        saveState(imported)
        return imported
    }

    fun reset(): AppState {
        val empty = AppState()
        saveState(empty)
        return empty
    }

    fun storageDescription(): String {
        return filesDir.absolutePath
    }

    private inline fun <reified T> readValue(fileName: String, defaultValue: T): T {
        val file = File(filesDir, fileName)
        if (!file.exists()) return defaultValue
        return try {
            json.decodeFromString<T>(file.readText())
        } catch (_: Exception) {
            val corruptCopy = File(filesDir, "$fileName.corrupt.${System.currentTimeMillis()}")
            file.copyTo(corruptCopy, overwrite = true)
            defaultValue
        }
    }

    private inline fun <reified T> writeValue(fileName: String, value: T) {
        val file = File(filesDir, fileName)
        val temp = File(filesDir, "$fileName.tmp")
        temp.writeText(json.encodeToString(value))
        if (file.exists()) {
            file.delete()
        }
        temp.renameTo(file)
    }
}
