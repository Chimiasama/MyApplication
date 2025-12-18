package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.ui.components.SectionCard

@Composable
fun TroposSection(
    state: CriadorState,
    feedbackMessages: MutableList<String>,
    onUserFeedback: () -> Unit
) {
    SectionCard(
        title = "Tropos (Arte da Guerra)",
        section = MainSection.TROPOS,
        state = state,
        onToggle = { state.toggleSection(MainSection.TROPOS) }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "Selecione seu Arquétipo (Tropo). Apenas um pode ser escolhido.",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            listaTropos.forEach { tropo ->
                val isSelected = state.tropoSelecionado?.id == tropo.id
                RadioButtonRow(
                    label = tropo.nome,
                    selected = isSelected,
                    onSelect = {
                        onUserFeedback()
                        state.applyTropo(tropo, feedbackMessages)
                    }
                )
                if (isSelected) {
                    Text(
                        text = tropo.descricao,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier
                            .padding(start = 32.dp, bottom = 8.dp)
                            .fillMaxWidth()
                    )
                    if (tropo.tecnicas_iniciais > 0) {
                        Text(
                            text = "Técnicas Iniciais: ${tropo.tecnicas_iniciais}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 32.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
