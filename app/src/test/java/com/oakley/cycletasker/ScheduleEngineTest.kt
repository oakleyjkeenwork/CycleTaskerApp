package com.oakley.cycletasker

import com.oakley.cycletasker.data.CycleDay
import com.oakley.cycletasker.data.CycleRoutine
import com.oakley.cycletasker.data.CycleTask
import com.oakley.cycletasker.data.IndividualTask
import com.oakley.cycletasker.data.TaskOrderEntry
import com.oakley.cycletasker.domain.ScheduleEngine
import com.oakley.cycletasker.domain.TodayTask
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleEngineTest {
    @Test
    fun individualRepeatsFromOriginalStartDate() {
        val task = IndividualTask(
            id = "creatine",
            title = "Take creatine",
            tag = "HLTH",
            startDate = "2026-06-29",
            repeatEveryDays = 3
        )

        assertTrue(ScheduleEngine.individualDueOn(task, LocalDate.parse("2026-06-29")))
        assertFalse(ScheduleEngine.individualDueOn(task, LocalDate.parse("2026-06-30")))
        assertFalse(ScheduleEngine.individualDueOn(task, LocalDate.parse("2026-07-01")))
        assertTrue(ScheduleEngine.individualDueOn(task, LocalDate.parse("2026-07-02")))
    }

    @Test
    fun cycleRoutineWrapsAfterCustomLength() {
        val routine = CycleRoutine(
            id = "gym",
            name = "Gym",
            tag = "GYM",
            startDate = "2026-06-29",
            cycleLengthDays = 8,
            days = (1..8).map { day ->
                CycleDay(
                    dayNumber = day,
                    title = "Day $day",
                    tasks = listOf(CycleTask(id = "task-$day", title = "Task $day"))
                )
            }
        )

        assertEquals("Day 1", ScheduleEngine.cycleDayFor(routine, LocalDate.parse("2026-06-29"))?.title)
        assertEquals("Day 8", ScheduleEngine.cycleDayFor(routine, LocalDate.parse("2026-07-06"))?.title)
        assertEquals("Day 1", ScheduleEngine.cycleDayFor(routine, LocalDate.parse("2026-07-07"))?.title)
    }

    @Test
    fun completionPercentTreatsEveryTaskEqually() {
        val tasks = listOf(
            TodayTask("a", "key-a", "A", "Individual", "A", 0xFF64B5F6, completed = true, hasBodyWeight = false),
            TodayTask("b", "key-b", "B", "Individual", "B", 0xFF64B5F6, completed = true, hasBodyWeight = false),
            TodayTask("c", "key-c", "C", "Individual", "C", 0xFF64B5F6, completed = false, hasBodyWeight = false)
        )

        assertEquals(67, ScheduleEngine.completionPercent(tasks))
    }

    @Test
    fun taskOrderUsesStableOrderKeys() {
        val tasks = listOf(
            TodayTask("dated-a", "key-a", "A", "Individual", "A", 0xFF64B5F6, completed = false, hasBodyWeight = false),
            TodayTask("dated-b", "key-b", "B", "Individual", "B", 0xFF64B5F6, completed = false, hasBodyWeight = false)
        )

        val ordered = ScheduleEngine.sortTasksByOrder(
            tasks,
            listOf(
                TaskOrderEntry("key-b", 0),
                TaskOrderEntry("key-a", 1)
            )
        )

        assertEquals(listOf("key-b", "key-a"), ordered.map { it.orderKey })
    }
}
