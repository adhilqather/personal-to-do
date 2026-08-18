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
import com.blackgold.todo.databinding.FragmentCompletedBinding
import com.blackgold.todo.ui.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TodoViewModel by activityViewModels()
    private val adapter by lazy { TodoAdapter(
        onToggle = { viewModel.toggleTodo(it) },
        onEdit = { openEditTodo(it) },
        onDelete = { viewModel.delete(it) }
    ) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeCompleted()
    }

    private fun setupRecyclerView() {
        binding.recyclerCompleted.adapter = adapter
        binding.recyclerCompleted.setHasFixedSize(true)
    }

    private fun observeCompleted() {
        viewModel.allTodos.observe(viewLifecycleOwner) { todos ->
            val completedTodos = todos.filter { it.done }
            adapter.submitList(completedTodos)
            updateEmptyState(completedTodos)
        }
    }

    private fun updateEmptyState(todos: List<com.blackgold.todo.data.TodoEntity>) {
        val isEmpty = todos.isEmpty()
        binding.layoutEmptyCompleted.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerCompleted.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openEditTodo(todo: com.blackgold.todo.data.TodoEntity) {
        findNavController().navigate(
            CompletedFragmentDirections.actionCompletedFragmentToAddEditTodoFragment(todo.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
