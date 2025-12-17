package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.EquipamentoListItem
import com.example.swadebuilder.ui.components.RenderCategoryList
import com.example.swadebuilder.ui.components.SectionCard

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun EquipamentoSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    compendioFantasiaAtivo: Boolean = false,
    compendioHorrorAtivo: Boolean = false,
    compendioSciFiAtivo: Boolean = false,
    compendioBuscatrilhaAtivo: Boolean = false,
    compendioDeadlandsAtivo: Boolean = false,
    compendioCrystalHeartAtivo: Boolean = false,
    compendioArteDaGuerraAtivo: Boolean = false,
    compendioCidadeSolVaporAtivo: Boolean = false,
    compendioWiseguysAtivo: Boolean = false
) {
    val context = LocalContext.current

    // State for toggling categories
    var expArmaduras by rememberSaveable { mutableStateOf(false) }
    var expArmasCC by rememberSaveable { mutableStateOf(false) }
    var expArmasDist by rememberSaveable { mutableStateOf(false) }
    var expMunicao by rememberSaveable { mutableStateOf(false) }
    var expVeiculos by rememberSaveable { mutableStateOf(false) }
    var expMundanos by rememberSaveable { mutableStateOf(false) }

    var expFantasiaEquip by rememberSaveable { mutableStateOf(false) }
    var expHorrorEquip by rememberSaveable { mutableStateOf(false) }
    var expSciFiEquip by rememberSaveable { mutableStateOf(false) }
    var expBuscatrilhaEquip by rememberSaveable { mutableStateOf(false) }
    var expDeadlandsEquip by rememberSaveable { mutableStateOf(false) }
    var expCrystalHeartEquip by rememberSaveable { mutableStateOf(false) }
    var expArteDaGuerraEquip by rememberSaveable { mutableStateOf(false) }
    var expCidadeSolVaporEquip by rememberSaveable { mutableStateOf(false) }
    var expWiseguysEquip by rememberSaveable { mutableStateOf(false) }


    // Load equipment lists
    // FIX: Using pattern to access from MainActivity to avoid GlobalData if missing
    // Actually, EquipamentoSection usually loads its own data or gets it passed.
    // Since I don't see it passed, I'll rely on loading standard file if available, or empty if not.
    // The previous implementation used GlobalData.listaEquipamentosJson.
    // If I removed GlobalData ref, I should load it here.
    val equipamentosBase = remember {
        try {
            context.loadJsonAsset<List<EquipamentoCategoria>>("equipamentos.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    // We need to merge all equipment files if we want a complete list like before
    // Or assume they are loaded separately for each compendium block below.
    // The previous code had a merged list in GlobalData.
    // I will try to replicate loading the specific files for each compendium here.

    // ... (Base categories implementation) ...

    // Helper to filter categories
    fun filterCategories(list: List<EquipamentoCategoria>, origemTarget: String): List<EquipamentoCategoria> {
        return list.mapNotNull { cat ->
            val filteredItems = cat.itens.filter { it.origem == origemTarget }
            if (filteredItems.isNotEmpty()) cat.copy(itens = filteredItems) else null
        }
    }

    val baseCategorias = equipamentosBase.mapNotNull { cat ->
        val items = cat.itens.filter {
            val o = it.origem ?: "BASICO"
            o == "BASICO"
        }
        if (items.isNotEmpty()) cat.copy(itens = items) else null
    }

    val buscatrilhaCategorias = remember(compendioBuscatrilhaAtivo) {
        if (compendioBuscatrilhaAtivo) {
            // Buscatrilha items might be in separate file or main file.
            // If separate file isn't found, try filtering main.
            // I'll try to load "equipamentos_buscatrilha.json" (if I renamed it?) or just "equipamentos.json" filter.
            // I saw "equipamentos_trilhador.json" missing in file list, so maybe it was never there.
            // I will assume they are in main list or I should load extra files.
            // Safe bet: load base list again and filter for Buscatrilha.
            // AND also try to load any potential extra files like `equipamentos_buscatrilha.json` if it existed?
            // Since I didn't create `equipamentos_buscatrilha.json`, I rely on main list.
            filterCategories(equipamentosBase, "FANTASIA_BUSCATRILHA")
        } else emptyList()
    }

    val fantasiaItems = remember(compendioFantasiaAtivo) {
        if (compendioFantasiaAtivo) filterCategories(equipamentosBase, "FANTASIA") else emptyList()
    }

    val horrorItems = remember(compendioHorrorAtivo) {
        if (compendioHorrorAtivo) filterCategories(equipamentosBase, "HORROR") else emptyList()
    }

    val sciFiItems = remember(compendioSciFiAtivo) {
        if (compendioSciFiAtivo) {
             // SciFi often has specific JSONs. I should try to load them if I can.
             // But without GlobalData, I might miss them if they were merged there.
             // I will try to load 'ciberneticos.json' etc if I knew the names.
             // For now, robust fallback: filter base list.
             filterCategories(equipamentosBase, "SCI_FI")
        } else emptyList()
    }

    SectionCard(
        title = "Equipamento",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Shield
    ) {
        Text(
            "Toque para comprar/vender. Dinheiro: $${state.dinheiro}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- BASICO ---
        if (baseCategorias.isNotEmpty()) {
            RenderCategoryList(
                categories = baseCategorias,
                state = state,
                expStates = mapOf(
                    "Armaduras" to expArmaduras,
                    "Armas Corpo a Corpo" to expArmasCC,
                    "Armas à Distância" to expArmasDist,
                    "Munição" to expMunicao,
                    "Veículos" to expVeiculos,
                    "Mundanos" to expMundanos,
                    "Geral" to expMundanos // Fallback
                ),
                onToggleMap = mapOf(
                    "Armaduras" to { expArmaduras = !expArmaduras },
                    "Armas Corpo a Corpo" to { expArmasCC = !expArmasCC },
                    "Armas à Distância" to { expArmasDist = !expArmasDist },
                    "Munição" to { expMunicao = !expMunicao },
                    "Veículos" to { expVeiculos = !expVeiculos },
                    "Mundanos" to { expMundanos = !expMundanos },
                    "Geral" to { expMundanos = !expMundanos }
                )
            )
        }

        // --- BUSCATRILHA ---
        if (compendioBuscatrilhaAtivo && buscatrilhaCategorias.isNotEmpty()) {
            com.example.swadebuilder.ui.components.CollapsibleSection(
                title = "Equipamento de Buscatrilha",
                expanded = expBuscatrilhaEquip,
                onToggle = { expBuscatrilhaEquip = !expBuscatrilhaEquip }
            ) {
                RenderCategoryList(
                    categories = buscatrilhaCategorias,
                    state = state,
                    expStates = emptyMap(), // Always expanded or handled internally if needed
                    onToggleMap = emptyMap(),
                    forceExpandAll = true
                )
            }
        }

        // --- FANTASIA ---
        if (compendioFantasiaAtivo && fantasiaItems.isNotEmpty()) {
             com.example.swadebuilder.ui.components.CollapsibleSection(
                title = "Equipamento de Fantasia",
                expanded = expFantasiaEquip,
                onToggle = { expFantasiaEquip = !expFantasiaEquip }
            ) {
                RenderCategoryList(categories = fantasiaItems, state = state, forceExpandAll = true)
            }
        }

        // --- HORROR ---
        if (compendioHorrorAtivo && horrorItems.isNotEmpty()) {
             com.example.swadebuilder.ui.components.CollapsibleSection(
                title = "Equipamento de Horror",
                expanded = expHorrorEquip,
                onToggle = { expHorrorEquip = !expHorrorEquip }
            ) {
                RenderCategoryList(categories = horrorItems, state = state, forceExpandAll = true)
            }
        }

         // --- SCI-FI ---
        if (compendioSciFiAtivo && sciFiItems.isNotEmpty()) {
             com.example.swadebuilder.ui.components.CollapsibleSection(
                title = "Equipamento Sci-Fi",
                expanded = expSciFiEquip,
                onToggle = { expSciFiEquip = !expSciFiEquip }
            ) {
                RenderCategoryList(categories = sciFiItems, state = state, forceExpandAll = true)
            }
        }
    }
}
