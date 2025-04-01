package com.harekrishna.otpClasses.ui.registeration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.models.RegistrationStatus
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

data class RegistrationListUiState(
    val registrations: List<RegistrationStatus> = emptyList(),
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,

    // Add new selection-related states
    val selectedRegistrations: Set<String> = emptySet(), // Store selected dates numbers
    val isInSelectionMode: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
class RegistrationViewModel(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationListUiState())
    val uiState: StateFlow<RegistrationListUiState> = _uiState.asStateFlow()

    private val TAG = "RegistrationViewModel"

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository =
                    application.container.studentRepository // Assuming container contains the repository
                RegistrationViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }

    init {
        viewModelScope.launch {
            getRegistration()  // This will now manage the loading state internally

            Log.d(TAG, "ViewModel Init Registration called")
        }
    }

    // Get all the registrations from the local
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRegistration() {
        viewModelScope.launch {
            try {
                // Start loading
                _uiState.update { current ->
                    current.copy( isLoading = true)
                }

                // Fetch the registrations from the local <registration status>
                withContext(Dispatchers.IO){
                    studentRepository.getRegistrationList().collect { registrationList ->
                        _uiState.update { current ->
                            current.copy(registrations = registrationList)
                        }
                    }

                    //_registrations.value = registrationList  // Update state
                    Log.d(TAG, "Registrations loaded ${uiState.value.registrations.size}")
                }
            } finally {
                // End loading
                _uiState.update { current ->
                    current.copy( isLoading = false)
                }
            }
        }
    }


    fun syncUnsyncedRegistrations() {
        // Set syncing to true at the start
        _uiState.update { current ->
            current.copy(isSyncing = true)
        }

        viewModelScope.launch {
            try {
                // Fetch dates in IO dispatcher
                val dates = withContext(Dispatchers.IO) {
                    Log.d(TAG, "datas " + AttendanceDataStore.getDates.first())
                    AttendanceDataStore.getDates.first() // Use first() safely
                }

                Log.d(TAG, "to sync dates: $dates")

                // Sync local registrations in parallel
                val results = withContext(Dispatchers.IO) {
                    dates.map { date ->
                        async {
                            try {
                                studentRepository.syncLocalRegistrations(date) // Sync registrations for the date
                                Log.d(TAG, "Sync Completed for date: $date")
                                AttendanceDataStore.removeDate(date) // Remove synced date
                                true // Return success
                            } catch (e: Exception) {
                                Log.e(TAG, "Error syncing for date: $date", e)
                                false // Return failure
                            }
                        }
                    }.awaitAll() // Await all async tasks
                }

                // Check overall sync result
                if (results.all { it }) {
                    Log.d(TAG, "All dates synced successfully")
                } else {
                    Log.e(TAG, "Some dates failed to sync")
                }

                // Update registrations after syncing
                getRegistration()
            } catch (e: Exception) {
                // Handle any errors that occur during the sync process
                Log.e(TAG, "Error during sync process", e)
            } finally {
                // Ensure syncing is set to false even if an error occurs
                _uiState.update { current ->
                    current.copy(isSyncing = false)
                }
            }
        }
    }


    fun syncRegistrations() {
        // Set syncing to true at the start
        _uiState.update { current ->
            current.copy(isSyncing = true)
        }

        viewModelScope.launch {
            try {
                // Fetch dates in IO dispatcher
                val dates = withContext(Dispatchers.IO) {
                    Log.d(TAG, "datas " + AttendanceDataStore.getDates.first())
                    AttendanceDataStore.getDates.first() // Use first() safely
                }

                Log.d(TAG, "to sync dates: $dates")

                // Use parallel processing for syncing local registrations
                val results = withContext(Dispatchers.IO) {
                    dates.map { date ->
                        async {
                            try {
                                studentRepository.syncFullLocalRegistrations(date) // Sync registrations for the date
                                Log.d(TAG, "Sync Completed for date: $date")
                                AttendanceDataStore.removeDate(date) // Remove synced date
                                true // Return success
                            } catch (e: Exception) {
                                Log.e(TAG, "Error syncing for date: $date", e)
                                false // Return failure
                            }
                        }
                    }
                }.awaitAll() // Wait for all async tasks to complete

                // Check overall result
                if (results.all { it }) {
                    Log.d(TAG, "All dates synced successfully")
                } else {
                    Log.e(TAG, "Some dates failed to sync")
                }

                // Update registrations after all sync operations
                getRegistration()
            } catch (e: Exception) {
                // Handle any errors that occur during the sync process
                Log.e(TAG, "Error during sync process", e)
            } finally {
                // Ensure syncing is set to false even if an error occurs
                _uiState.update { current ->
                    current.copy(isSyncing = false)
                }
            }
        }
    }


    // TODO:
    fun deleteSelectedRegistrations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            try {
                coroutineScope {
                    val successfulDeletions = uiState.value.selectedRegistrations.map { date ->
                        async(Dispatchers.IO) {
                            try {
                                // Delete from the database
                                studentRepository.deleteRegistrationByDate(date)

                                date // Return phone if successful
                            } catch (e: Exception) {
                                Log.e("DeleteError", "Error deleting $date: ${e.message}")
                                null // Return null if failed
                            }
                        }
                    }.awaitAll().filterNotNull() // Filter out failed deletions

                    // Update local state by removing deleted reports
                    _uiState.value = _uiState.value.copy(
                        registrations = _uiState.value.registrations.filterNot { it.date in successfulDeletions },
                        selectedRegistrations = emptySet() // Clear selections after deletion,
                    )

                    Log.d("DeleteStatus", "Successfully deleted: $successfulDeletions")
                }
            } catch (e: Exception) {
                Log.e("DeleteError", "Error in deleteSelectedStudents: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isDeleting = false, showDeleteDialog = false, isInSelectionMode = false)
            }
        }
    }


    // Toggle selection mode
    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isInSelectionMode = !_uiState.value.isInSelectionMode,
            selectedRegistrations = emptySet() // Clear selections when toggling off
        )
    }

    // Toggle selection for a specific attendee
    fun toggleRegistrationSelection(date: String) {
        val currentSelections = _uiState.value.selectedRegistrations.toMutableSet()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (currentSelections.contains(date)) {
                    currentSelections.remove(date)
                    AttendanceDataStore.removeDate(date)
                } else {
                    currentSelections.add(date)
                    AttendanceDataStore.addDate(date)
                }
                Log.d(TAG,"Dates "+AttendanceDataStore.getDates.first())
            }


            _uiState.value = _uiState.value.copy(
                selectedRegistrations = currentSelections,
                isInSelectionMode = currentSelections.isNotEmpty()
            )
        }
    }

    // Clear all selections
    fun clearSelections() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState.value.selectedRegistrations.forEach { date ->
                AttendanceDataStore.removeDate(date)
            }
        }

        _uiState.value = _uiState.value.copy(
            selectedRegistrations = emptySet(),
            isInSelectionMode = false
        )
    }

    fun onClickDelete(){
        _uiState.value = _uiState.value.copy( showDeleteDialog = true );
    }

    fun onDismissDelete(){
        _uiState.value = _uiState.value.copy( showDeleteDialog = false )
    }

    }

