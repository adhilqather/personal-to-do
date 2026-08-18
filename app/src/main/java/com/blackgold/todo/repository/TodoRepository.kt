package com.blackgold.todo.repository

import com.blackgold.todo.data.TodoDao
import com.blackgold.todo.data.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TodoRepository(private val todoDao: TodoDao) {

    // All todos stream
    val allTodos: Flow<List<TodoEntity>> = todoDao.getAllTodos()

    // Active todos stream
    val activeTodos: Flow<List<TodoEntity>> = todoDao.getActiveTodos()

    // Completed todos stream
    val completedTodos: Flow<List<TodoEntity>> = todoDao.getCompletedTodos()

    // Todos with reminders stream
    val reminderTodos: Flow<List<TodoEntity>> = todoDao.getTodosWithReminders()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: Flow<String> = _searchQuery.asStateFlow()

    // Filter type
    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: Flow<FilterType> = _filterType.asStateFlow()

    // Filtered todos based on search and filter
    val filteredTodos: Flow<List<TodoEntity>> = combine(allTodos, searchQuery, filterType) { todos, query, filter ->
        todos.filter { todo ->
            val matchesQuery = query.isBlank() || todo.title.contains(query, ignoreCase = true) ||
                    todo.description.contains(query, ignoreCase = true) ||
                    todo.category.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterType.ALL -> true
                FilterType.ACTIVE -> !todo.done
                FilterType.COMPLETED -> todo.done
                FilterType.TODAY -> isToday(todo)
                FilterType.UPCOMING -> isUpcoming(todo)
                FilterType.OVERDUE -> isOverdue(todo)
            }

            matchesQuery && matchesFilter
        }
    }

    // Stats
    val totalCount: Flow<Int> = allTodos.map { it.size }
    val completedCount: Flow<Int> = completedTodos.map { it.size }
    val activeCount: Flow<Int> = activeTodos.map { it.size }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: FilterType) {
        _filterType.value = type
    }

    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)

    suspend fun update(todo: TodoEntity) = todoDao.update(todo)

    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)

    suspend fun deleteAllCompleted() = todoDao.deleteAllCompleted()

    suspend fun deleteAll() = todoDao.deleteAll()

    private fun isToday(todo: TodoEntity): Boolean {
        return todo.dueDate?.let { dueDate ->
            val today = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = dueDate
            val todayCal = java.util.Calendar.getInstance()
            todayCal.timeInMillis = today
            calendar.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)
        } ?: false
    }

    private fun isUpcoming(todo: TodoEntity): Boolean {
        return todo.dueDate?.let { dueDate ->
            val today = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = dueDate
            val todayCal = java.util.Calendar.getInstance()
            todayCal.timeInMillis = today
            calendar.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
            calendar.get(java.util.Calendar.DAY_OF_YEAR) > todayCal.get(java.util.Calendar.DAY_OF_YEAR)
        } ?: false
    }

    private fun isOverdue(todo: TodoEntity): Boolean {
        return todo.dueDate?.let { dueDate ->
            dueDate < System.currentTimeMillis() && !todo.done
        } ?: false
    }

    enum class FilterType {
        ALL, ACTIVE, COMPLETED, TODAY, UPCOMING, OVERDUE
    }
}
