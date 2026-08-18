package com.blackgold.todo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.blackgold.todo.data.TodoDatabase
import com.blackgold.todo.data.TodoEntity
import com.blackgold.todo.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TodoDatabase.getInstance(application)
    private val repository = TodoRepository(database.todoDao())

    // Stats
    val totalCount: LiveData<Int> = repository.totalCount.asLiveData(Dispatchers.IO)
    val completedCount: LiveData<Int> = repository.completedCount.asLiveData(Dispatchers.IO)
    val activeCount: LiveData<Int> = repository.activeCount.asLiveData(Dispatchers.IO)

    // All todos
    val allTodos: LiveData<List<TodoEntity>> = repository.allTodos.asLiveData(Dispatchers.IO)

    // Filtered todos for lists
    val filteredTodos: LiveData<List<TodoEntity>> = repository.filteredTodos.asLiveData(Dispatchers.IO)

    // Search and filter
    fun setSearchQuery(query: String) {
        repository.setSearchQuery(query)
    }

    fun setFilterType(filterType: TodoRepository.FilterType) {
        repository.setFilterType(filterType)
    }

    // CRUD operations
    fun insert(todo: TodoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(todo)
        }
    }

    fun update(todo: TodoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(todo)
        }
    }

    fun delete(todo: TodoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(todo)
        }
    }

    fun deleteAllCompleted() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllCompleted()
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun toggleTodo(todo: TodoEntity) {
        val updated = todo.copy(done = !todo.done)
        update(updated)
    }

    // Suspend function for getting single todo - called from coroutine
    suspend fun getTodoById(id: Long): TodoEntity? {
        return database.todoDao().getTodoById(id)
    }
}
