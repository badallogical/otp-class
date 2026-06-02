package com.harekrishna.otpClasses.ui.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.data.models.SangkirtanStudentPOJO
import com.harekrishna.otpClasses.data.sources.db.dao.SangkirtanStudentDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HarinaamDetailsViewModel @Inject constructor(
    private val dao: SangkirtanStudentDao
) : ViewModel() {
    private val _students = MutableStateFlow<List<SangkirtanStudentPOJO>>(emptyList())
    val students = _students.asStateFlow()

    fun loadStudents(date: String) {
        viewModelScope.launch {
            dao.getStudentsByDate(date).collect { _students.value = it }
        }
    }

    fun shareHarinaamReport(context: Context, date: String) {
        viewModelScope.launch {
            val studentList = students.value
            if (studentList.isEmpty()) return@launch

            val message = withContext(Dispatchers.Default) {
                StringBuilder().apply {
                    append("\uD83D\uDCDD *Harinaam Registration Report: $date*\n\n")
                    append("Total Strength : ${studentList.size}\n\n")
                    studentList.forEachIndexed { index, student ->
                        append("${index + 1}. 👤 *${student.name.trim()}* \n📞 ${student.phone.trim()}\n📍 Location: *${student.location.trim()}*\n\n")
                    }
                    append("Your Servant \uD83D\uDE4F \nISKCON Youth Forum")
                }.toString()
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
            }
            context.startActivity(intent)
        }
    }
}
