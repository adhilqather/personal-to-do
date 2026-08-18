package com.blackgold.todo.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blackgold.todo.data.TodoEntity
import com.blackgold.todo.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(
    private val onToggle: (TodoEntity) -> Unit,
    private val onEdit: (TodoEntity) -> Unit,
    private val onDelete: (TodoEntity) -> Unit
) : ListAdapter<TodoEntity, ReminderAdapter.ReminderViewHolder>(DIFF_CALLBACK) {

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

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: TodoEntity) {
            val context = binding.root.context
            binding.apply {
                textTitle.text = todo.title

                if (todo.description.isNotEmpty()) {
                    textDescription.visibility = ViewGroup.VISIBLE
                    textDescription.text = todo.description
                } else {
                    textDescription.visibility = ViewGroup.GONE
                }

                if (todo.reminderTime != null) {
                    textReminderTime.visibility = ViewGroup.VISIBLE
                    textReminderTime.text = formatReminderTime(todo.reminderTime!!)
                } else {
                    textReminderTime.visibility = ViewGroup.GONE
                }

                if (todo.dueDate != null) {
                    textDueDate.visibility = ViewGroup.VISIBLE
                    textDueDate.text = formatDueDate(todo.dueDate!!)
                    val isOverdue = todo.dueDate!! < System.currentTimeMillis() && !todo.done
                    textDueDate.setTextColor(
                        if (isOverdue) ContextCompat.getColor(context, com.blackgold.todo.R.color.priority_high)
                        else ContextCompat.getColor(context, com.blackgold.todo.R.color.text_secondary)
                    )
                } else {
                    textDueDate.visibility = ViewGroup.GONE
                }

                if (todo.done) {
                    textStatus.text = "COMPLETED"
                    textStatus.setTextColor(ContextCompat.getColor(context, com.blackgold.todo.R.color.completed_green))
                    iconStatus.setImageResource(com.blackgold.todo.R.drawable.ic_check_circle)
                    iconStatus.setColorFilter(ContextCompat.getColor(context, com.blackgold.todo.R.color.completed_green))
                } else {
                    textStatus.text = "PENDING"
                    textStatus.setTextColor(ContextCompat.getColor(context, com.blackgold.todo.R.color.gold_primary))
                    iconStatus.setImageResource(com.blackgold.todo.R.drawable.ic_check_circle)
                    iconStatus.setColorFilter(ContextCompat.getColor(context, com.blackgold.todo.R.color.gold_primary))
                }

                checkboxDone.isChecked = todo.done
                checkboxDone.setOnClickListener { onToggle(todo) }
                btnEdit.setOnClickListener { onEdit(todo) }
                btnDelete.setOnClickListener { onDelete(todo) }
                root.setOnClickListener { onEdit(todo) }
            }
        }

        private fun formatReminderTime(timeMillis: Long): String {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return "⏰ ${sdf.format(Date(timeMillis))}"
        }

        private fun formatDueDate(dateMillis: Long): String {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val today = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = dateMillis
            val todayCal = java.util.Calendar.getInstance()
            todayCal.timeInMillis = today

            return when {
                cal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                cal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR) -> "TODAY"
                cal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                cal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR) + 1 -> "TOMORROW"
                dateMillis < today && !cal.timeInMillis.equals(today) -> "OVERDUE"
                else -> sdf.format(Date(dateMillis))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
