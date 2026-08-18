package com.blackgold.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.blackgold.todo.R
import com.blackgold.todo.databinding.FragmentRemindersBinding
import com.blackgold.todo.repository.TodoRepository
import com.blackgold.todo.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TodoViewModel by activityViewModels()
    private val adapter by lazy { ReminderAdapter(
        onToggle = { viewModel.toggleTodo(it) },
        onEdit = { openEditTodo(it) },
        onDelete = { viewModel.delete(it) }
    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeReminders()
    }

    private fun setupRecyclerView() {
        binding.recyclerReminders.adapter = adapter
        binding.recyclerReminders.setHasFixedSize(true)
    }

    private fun observeReminders() {
        viewModel.filteredTodos.observe(viewLifecycleOwner) { todos ->
            val reminderTodos = todos.filter { it.reminderEnabled }
            adapter.submitList(reminderTodos)
            updateEmptyState(reminderTodos)
        }
    }

    private fun updateEmptyState(todos: List<com.blackgold.todo.data.TodoEntity>) {
        val isEmpty = todos.isEmpty()
        binding.layoutEmptyReminders.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerReminders.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openEditTodo(todo: com.blackgold.todo.data.TodoEntity) {
        findNavController().navigate(
            RemindersFragmentDirections.actionRemindersFragmentToAddEditTodoFragment(todo.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
