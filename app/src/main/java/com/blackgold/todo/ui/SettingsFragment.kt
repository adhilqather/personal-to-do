package com.blackgold.todo.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.blackgold.todo.R
import com.blackgold.todo.databinding.FragmentSettingsBinding
import com.blackgold.todo.ui.viewmodel.TodoViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TodoViewModel by activityViewModels()

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { exportTodos(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importTodos(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        binding.switchAmoled.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "AMOLED mode is always enabled in Black Gold theme", Toast.LENGTH_SHORT).show()
        }

        binding.textExport.setOnClickListener {
            exportLauncher.launch("todo_backup.json")
        }

        binding.textImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        binding.textClearCompleted.setOnClickListener {
            showConfirmDialog(
                getString(R.string.confirm_clear_completed),
                { viewModel.deleteAllCompleted() }
            )
        }

        binding.textDeleteAll.setOnClickListener {
            showConfirmDialog(
                getString(R.string.confirm_delete_all),
                { viewModel.deleteAll() }
            )
        }
    }

    private fun exportTodos(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val todos = viewModel.allTodos.value ?: emptyList()
                val json = Gson().toJson(todos)
                val outputStream = requireContext().contentResolver.openOutputStream(uri)
                outputStream?.write(json.toByteArray())
                outputStream?.close()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun importTodos(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val json = inputStream?.readBytes()?.decodeToString()
                inputStream?.close()

                if (json != null) {
                    val type: Type = object : TypeToken<List<com.blackgold.todo.data.TodoEntity>>() {}.type
                    val todos = Gson().fromJson<List<com.blackgold.todo.data.TodoEntity>>(json, type)
                    if (todos != null) {
                        todos.forEach { todo ->
                            viewModel.insert(todo)
                        }
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), R.string.import_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showConfirmDialog(message: String, onConfirm: () -> Unit) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> onConfirm() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
