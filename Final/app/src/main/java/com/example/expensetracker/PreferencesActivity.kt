package com.example.expensetracker

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class PreferencesActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ExpenseTrackerPrefs", MODE_PRIVATE)

        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PreferencesScreen(
                        sharedPreferences = sharedPreferences,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun PreferencesScreen(sharedPreferences: SharedPreferences, onBack: () -> Unit) {
    val currencyOptions = listOf("USD ($)", "EUR (€)", "CAD (C$)")
    val defaultCurrency = sharedPreferences.getString("selected_currency", currencyOptions[0]) ?: currencyOptions[0]
    var selectedCurrency by remember { mutableStateOf(defaultCurrency) }
    var notificationsEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("notifications_enabled", false)) }
    var expanded by remember { mutableStateOf(false) }  // Dropdown visibility state
    var showResetDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Preferences", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Currency:")
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown for currency selection
        Box {
            Button(
                onClick = { expanded = !expanded },  // Toggle dropdown visibility
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedCurrency)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                currencyOptions.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            selectedCurrency = currency
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch for enabling/disabling notifications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable Notifications")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Save preferences on click
                with(sharedPreferences.edit()) {
                    putString("selected_currency", selectedCurrency)
                    putBoolean("notifications_enabled", notificationsEnabled)
                    apply()  // Apply changes
                }
                onBack()  // Return to Main Screen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Preferences")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset All Data", color = MaterialTheme.colorScheme.onError)
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            resetAllData(sharedPreferences, context)
                            showResetDialog = false
                            onBack()
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showResetDialog = false }) {
                        Text("No")
                    }
                },
                title = { Text("Are you sure you want to reset all of your data?") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Main")
        }
    }
}


private fun resetAllData(sharedPreferences: SharedPreferences, context: android.content.Context) {
    val editor = sharedPreferences.edit()
    editor.clear() // Clear all preferences and data
    editor.apply()

    // Reset spending categories to 0
    val spendingByCategory = mutableMapOf(
        "Food" to 0.0,
        "Transportation" to 0.0,
        "Entertainment" to 0.0,
        "Income" to 0.0
    )

    // Save the reset data back to SharedPreferences
    val expensesPrefs = context.getSharedPreferences("expenses_data", android.content.Context.MODE_PRIVATE)
    val expenseEditor = expensesPrefs.edit()
    for ((category, amount) in spendingByCategory) {
        expenseEditor.putFloat(category, amount.toFloat())
    }
    expenseEditor.apply()

    // Restart the app by launching MainActivity
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    context.startActivity(intent)

    // Close the app
    if (context is Activity) {
        context.finishAffinity() // Close all activities and exit the app
    }
}