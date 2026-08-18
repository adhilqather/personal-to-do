package com.blackgold.todo.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blackgold.todo.R
import com.blackgold.todo.data.TodoEntity
import com.blackgold.todo.databinding.FragmentAddEditTodoBinding
import com.blackgold.todo.ui.viewmodel.TodoViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddEditTodoFragment : Fragment() {

    private var _binding: FragmentAddEditTodoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TodoViewModel by activityViewModels()
    private val args: AddEditTodoFragmentArgs by navArgs()

    private var selectedDueDate: Long? = null
    private var selectedDueTime: Long? = null
    private var selectedReminderTime: Long? = null
    private var selectedPriority: String = "medium"
    private var selectedCategory: String = "personal"
    private var isEditing = false
    private var existingTodo: TodoEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isEditing = args.todoId != 0L

        if (isEditing) {
            loadExistingTodo()
        }

        setupPriorityChips()
        setupCategoryChips()
        setupReminderSwitch()
        setupDateTimePickers()
        setupSaveButton()
    }

    private fun loadExistingTodo() {
        lifecycleScope.launch(Dispatchers.IO) {
            existingTodo = viewModel.getTodoById(args.todoId)
            requireActivity().runOnUiThread {
                existingTodo?.let { populateFields(it) }
            }
        }
    }

    private fun populateFields(todo: TodoEntity) {
        binding.editTitle.setText(todo.title)
        binding.editDescription.setText(todo.description)
        selectedPriority = todo.priority
        selectedCategory = todo.category

        todo.dueDate?.let {
            selectedDueDate = it
            binding.btnDueDate.text = formatDate(it)
        }

        todo.dueTime?.let {
            selectedDueTime = it
            binding.btnDueTime.text = formatTime(it)
        }

        binding.switchReminder.isChecked = todo.reminderEnabled
        binding.btnReminderTime.visibility = if (todo.reminderEnabled) View.VISIBLE else View.GONE
        todo.reminderTime?.let {
            selectedReminderTime = it
            binding.btnReminderTime.text = "REMINDER: ${formatTime(it)}"
        }

        updateChipSelection(binding.chipGroupPriority, selectedPriority)
        updateChipSelection(binding.chipGroupCategory, selectedCategory)
    }

    private fun setupPriorityChips() {
        val priorities = listOf("low" to R.string.low, "medium" to R.string.medium, "high" to R.string.high)
        priorities.forEach { (value, labelRes) ->
            val chip = Chip(requireContext()).apply {
                text = getString(labelRes)
                tag = value
                isCheckable = true
                setOnClickListener {
                    selectedPriority = value
                }
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(android.R.color.transparent)
                )
                setTextColor(requireContext().getColorStateList(R.color.chip_text_color))
                chipStrokeColor = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.border_gold)
                )
                chipStrokeWidth = 1.5f
                chipCornerRadius = 20f
                setChipMinHeight(40f)
            }
            binding.chipGroupPriority.addView(chip)
        }
        binding.chipGroupPriority.getChildAt(1).performClick()
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            "personal" to R.string.personal,
            "work" to R.string.work,
            "study" to R.string.study,
            "fitness" to R.string.fitness,
            "other" to R.string.other
        )
        categories.forEach { (value, labelRes) ->
            val chip = Chip(requireContext()).apply {
                text = getString(labelRes)
                tag = value
                isCheckable = true
                setOnClickListener {
                    selectedCategory = value
                }
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(android.R.color.transparent)
                )
                setTextColor(requireContext().getColorStateList(R.color.chip_text_color))
                chipStrokeColor = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.border_gold)
                )
                chipStrokeWidth = 1.5f
                chipCornerRadius = 20f
                setChipMinHeight(40f)
            }
            binding.chipGroupCategory.addView(chip)
        }
        binding.chipGroupCategory.getChildAt(0).performClick()
    }

    private fun updateChipSelection(chipGroup: com.google.android.material.chip.ChipGroup, selectedValue: String) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = (chip.tag as String) == selectedValue
        }
    }

    private fun setupReminderSwitch() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnReminderTime.visibility = if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                selectedReminderTime = null
            }
        }
    }

    private fun setupDateTimePickers() {
        binding.btnDueDate.setOnClickListener { showDatePicker { dateMillis ->
            selectedDueDate = dateMillis
            binding.btnDueDate.text = formatDate(dateMillis)
        } }

        binding.btnDueTime.setOnClickListener { showTimePicker { timeMillis ->
            selectedDueTime = timeMillis
            binding.btnDueTime.text = formatTime(timeMillis)
        } }

        binding.btnReminderTime.setOnClickListener { showTimePicker { timeMillis ->
            selectedReminderTime = timeMillis
            binding.btnReminderTime.text = "REMINDER: ${formatTime(timeMillis)}"
        } }
    }

    private fun showDatePicker(onDateSet: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDueDate ?: System.currentTimeMillis()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val c = Calendar.getInstance()
                c.set(year, month, day)
                onDateSet(c.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(onTimeSet: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedReminderTime ?: selectedDueTime ?: System.currentTimeMillis()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val c = Calendar.getInstance()
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                c.set(Calendar.SECOND, 0)
                c.set(Calendar.MILLISECOND, 0)
                onTimeSet(c.timeInMillis)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun formatDate(dateMillis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(dateMillis))
    }

    private fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveTodo()
        }
    }

    private fun saveTodo() {
        val title = binding.editTitle.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a task title", Toast.LENGTH_SHORT).show()
            return
        }

        val description = binding.editDescription.text.toString().trim()
        val reminderEnabled = binding.switchReminder.isChecked

        val todo = TodoEntity(
            id = if (isEditing) existingTodo!!.id else System.currentTimeMillis(),
            title = title,
            description = description,
            done = existingTodo?.done ?: false,
            priority = selectedPriority,
            category = selectedCategory,
            createdAt = existingTodo?.createdAt ?: System.currentTimeMillis(),
            dueDate = selectedDueDate,
            dueTime = selectedDueTime,
            reminderEnabled = reminderEnabled,
            reminderTime = if (reminderEnabled) selectedReminderTime else null,
            reminderId = if (reminderEnabled && selectedReminderTime != null) (System.currentTimeMillis() and 0xFFFFFFFF).toString() else null
        )

        if (isEditing) {
            viewModel.update(todo)
        } else {
            viewModel.insert(todo)
        }

        if (reminderEnabled && todo.reminderTime != null) {
            com.blackgold.todo.notification.ReminderScheduler.scheduleReminder(
                requireContext(),
                todo.id,
                todo.title,
                todo.reminderTime!!
            )
        }

        findNavController().popBackStack()
        Toast.makeText(
            requireContext(),
            if (isEditing) "Task updated" else "Task added",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
