package com.blackgold.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.blackgold.todo.R
import com.blackgold.todo.databinding.FragmentTodosBinding
import com.blackgold.todo.repository.TodoRepository
import com.blackgold.todo.ui.viewmodel.TodoViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class TodosFragment : Fragment() {

    private var _binding: FragmentTodosBinding? = null
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
        _binding = FragmentTodosBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        observeTodos()
    }

    private fun setupRecyclerView() {
        binding.recyclerTodos.adapter = adapter
        binding.recyclerTodos.setHasFixedSize(true)
    }

    private fun setupFilterChips() {
        val filters = listOf(
            TodoRepository.FilterType.ALL to getString(R.string.filter_all),
            TodoRepository.FilterType.ACTIVE to getString(R.string.filter_active),
            TodoRepository.FilterType.COMPLETED to getString(R.string.filter_completed),
            TodoRepository.FilterType.TODAY to getString(R.string.filter_today),
            TodoRepository.FilterType.UPCOMING to getString(R.string.filter_upcoming),
            TodoRepository.FilterType.OVERDUE to getString(R.string.filter_overdue)
        )

        filters.forEach { (type, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                setOnClickListener {
                    viewModel.setFilterType(type)
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
            binding.chipGroupFilter.addView(chip)
        }

        binding.chipGroupFilter.getChildAt(0).performClick()

        viewModel.filteredTodos.observe(viewLifecycleOwner) { }
    }

    private fun observeTodos() {
        viewModel.filteredTodos.observe(viewLifecycleOwner) { todos ->
            adapter.submitList(todos)
            updateEmptyState(todos)
        }
    }

    private fun updateEmptyState(todos: List<com.blackgold.todo.data.TodoEntity>) {
        val isEmpty = todos.isEmpty()
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerTodos.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun openEditTodo(todo: com.blackgold.todo.data.TodoEntity) {
        findNavController().navigate(
            TodosFragmentDirections.actionTodosFragmentToAddEditTodoFragment(todo.id)
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setQueryHint(getString(R.string.search_tasks))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
