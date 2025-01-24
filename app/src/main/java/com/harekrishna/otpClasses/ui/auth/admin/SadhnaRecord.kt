package com.harekrishna.otpClasses.ui.auth.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Data Model
data class StudentSadhna(
    val id: String,
    val name: String,
    val contact: String,
    val facilitator: String,
    val classLevel: String,
    val chantingRounds: Int,
    val weeklyReadingHours: Int,
    val bookReading: String,
    val weeklyHearingHours: Int,
    val isActive: Boolean,
    val followsPrinciples: Boolean,
    val regularityInClass: Int,
    val meetingInterval: String,
    val sewa: String,
    val seriousness: String,
    val attendsEveningClass: Boolean,
    val reasonForNotAttending: String?,
    val nightStayPerMonth: Int,
    val remarks: String,
    val lastMeetingDate: String
)

val dummyData = listOf(
    StudentSadhna("1", "Ravi Kumar", "1234567890", "Amit", "Level 2", 16, 5, "Bhagavad Gita", 4, true, true, 80, "Weekly", "Seva", "Serious", true, null, 2, "Good progress", "2024-02-10"),
    StudentSadhna("2", "Sita Devi", "9876543210", "Sunil", "Level 1", 8, 3, "Srimad Bhagavatam", 2, false, true, 60, "Bi-weekly", "Seva", "Moderate", false, "Work commitments", 0, "Needs improvement", "2024-02-05")
)

@Composable
fun StudentSadhnaListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val filteredStudents = dummyData.filter {
        it.name.contains(searchQuery.text, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(filteredStudents) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                        navController.navigate("detail/${student.id}")
                    },
                    elevation = CardDefaults.cardElevation(disabledElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = student.name, style = MaterialTheme.typography.headlineMedium)
                        Text(text = "Facilitator: ${student.facilitator}")
                        Text(text = "Class Level: ${student.classLevel}")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentSadhnaDetailScreen(navController: NavController, studentId: String?) {
    val student = dummyData.find { it.id == studentId }

    var remarks by remember { mutableStateOf(student?.remarks ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        student?.let {
            Text(text = "Name: ${it.name}", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Class Level: ${it.classLevel}")
            Text(text = "Chanting Rounds: ${it.chantingRounds}")
            Text(text = "Book Reading: ${it.bookReading}")
            Text(text = "Meeting Interval: ${it.meetingInterval}")
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* Save logic */ }) {
                Text("Save Changes")
            }
        } ?: Text("Student not found")
    }
}


