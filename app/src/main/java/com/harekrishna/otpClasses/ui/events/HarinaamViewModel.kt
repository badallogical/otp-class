package com.harekrishna.otpClasses.ui.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.data.models.SangkirtanRegistrationStatus
import com.harekrishna.otpClasses.data.sources.repos.AttendancePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.SangkirtanStudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HarinaamUiState(
    val registrations: List<SangkirtanRegistrationStatus> = emptyList(),
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,
    val selectedRegistrations: Set<String> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val showDropdown: Boolean = false
)

@HiltViewModel
class HarinaamViewModel @Inject constructor(
    private val repository: SangkirtanStudentRepository,
    private val attendancePreferencesRepository: AttendancePreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HarinaamUiState())
    val uiState: StateFlow<HarinaamUiState> = _uiState.asStateFlow()

    private val TAG = "HarinaamViewModel"

    init {
        viewModelScope.launch {
            getRegistration()
            Log.d(TAG, "ViewModel Init Registration called")
        }
    }

    fun getRegistration() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                withContext(Dispatchers.IO) {
                    repository.getRegistrationList().collect { registrationList ->
                        _uiState.update { it.copy(registrations = registrationList) }
                    }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun syncRegistrations() {
        _uiState.update { it.copy(isSyncing = true) }
        viewModelScope.launch {
            try {
                val dates = withContext(Dispatchers.IO) { attendancePreferencesRepository.dates.first() }
                withContext(Dispatchers.IO) {
                    dates.map { date ->
                        async {
                            try {
                                repository.syncFullLocalRegistrations(date)
                                attendancePreferencesRepository.removeDate(date)
                                true
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }.awaitAll()
                }
                getRegistration()
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun syncUnsyncedRegistrations() {
        _uiState.update { it.copy(isSyncing = true) }
        viewModelScope.launch {
            try {
                val dates = withContext(Dispatchers.IO) { attendancePreferencesRepository.dates.first() }
                withContext(Dispatchers.IO) {
                    dates.map { date ->
                        async {
                            try {
                                repository.syncLocalRegistrations(date)
                                attendancePreferencesRepository.removeDate(date)
                                true
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }.awaitAll()
                }
                getRegistration()
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun deleteSelectedRegistrations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                coroutineScope {
                    val successfulDeletions = uiState.value.selectedRegistrations.map { date ->
                        async(Dispatchers.IO) {
                            try {
                                repository.deleteRegistrationByDate(date)
                                date
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }.awaitAll().filterNotNull()

                    _uiState.update {
                        it.copy(
                            registrations = it.registrations.filterNot { reg -> reg.date in successfulDeletions },
                            selectedRegistrations = emptySet(),
                            isDeleting = false,
                            showDeleteDialog = false,
                            isInSelectionMode = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun toggleSelectionMode() {
        _uiState.update { it.copy(isInSelectionMode = !it.isInSelectionMode, selectedRegistrations = emptySet()) }
    }

    fun toggleRegistrationSelection(date: String) {
        val currentSelections = _uiState.value.selectedRegistrations.toMutableSet()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (currentSelections.contains(date)) {
                    currentSelections.remove(date)
                    attendancePreferencesRepository.removeDate(date)
                } else {
                    currentSelections.add(date)
                    attendancePreferencesRepository.addDate(date)
                }
            }
            _uiState.update {
                it.copy(
                    selectedRegistrations = currentSelections,
                    isInSelectionMode = currentSelections.isNotEmpty()
                )
            }
        }
    }

    fun clearSelections() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.selectedRegistrations.forEach { attendancePreferencesRepository.removeDate(it) }
        }
        _uiState.update { it.copy(selectedRegistrations = emptySet(), isInSelectionMode = false) }
    }

    fun onClickDelete() = _uiState.update { it.copy(showDeleteDialog = true) }
    fun onDismissDelete() = _uiState.update { it.copy(showDeleteDialog = false) }


}
