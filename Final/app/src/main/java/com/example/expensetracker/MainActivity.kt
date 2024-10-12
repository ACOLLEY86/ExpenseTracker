package com.example.expensetracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val spendingByCategory = mutableStateMapOf(
        "Food" to 0.0,
        "Transportation" to 0.0,
        "Entertainment" to 0.0,
        "Income" to 0.0
    )

    // Declare ActivityResultLauncher for ExpenseDetailActivity
    private lateinit var expenseResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("ExpenseTrackerPrefs", MODE_PRIVATE)

        // Register ActivityResultLauncher for ExpenseDetailActivity
        expenseResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val category = data.getStringExtra("category") ?: "Misc"
                    val amount = data.getDoubleExtra("amount", 0.0)

                    // Update spendingByCategory
                    spendingByCategory[category] = (spendingByCategory[category] ?: 0.0) + amount

                    // Save updated expenses
                    saveExpensesToSharedPreferences()

                    // Recompose the MainScreen with updated values
                    val selectedCurrency = sharedPreferences.getString("selected_currency", "USD ($)") ?: "USD ($)"
                    val currencySymbol = Regex("\\((.*?)\\)").find(selectedCurrency)?.groupValues?.get(1) ?: "$"

                    setContent {
                        ExpenseTrackerTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                MainScreen(
                                    spendingByCategory = spendingByCategory,
                                    currencySymbol = currencySymbol,
                                    onNavigateToDetail = { launchExpenseDetailActivity() },
                                    onNavigateToPreferences = { startActivity(Intent(this, PreferencesActivity::class.java)) },
                                    onNavigateToHelp = { startActivity(Intent(this, HelpActivity::class.java)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Load expenses and currency symbol on app start
        loadExpensesFromSharedPreferences()
        val selectedCurrency = sharedPreferences.getString("selected_currency", "USD ($)") ?: "USD ($)"
        val currencySymbol = Regex("\\((.*?)\\)").find(selectedCurrency)?.groupValues?.get(1) ?: "$"

        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        spendingByCategory = spendingByCategory,
                        currencySymbol = currencySymbol,
                        onNavigateToDetail = { launchExpenseDetailActivity() },
                        onNavigateToPreferences = { startActivity(Intent(this, PreferencesActivity::class.java)) },
                        onNavigateToHelp = { startActivity(Intent(this, HelpActivity::class.java)) }
                    )
                }
            }
        }
    }

    // Function to launch ExpenseDetailActivity
    private fun launchExpenseDetailActivity() {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        expenseResultLauncher.launch(intent)
    }

    private fun saveExpensesToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("expenses_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for ((category, amount) in spendingByCategory) {
            editor.putFloat(category, amount.toFloat())
        }
        editor.apply()
    }

    private fun loadExpensesFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences("expenses_data", MODE_PRIVATE)
        for (category in spendingByCategory.keys) {
            val amount = sharedPreferences.getFloat(category, 0.0f).toDouble()
            spendingByCategory[category] = amount
        }
    }
}

@Composable
fun MainScreen(
    spendingByCategory: MutableMap<String, Double>,
    currencySymbol: String,
    onNavigateToDetail: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    // Income is handled separately and not included in expenses or the pie chart
    val totalIncome = spendingByCategory.getOrDefault("Income", 0.0)
    val totalExpenses = spendingByCategory.filterKeys { it != "Income" }.values.sum()
    val currentBalance = totalIncome - totalExpenses

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        SummarySection(currentBalance, totalIncome, totalExpenses, currencySymbol)
        SpendingByCategory(spendingByCategory, currencySymbol)
        ChartsSection(spendingByCategory)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToDetail,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense/Income")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNavigateToPreferences,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Preferences")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onNavigateToHelp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Help")
        }
    }
}

@Composable
fun SummarySection(currentBalance: Double, totalIncome: Double, totalExpenses: Double, currencySymbol: String) {
    Column {
        Text(text = "Current Balance: $currencySymbol$currentBalance")
        Text(text = "Total Income: $currencySymbol$totalIncome")
        Text(text = "Total Expenses: $currencySymbol$totalExpenses")
    }
}

@Composable
fun SpendingByCategory(spendingByCategory: Map<String, Double>, currencySymbol: String) {
    LazyColumn {
        items(spendingByCategory.filterKeys { it != "Income" }.toList()) { (category, amount) ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(text = category)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "$currencySymbol$amount")
            }
        }
    }
}

@Composable
fun ChartsSection(spendingByCategory: Map<String, Double>) {
    // Only showing expenses in the pie chart, filtering out Income
    val pieEntries = spendingByCategory.filterKeys { it != "Income" }.map { PieEntry(it.value.toFloat(), it.key) }
    val pieDataSet = PieDataSet(pieEntries, "Spending by Category")
    pieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

    val pieData = PieData(pieDataSet)

    AndroidView(factory = { context ->
        PieChart(context).apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(android.graphics.Color.BLACK)
            animateY(1400)
        }
    }, modifier = Modifier
        .fillMaxWidth()
        .height(450.dp))
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ExpenseTrackerTheme {
        MainScreen(
            spendingByCategory = mutableMapOf("Food" to 150.0, "Transportation" to 100.0, "Entertainment" to 80.0),
            currencySymbol = "$",
            onNavigateToDetail = {},
            onNavigateToPreferences = {},
            onNavigateToHelp = {}
        )
    }
}