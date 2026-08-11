package com.harekrishna.otpClasses.ui.registration2

import androidx.lifecycle.ViewModel
import com.harekrishna.otpClasses.domain.repository.RegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


// =========================================================================================
// NOTE ON COLORS
// -----------------------------------------------------------------------------------------
// Every color used below is pulled from MaterialTheme.colorScheme (defined in your Theme.kt
// with OrangeLightColorScheme / OrangeDarkColorScheme), so the screen automatically adapts
// to light & dark mode. Gradients are built from colorScheme.primary / colorScheme.tertiary /
// colorScheme.surface with alpha blending — no hardcoded hex colors are used in the UI.
//
// The only extra semantic colors this screen needs (rank badge gold/silver/bronze) are
// derived from colorScheme.primary / secondary / tertiary with alpha, so nothing new needs
// to be added to Color.kt. If you later want distinct brand gold/silver/bronze colors, add
// e.g.:
//   val RankGold  = Color(0xFFFFC107) / RankGoldDark  = Color(0xFFFFB300)
//   val RankSilver= Color(0xFFB0BEC5) / RankSilverDark = Color(0xFF90A4AE)
//   val RankBronze= Color(0xFFCD7F32) / RankBronzeDark = Color(0xFFA9662B)
// and expose them via a small CompositionLocal/extension on MaterialTheme, one set for each
// scheme in OrangeLightColorScheme / OrangeDarkColorScheme wiring.
// =========================================================================================

// -----------------------------------------------------------------------------------------
// MODELS
// -----------------------------------------------------------------------------------------

data class RankingEntry(
    val rank: Int,
    val name: String,
    val count: Int
)

data class RegistrationEntry(
    val dayLabel: String,   // e.g. "Sun, Jul 12, 2026"
    val count: Int,
    val verified: Boolean = true
)

data class RegistrationGroup(
    val sectionTitle: String, // "THIS WEEK" / "EARLIER"
    val entries: List<RegistrationEntry>
)

// -----------------------------------------------------------------------------------------
// UI STATE
// -----------------------------------------------------------------------------------------

data class RegistrationListUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val todayCount: Int = 0,
    val facilitatorInitials: List<String> = emptyList(),
    val extraFacilitatorCount: Int = 0,
    val rankings: List<RankingEntry> = emptyList(),
    val groups: List<RegistrationGroup> = emptyList(),
    val showLeaderboard: Boolean = false
)

// -----------------------------------------------------------------------------------------
// VIEWMODEL
// -----------------------------------------------------------------------------------------

@HiltViewModel
class RegistrationListViewModel @Inject constructor(
    registrationRepository: RegistrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationListUiState())
    val uiState: StateFlow<RegistrationListUiState> = _uiState.asStateFlow()

    init {
        loadDummyData()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onToggleLeaderboard() {
        _uiState.update { it.copy(showLeaderboard = !it.showLeaderboard) }
    }

    fun onRefresh() {
        // In a real app: re-fetch from repository. Here we just reload dummy data.
        loadDummyData()
    }

    private fun loadDummyData() {
        _uiState.update {
            it.copy(
                isLoading = false,
                todayCount = 14,
                facilitatorInitials = listOf("AS", "PS", "RK"),
                extraFacilitatorCount = 3,
                rankings = listOf(
                    RankingEntry(1, "Amit Sharma", 6),
                    RankingEntry(2, "Priya Singh", 4),
                    RankingEntry(3, "Rohit Kumar", 2),
                    RankingEntry(4, "Neha Gupta", 2)
                ),
                groups = listOf(
                    RegistrationGroup(
                        sectionTitle = "THIS WEEK",
                        entries = listOf(
                            RegistrationEntry("Sun, Jul 12, 2026", 14)
                        )
                    ),
                    RegistrationGroup(
                        sectionTitle = "EARLIER",
                        entries = listOf(
                            RegistrationEntry("Sun, Jun 28, 2026", 6),
                            RegistrationEntry("Sun, Jun 21, 2026", 5),
                            RegistrationEntry("Sun, Jun 14, 2026", 2),
                            RegistrationEntry("Sun, May 31, 2026", 8)
                        )
                    )
                )
            )
        }
    }
}
