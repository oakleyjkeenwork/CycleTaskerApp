package com.oakley.cycletasker.domain

import com.oakley.cycletasker.data.CompletionRecord
import com.oakley.cycletasker.data.CycleDay
import com.oakley.cycletasker.data.CycleRoutine
import com.oakley.cycletasker.data.CycleTask
import com.oakley.cycletasker.data.IndividualTask
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class TodayTask(
    val id: String,
    val title: String,
    val groupTitle: String,
    val tag: String,
    val tagColor: Long,
    val completed: Boolean
)

data class CalendarLabel(
    val text: String,
    val color: Long
)

object ScheduleEngine {
    fun scheduledTasksForDate(
        individualTasks: List<IndividualTask>,
        cycleRoutines: List<CycleRoutine>,
        completionHistory: List<CompletionRecord>,
        date: LocalDate
    ): List<TodayTask> {
        val completedIds = completionHistory
            .firstOrNull { it.date == date.toString() }
            ?.completedTaskIds
            ?.toSet()
            .orEmpty()

        val individualDue = individualTasks
            .filter { individualDueOn(it, date) }
            .map { task ->
                val taskId = individualTaskInstanceId(task, date)
                TodayTask(
                    id = taskId,
                    title = task.title,
                    groupTitle = "Individual",
                    tag = task.tag,
                    tagColor = task.tagColor,
                    completed = taskId in completedIds
                )
            }

        val cycleDue = cycleRoutines.flatMap { routine ->
            val cycleDay = cycleDayFor(routine, date) ?: return@flatMap emptyList()
            cycleDay.tasks.map { task ->
                val taskId = cycleTaskInstanceId(routine, cycleDay, task, date)
                TodayTask(
                    id = taskId,
                    title = task.title,
                    groupTitle = "Cycle: ${routine.name} / ${cycleDay.title}",
                    tag = routine.tag,
                    tagColor = routine.tagColor,
                    completed = taskId in completedIds
                )
            }
        }

        return individualDue + cycleDue
    }

    fun individualDueOn(task: IndividualTask, date: LocalDate): Boolean {
        if (!task.enabled) return false
        val startDate = task.startDate.toLocalDateOrNull() ?: return false
        if (date.isBefore(startDate)) return false
        val interval = task.repeatEveryDays.coerceAtLeast(1)
        return ChronoUnit.DAYS.between(startDate, date) % interval == 0L
    }

    fun cycleDayFor(routine: CycleRoutine, date: LocalDate): CycleDay? {
        if (!routine.enabled) return null
        val startDate = routine.startDate.toLocalDateOrNull() ?: return null
        if (date.isBefore(startDate)) return null
        val length = routine.cycleLengthDays.coerceAtLeast(1)
        val dayNumber = (ChronoUnit.DAYS.between(startDate, date) % length).toInt() + 1
        return routine.days.firstOrNull { it.dayNumber == dayNumber }
    }

    fun labelsForDate(
        individualTasks: List<IndividualTask>,
        cycleRoutines: List<CycleRoutine>,
        date: LocalDate
    ): List<CalendarLabel> {
        val individualLabels = individualTasks
            .filter { individualDueOn(it, date) }
            .map { CalendarLabel(it.tag, it.tagColor) }

        val cycleLabels = cycleRoutines
            .filter { cycleDayFor(it, date) != null }
            .map { CalendarLabel(it.tag, it.tagColor) }

        return (individualLabels + cycleLabels)
            .filter { it.text.isNotBlank() }
            .distinctBy { "${it.text}:${it.color}" }
    }

    fun completionPercent(tasks: List<TodayTask>, completedIds: Set<String> = tasks.filter { it.completed }.map { it.id }.toSet()): Int {
        if (tasks.isEmpty()) return 100
        return ((completedIds.size.toFloat() / tasks.size.toFloat()) * 100f).roundToInt()
    }

    fun completionRecordFor(
        date: LocalDate,
        tasks: List<TodayTask>,
        completedIds: Set<String>,
        compact: Boolean
    ): CompletionRecord {
        val dueIds = tasks.map { it.id }
        val normalizedCompleted = completedIds.intersect(dueIds.toSet()).toList()
        val percent = completionPercent(tasks, normalizedCompleted.toSet())
        return CompletionRecord(
            date = date.toString(),
            completedTaskIds = if (compact) emptyList() else normalizedCompleted,
            totalTaskIds = if (compact) emptyList() else dueIds,
            percentComplete = percent
        )
    }

    fun monthDates(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1)
        val leadingDays = first.dayOfWeek.value - 1
        val gridStart = first.minusDays(leadingDays.toLong())
        return List(42) { index -> gridStart.plusDays(index.toLong()) }
    }

    fun compactLockedHistory(
        records: List<CompletionRecord>,
        today: LocalDate
    ): List<CompletionRecord> {
        val yesterday = today.minusDays(1)
        return records.map { record ->
            val recordDate = record.date.toLocalDateOrNull()
            if (recordDate != null && recordDate.isBefore(yesterday)) {
                record.copy(completedTaskIds = emptyList(), totalTaskIds = emptyList())
            } else {
                record
            }
        }
    }

    fun String.toLocalDateOrNull(): LocalDate? {
        return try {
            LocalDate.parse(this)
        } catch (_: Exception) {
            null
        }
    }

    private fun individualTaskInstanceId(task: IndividualTask, date: LocalDate): String {
        return "individual:${task.id}:$date"
    }

    private fun cycleTaskInstanceId(
        routine: CycleRoutine,
        day: CycleDay,
        task: CycleTask,
        date: LocalDate
    ): String {
        return "cycle:${routine.id}:day:${day.dayNumber}:task:${task.id}:$date"
    }
}
