package com.blackgold.todo

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.blackgold.todo.databinding.ActivityMainBinding
import com.blackgold.todo.notification.ReminderScheduler
import com.blackgold.todo.ui.AddEditTodoFragment
import com.blackgold.todo.ui.HomeFragment
import com.blackgold.todo.ui.RemindersFragment
import com.blackgold.todo.ui.SettingsFragment
import com.blackgold.todo.ui.TodosFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private var currentDestination: Int = R.id.homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.todosFragment,
                R.id.remindersFragment,
                R.id.completedFragment,
                R.id.settingsFragment
            )
        )

        binding.bottomNav.setupWithNavController(navController)

        binding.fabAdd.setOnClickListener {
            navigateToAddTodo()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination.id
            updateFabVisibility(destination.id)
        }

        lifecycleScope.launch {
            ReminderScheduler.rescheduleAllReminders(this@MainActivity)
        }
    }

    private fun updateFabVisibility(destinationId: Int) {
        val showFab = destinationId != R.id.settingsFragment && destinationId != R.id.addEditTodoFragment
        binding.fabAdd.visibility = if (showFab) View.VISIBLE else View.GONE

        binding.fabAdd.setImageResource(when (destinationId) {
            R.id.homeFragment -> R.drawable.ic_add
            R.id.todosFragment -> R.drawable.ic_add
            R.id.remindersFragment -> R.drawable.ic_add
            R.id.completedFragment -> R.drawable.ic_add
            else -> R.drawable.ic_add
        })
    }

    private fun navigateToAddTodo() {
        navController.navigate(R.id.addEditTodoFragment)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (currentDestination == R.id.addEditTodoFragment) {
            navController.popBackStack()
        } else if (currentDestination != R.id.homeFragment) {
            navController.navigate(R.id.homeFragment)
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            supportFragmentManager.fragments.forEach { fragment ->
                if (fragment is TodosFragment || fragment is RemindersFragment || fragment is SettingsFragment) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
