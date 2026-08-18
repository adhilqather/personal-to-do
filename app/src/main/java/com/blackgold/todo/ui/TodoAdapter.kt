package com.blackgold.todo.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blackgold.todo.data.TodoEntity
import com.blackgold.todo.databinding.ItemTodoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoAdapter(
    private val onToggle: (TodoEntity) -> Unit,
    private val onEdit: (TodoEntity) -> Unit,
    private val onDelete: (TodoEntity) -> Unit
) : ListAdapter<TodoEntity, TodoAdapter.TodoViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TodoEntity>() {
            override fun areItemsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class TodoViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: TodoEntity) {
            binding.apply {
                textTitle.text = todo.title
                checkboxDone.isChecked = todo.done

                if (todo.done) {
                    textTitle.alpha = 0.4f
                    textTitle.paintFlags = textTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    textTitle.alpha = 1.0f
                    textTitle.paintFlags = textTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // Description
                if (todo.description.isNotEmpty()) {
                    textDescription.visibility = ViewGroup.VISIBLE
                    textDescription.text = todo.description
                } else {
                    textDescription.visibility = ViewGroup.GONE
                }

                // Due date
                if (todo.dueDate != null) {
                    textDue.visibility = ViewGroup.VISIBLE
                    textDue.text = formatDate(todo.dueDate)
                } else {
                    textDue.visibility = ViewGroup.GONE
                }

                // Priority
                if (todo.priority.isNotEmpty()) {
                    textPriority.visibility = ViewGroup.VISIBLE
                    textPriority.text = todo.priority.uppercase()
                    val color = when (todo.priority) {
                        "high" -> Color.parseColor("#FF3B30")
                        "medium" -> Color.parseColor("#FF9500")
                        else -> Color.parseColor("#34C759")
                    }
                    textPriority.setTextColor(color)
                } else {
                    textPriority.visibility = ViewGroup.GONE
                }

                // Reminder
                if (todo.reminderEnabled) {
                    textReminder.visibility = ViewGroup.VISIBLE
                    textReminder.text = "⏰"
                } else {
                    textReminder.visibility = ViewGroup.GONE
                }

                checkboxDone.setOnClickListener { onToggle(todo) }
                btnEdit.setOnClickListener { onEdit(todo) }
                btnDelete.setOnClickListener { onDelete(todo) }
                root.setOnClickListener { onEdit(todo) }
            }
        }

        private fun formatDate(dateMillis: Long): String {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return sdf.format(Date(dateMillis))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}