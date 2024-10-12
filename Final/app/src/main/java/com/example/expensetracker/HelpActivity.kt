package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelpScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(text = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Help", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Help content
        Text(
            text = "This app helps you track your expenses and manage your budget effectively.",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can add, view, and categorize your expenses. Use the Preferences screen to customize the app settings such as currency and notifications.",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Frequently Asked Questions:",
            fontSize = 20.sp,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "1. How do I add an expense?\n" +
                "You can add an expense by clicking on the 'Add Expense/Income' button on the main screen.\n")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "2. Can I edit an existing expense?\n" +
                "Currently, with the existing functionality to edit your expense or income you would simply use the minus symbol before the amount you want to change and select the proper category i.e Income, Food etc. Future updates would include the edit function better.\n")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "3. How do I change the currency?\n" +
                "Go to the Preferences screen by clicking on the 'Preferences' button on the main screen. From there, you can select your preferred currency.\n")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "4. How do I reset my data?\n" +
                "Go to the Preferences screen by clicking on the 'Preferences' button on the main screen. From there, you can select Reset All Data, then Yes.\n")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "For more questions, feel free to reach out to us!")
    }
}
