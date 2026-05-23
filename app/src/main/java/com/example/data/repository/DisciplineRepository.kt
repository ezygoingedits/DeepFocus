package com.example.data.repository

import com.example.data.dao.HabitDao
import com.example.data.dao.JournalDao
import com.example.data.dao.TaskDao
import com.example.data.dao.HabitCompletionDao
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.JournalEntry
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DisciplineRepository(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao,
    private val journalDao: JournalDao,
    private val habitCompletionDao: HabitCompletionDao
) {
    val habits: Flow<List<Habit>> = habitDao.getAllHabits()
    val tasks: Flow<List<Task>> = taskDao.getAllTasks()
    val journalEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val habitCompletions: Flow<List<HabitCompletion>> = habitCompletionDao.getAllCompletions()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
    suspend fun getHabitById(id: Int) = habitDao.getHabitById(id)

    suspend fun prepopulateStarterHabitsIfEmpty() {
        val currentHabits = habits.first()
        if (currentHabits.isEmpty()) {
            val defaultHabits = listOf(
                Habit(name = "Deep Work Focus Session"),
                Habit(name = "Daily Reflection & Journal"),
                Habit(name = "Physical Exercise"),
                Habit(name = "Mindfulness / Meditation")
            )
            for (habit in defaultHabits) {
                habitDao.insertHabit(habit)
            }
        }
    }

    suspend fun toggleHabit(habitId: Int, currentDate: String, yesterdayDate: String) {
        val habit = habitDao.getHabitById(habitId) ?: return
        val isCompletedToday = habit.lastCompleted == currentDate

        val updatedHabit = if (isCompletedToday) {
            // Undo completion:
            habitCompletionDao.delete(habitId, currentDate)
            if (habit.streak <= 1) {
                habit.copy(lastCompleted = "", streak = 0)
            } else {
                habit.copy(lastCompleted = yesterdayDate, streak = habit.streak - 1)
            }
        } else {
            // Record completion:
            habitCompletionDao.insert(HabitCompletion(habitId, currentDate))
            // Verify if completed yesterday:
            val isYesterdayCompleted = habit.lastCompleted == yesterdayDate
            val newStreak = if (isYesterdayCompleted) {
                habit.streak + 1
            } else {
                1 // If they missed consecutive days, the streak resets to 1 as mandated!
            }
            habit.copy(lastCompleted = currentDate, streak = newStreak)
        }
        habitDao.updateHabit(updatedHabit)
    }

    // Tasks
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    // Journal
    suspend fun insertJournalEntry(entry: JournalEntry) = journalDao.insertEntry(entry)
    suspend fun updateJournalEntry(entry: JournalEntry) = journalDao.updateEntry(entry)
    suspend fun deleteJournalEntry(entry: JournalEntry) = journalDao.deleteEntry(entry)
    suspend fun getJournalEntryByDate(date: String) = journalDao.getEntryByDate(date)

    // Clear and restore
    suspend fun clearAllData() {
        habitDao.clearAllHabits()
        taskDao.clearAllTasks()
        journalDao.clearAllJournalEntries()
        habitCompletionDao.clearAllCompletions()
    }

    suspend fun insertHabits(habitsList: List<Habit>) {
        for (h in habitsList) {
            habitDao.insertHabit(h)
        }
    }

    suspend fun insertTasks(tasksList: List<Task>) {
        for (t in tasksList) {
            taskDao.insertTask(t)
        }
    }

    suspend fun insertJournalEntries(entriesList: List<JournalEntry>) {
        for (j in entriesList) {
            journalDao.insertEntry(j)
        }
    }

    suspend fun insertHabitCompletions(completionsList: List<HabitCompletion>) {
        for (c in completionsList) {
            habitCompletionDao.insert(c)
        }
    }
}
