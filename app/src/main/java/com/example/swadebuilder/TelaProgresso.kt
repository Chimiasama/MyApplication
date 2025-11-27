package com.example.swadebuilder

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.ui.components.SectionCard

@Composable
fun TelaProgresso() {
    var expInfos by remember { mutableStateOf(true) }
    var expAncs by remember { mutableState of(true) }
    var expComps by remember { mutableStateOf(true) }
    var expEquip by remember { mutableStateOf(true) }
    var expAttrs by remember { mutableStateOf(true) }
    var expPer by remember { mutableStateOf(true) }
    var expVants by remember { mutableStateOf(true) }
    var expResumo by remember { mutableStateOf(true) }
    var expPoderes by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        SectionCard(
            title = "Informações",
            expanded = expInfos,
            onToggle = { expInfos = !expInfos },
            icon = Icons.Default.Description
        ) {
            // Placeholder for progression info
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Ancestralidade",
            expanded = expAncs,
            onToggle = { expAncs = !expAncs },
            icon = Icons.Default.Description
        ) {
            // Placeholder for progression ancestry
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Complicações",
            expanded = expComps,
            onToggle = { expComps = !expComps },
            icon = Icons.Default.Description
        ) {
            // Placeholder for progression complications
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Atributos",
            expanded = expAttrs,
            onToggle = { expAttrs = !expAttrs },
            icon = Icons.Default.FitnessCenter
        ) {
            // Placeholder for progression attributes
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Perícias",
            expanded = expPer,
            onToggle = { expPer = !expPer },
            icon = Icons.Default.School
        ) {
            // Placeholder for progression skills
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Vantagens",
            expanded = expVants,
            onToggle = { expVants = !expVants },
            icon = Icons.Default.Star
        ) {
            // Placeholder for progression advantages
        }

        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Poderes",
            expanded = expPoderes,
            onToggle = { expPoderes = !expPoderes },
            icon = Icons.Default.FlashOn
        ) {
            // Placeholder for progression powers
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp)

        SectionCard(
            title = "Equipamento",
            expanded = expEquip,
            onToggle = { expEquip = !expEquip },
            icon = Icons.Default.Description
        ) {
            // Placeholder for progression equipment
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 3.dp)

        SectionCard(
            title = "Resumo do Personagem",
            expanded = expResumo,
            onToggle = { expResumo = !expResumo },
            icon = Icons.Default.Description
        ) {
            // Placeholder for progression summary
        }
    }
}
