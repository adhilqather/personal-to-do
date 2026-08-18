package com.blackgold.todo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE done = 0 ORDER BY createdAt DESC")
    fun getActiveTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE done = 1 ORDER BY createdAt DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE reminderEnabled = 1 AND reminderTime IS NOT NULL AND done = 0 ORDER BY reminderTime ASC")
    fun getUpcomingReminders(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE reminderEnabled = 1 AND reminderTime IS NOT NULL AND done = 0 ORDER BY reminderTime ASC")
    suspend fun getActiveReminders(): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE reminderEnabled = 1 AND reminderTime IS NOT NULL ORDER BY reminderTime ASC")
    fun getTodosWithReminders(): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE done = 1")
    suspend fun deleteAllCompleted()

    @Query("DELETE FROM todos")
    suspend fun deleteAll()

    @Query("UPDATE todos SET done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("SELECT * FROM todos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTodos(query: String): Flow<List<TodoEntity>>
}
