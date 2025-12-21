package com.example.swadebuilder.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ScreenTab(val title: String) {
    data object Character : ScreenTab("Character")
    data object Skills : ScreenTab("Skills")
    data object Edges : ScreenTab("Edges")
    data object Gear : ScreenTab("Gear")
    data object Notes : ScreenTab("Notes")
}

data class CharacterStats(
    val agility: Int = 6,
    val smarts: Int = 8,
    val spirit: Int = 6,
    val strength: Int = 6,
    val vigor: Int = 6,
    val pace: Int = 6,
    val parry: Int = 5,
    val toughness: Int = 5,
    val bennies: Int = 3
)

class CharacterViewModel : ViewModel() {

    private val _selectedTab = MutableStateFlow<ScreenTab>(ScreenTab.Character)
    val selectedTab: StateFlow<ScreenTab> = _selectedTab.asStateFlow()

    private val _stats = MutableStateFlow(CharacterStats())
    val stats: StateFlow<CharacterStats> = _stats.asStateFlow()

    fun selectTab(tab: ScreenTab) {
        _selectedTab.value = tab
    }

    // Dummy skills data for the list proof
    val skillsList = listOf(
        "Athletics" to "d6",
        "Common Knowledge" to "d4",
        "Notice" to "d8",
        "Persuasion" to "d6",
        "Stealth" to "d4",
        "Fighting" to "d6",
        "Shooting" to "d4"
    )
}
