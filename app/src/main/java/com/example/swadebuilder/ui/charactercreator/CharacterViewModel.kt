package com.example.swadebuilder.ui.charactercreator

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

    companion object {
        val allTabs = listOf(Character, Skills, Edges, Gear, Notes)
    }
}

class CharacterViewModel : ViewModel() {
    private val _selectedTab = MutableStateFlow<ScreenTab>(ScreenTab.Character)
    val selectedTab: StateFlow<ScreenTab> = _selectedTab.asStateFlow()

    fun onTabSelected(tab: ScreenTab) {
        _selectedTab.value = tab
    }
}
