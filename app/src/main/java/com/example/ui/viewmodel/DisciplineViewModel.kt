package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Habit
import com.example.data.model.HabitCompletion
import com.example.data.model.JournalEntry
import com.example.data.model.Task
import com.example.data.repository.DisciplineRepository
import com.example.util.DateUtils
import com.example.util.AlarmScheduler
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DisciplineViewModel(private val repository: DisciplineRepository) : ViewModel() {

    enum class Screen {
        Home,
        Planner,
        Timer,
        Habits,
        Journal,
        Stats
    }

    val currentScreen = MutableStateFlow(Screen.Home)

    val habits: StateFlow<List<Habit>> = repository.habits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val journalEntries: StateFlow<List<JournalEntry>> = repository.journalEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habitCompletions: StateFlow<List<HabitCompletion>> = repository.habitCompletions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedJournalDate = MutableStateFlow(DateUtils.getCurrentDateString())
    val activeJournalText = MutableStateFlow("")
    val activeJournalImageUri = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.prepopulateStarterHabitsIfEmpty()
            // Load journal for today as initially selected date
            selectJournalDate(DateUtils.getCurrentDateString())
        }
    }

    fun selectScreen(screen: Screen) {
        currentScreen.value = screen
    }

    // --- Habits ---
    fun toggleHabit(habitId: Int, context: Context? = null) {
        viewModelScope.launch {
            repository.toggleHabit(
                habitId,
                DateUtils.getCurrentDateString(),
                DateUtils.getYesterdayDateString()
            )
            if (context != null) {
                val h = repository.getHabitById(habitId)
                if (h != null) {
                    val isCompletedToday = h.lastCompleted == DateUtils.getCurrentDateString()
                    if (isCompletedToday) {
                        try {
                            val notificationId = ("habit" + habitId).hashCode()
                            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            notificationManager.cancel(notificationId)
                        } catch (e: Exception) {
                            Log.e("DisciplineViewModel", "Failed to cancel habit notification", e)
                        }
                    }
                }
            }
        }
    }

    fun addHabit(name: String, reminderTime: String? = null, hasAlarm: Boolean = false, context: Context? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val habit = Habit(
                name = name.trim(),
                reminderTime = reminderTime,
                hasAlarm = hasAlarm
            )
            val newId = repository.insertHabit(habit)
            if (hasAlarm && reminderTime != null && context != null) {
                AlarmScheduler.scheduleAlarm(context, newId.toInt(), "habit", name.trim(), reminderTime)
            }
        }
    }

    fun deleteHabit(habit: Habit, context: Context? = null) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            if (context != null) {
                AlarmScheduler.cancelAlarm(context, habit.id, "habit")
            }
        }
    }

    // --- Tasks ---
    fun toggleTask(task: Task, context: Context? = null) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            if (context != null) {
                if (updated.isCompleted) {
                    AlarmScheduler.cancelAlarm(context, task.id, "task")
                    try {
                        val notificationId = ("task" + task.id).hashCode()
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.cancel(notificationId)
                    } catch (e: Exception) {
                        Log.e("DisciplineViewModel", "Failed to cancel task notification", e)
                    }
                } else if (updated.hasAlarm && !updated.reminderTime.isNullOrBlank()) {
                    AlarmScheduler.scheduleAlarm(context, task.id, "task", updated.title, updated.reminderTime)
                }
            }
        }
    }

    fun addTask(
        title: String,
        category: String,
        reminderTime: String?,
        hasAlarm: Boolean = false,
        isPinned: Boolean = false,
        dateString: String? = null,
        context: Context? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                category = category,
                reminderTime = reminderTime,
                hasAlarm = hasAlarm,
                isCompleted = false,
                isPinned = isPinned,
                dateString = dateString
            )
            val newId = repository.insertTask(task)
            if (hasAlarm && reminderTime != null && context != null) {
                AlarmScheduler.scheduleAlarm(context, newId.toInt(), "task", title.trim(), reminderTime)
            }
        }
    }

    fun deleteTask(task: Task, context: Context? = null) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (context != null) {
                AlarmScheduler.cancelAlarm(context, task.id, "task")
            }
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }

    // --- Journal ---
    fun selectJournalDate(dateString: String) {
        selectedJournalDate.value = dateString
        viewModelScope.launch {
            val entry = repository.getJournalEntryByDate(dateString)
            activeJournalText.value = entry?.content ?: ""
            activeJournalImageUri.value = entry?.imageUri
        }
    }

    fun saveJournalEntry(content: String, imageUri: String? = activeJournalImageUri.value) {
        val date = selectedJournalDate.value
        activeJournalText.value = content
        activeJournalImageUri.value = imageUri
        viewModelScope.launch {
            val existing = repository.getJournalEntryByDate(date)
            if (existing != null) {
                if (content.isBlank() && imageUri == null) {
                    repository.deleteJournalEntry(existing)
                } else {
                    repository.updateJournalEntry(existing.copy(content = content, imageUri = imageUri))
                }
            } else {
                if (content.isNotBlank() || imageUri != null) {
                    repository.insertJournalEntry(JournalEntry(date = date, content = content, imageUri = imageUri))
                }
            }
        }
    }

    fun updateActiveJournalImage(imageUri: String?) {
        activeJournalImageUri.value = imageUri
        saveJournalEntry(activeJournalText.value, imageUri)
    }

    // --- Backup & Restore ---
    fun backupData(): String {
        val backupObj = org.json.JSONObject()
        try {
            // habits
            val habitsArray = org.json.JSONArray()
            for (h in habits.value) {
                val o = org.json.JSONObject().apply {
                    put("id", h.id)
                    put("name", h.name)
                    put("streak", h.streak)
                    put("lastCompleted", h.lastCompleted)
                    put("reminderTime", h.reminderTime ?: org.json.JSONObject.NULL)
                    put("hasAlarm", h.hasAlarm)
                    put("createdAt", h.createdAt)
                }
                habitsArray.put(o)
            }
            backupObj.put("habits", habitsArray)

            // tasks
            val tasksArray = org.json.JSONArray()
            for (t in tasks.value) {
                val o = org.json.JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("category", t.category)
                    put("reminderTime", t.reminderTime ?: org.json.JSONObject.NULL)
                    put("hasAlarm", t.hasAlarm)
                    put("isCompleted", t.isCompleted)
                    put("isPinned", t.isPinned)
                    put("dateString", t.dateString ?: org.json.JSONObject.NULL)
                    put("createdAt", t.createdAt)
                }
                tasksArray.put(o)
            }
            backupObj.put("tasks", tasksArray)

            // completions
            val completionsArray = org.json.JSONArray()
            for (c in habitCompletions.value) {
                val o = org.json.JSONObject().apply {
                    put("habitId", c.habitId)
                    put("date", c.date)
                }
                completionsArray.put(o)
            }
            backupObj.put("habit_completions", completionsArray)

            // journal
            val journalArray = org.json.JSONArray()
            for (j in journalEntries.value) {
                val o = org.json.JSONObject().apply {
                    put("date", j.date)
                    put("content", j.content)
                    put("imageUri", j.imageUri ?: org.json.JSONObject.NULL)
                    put("createdAt", j.createdAt)
                }
                journalArray.put(o)
            }
            backupObj.put("journal_entries", journalArray)

        } catch (e: Exception) {
            Log.e("DisciplineViewModel", "Backup serialization error", e)
        }
        return backupObj.toString(4)
    }

    fun restoreData(jsonStr: String, context: Context, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (jsonStr.isBlank()) {
                    callback(false)
                    return@launch
                }
                val backupObj = org.json.JSONObject(jsonStr)
                repository.clearAllData()

                // Restore habits
                val habitsArray = backupObj.optJSONArray("habits")
                if (habitsArray != null) {
                    val list = mutableListOf<com.example.data.model.Habit>()
                    for (i in 0 until habitsArray.length()) {
                        val o = habitsArray.getJSONObject(i)
                        val hId = o.optInt("id", 0)
                        list.add(com.example.data.model.Habit(
                            id = if (hId > 0) hId else 0,
                            name = o.getString("name"),
                            streak = o.optInt("streak", 0),
                            lastCompleted = o.optString("lastCompleted", ""),
                            reminderTime = if (o.isNull("reminderTime")) null else o.getString("reminderTime"),
                            hasAlarm = o.optBoolean("hasAlarm", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        ))
                    }
                    repository.insertHabits(list)
                    
                    // Reschedule alarms
                    for (h in list) {
                        if (h.hasAlarm && !h.reminderTime.isNullOrBlank()) {
                            AlarmScheduler.scheduleAlarm(context, h.id, "habit", h.name, h.reminderTime)
                        }
                    }
                }

                // Restore tasks
                val tasksArray = backupObj.optJSONArray("tasks")
                if (tasksArray != null) {
                    val list = mutableListOf<com.example.data.model.Task>()
                    for (i in 0 until tasksArray.length()) {
                        val o = tasksArray.getJSONObject(i)
                        val tId = o.optInt("id", 0)
                        list.add(com.example.data.model.Task(
                            id = if (tId > 0) tId else 0,
                            title = o.getString("title"),
                            category = o.optString("category", "Work"),
                            reminderTime = if (o.isNull("reminderTime")) null else o.getString("reminderTime"),
                            hasAlarm = o.optBoolean("hasAlarm", false),
                            isCompleted = o.optBoolean("isCompleted", false),
                            isPinned = o.optBoolean("isPinned", false),
                            dateString = if (o.isNull("dateString")) null else o.getString("dateString"),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        ))
                    }
                    repository.insertTasks(list)

                    for (t in list) {
                        if (t.hasAlarm && !t.reminderTime.isNullOrBlank() && !t.isCompleted) {
                            AlarmScheduler.scheduleAlarm(context, t.id, "task", t.title, t.reminderTime)
                        }
                    }
                }

                // Restore completions
                val completionsArray = backupObj.optJSONArray("habit_completions")
                if (completionsArray != null) {
                    val list = mutableListOf<com.example.data.model.HabitCompletion>()
                    for (i in 0 until completionsArray.length()) {
                        val o = completionsArray.getJSONObject(i)
                        list.add(com.example.data.model.HabitCompletion(
                            habitId = o.getInt("habitId"),
                            date = o.getString("date")
                        ))
                    }
                    repository.insertHabitCompletions(list)
                }

                // Restore journal
                val journalArray = backupObj.optJSONArray("journal_entries")
                if (journalArray != null) {
                    val list = mutableListOf<com.example.data.model.JournalEntry>()
                    for (i in 0 until journalArray.length()) {
                        val o = journalArray.getJSONObject(i)
                        list.add(com.example.data.model.JournalEntry(
                            date = o.getString("date"),
                            content = o.optString("content", ""),
                            imageUri = if (o.isNull("imageUri")) null else o.getString("imageUri"),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        ))
                    }
                    repository.insertJournalEntries(list)
                }

                callback(true)
            } catch (e: Exception) {
                Log.e("DisciplineViewModel", "Restore operation failed", e)
                callback(false)
            }
        }
    }

    // --- Factory ---
    class Factory(private val repository: DisciplineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DisciplineViewModel::class.java)) {
                return DisciplineViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
