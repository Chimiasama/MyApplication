package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.util.CharacterStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaveLoadDialog(
    initialTab: Int = 0, // 0 = Save, 1 = Load
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onLoad: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("Salvar", "Carregar")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Gerenciar Personagens") },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    SaveTabContent(onSave = onSave)
                } else {
                    LoadTabContent(onLoad = onLoad)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun SaveTabContent(onSave: (String) -> Unit) {
    var filename by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {
        Text("Salvar Personagem Atual", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = filename,
            onValueChange = { filename = it },
            label = { Text("Nome do Arquivo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (filename.isNotBlank()) {
                    onSave(filename)
                }
            },
            enabled = filename.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salvar")
        }
    }
}

@Composable
fun LoadTabContent(onLoad: (String) -> Unit) {
    val context = LocalContext.current
    val fileList = remember { mutableStateListOf<File>() }

    fun refreshList() {
        fileList.clear()
        fileList.addAll(CharacterStorage.listSavedFiles(context))
    }

    LaunchedEffect(Unit) {
        refreshList()
    }

    if (fileList.isEmpty()) {
        Text("Nenhum personagem salvo encontrado.", style = MaterialTheme.typography.bodyMedium)
    } else {
        LazyColumn(modifier = Modifier.height(300.dp)) {
            items(fileList) { file ->
                SavedFileItem(
                    file = file,
                    onLoad = { onLoad(file.name) },
                    onDelete = {
                        CharacterStorage.deleteFile(context, file.name)
                        refreshList()
                    }
                )
            }
        }
    }
}

@Composable
fun SavedFileItem(
    file: File,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val lastModified = dateFormat.format(Date(file.lastModified()))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLoad)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.nameWithoutExtension, style = MaterialTheme.typography.bodyLarge)
            Text(text = lastModified, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.error)
        }
    }
}
