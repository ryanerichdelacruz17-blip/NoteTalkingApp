package ph.edu.comteq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll // ✅ added
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.edu.comteq.ui.theme.NoteTalkingAppTheme

class EditNote : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val noteId = intent.getIntExtra("noteId", -1)

        setContent {
            NoteTalkingAppTheme {
                if (noteId != -1) {
                    EditNoteScreen(
                        noteId = noteId,
                        viewModel = viewModel,
                        onNavigateBack = { finish() }
                    )
                } else {
                    Text("Error: Note not found")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditNoteScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }

    var isLoaded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    val noteWithTags by viewModel.getNoteWithTags(noteId).collectAsState(initial = null)
    val selectedTags = remember { mutableStateListOf<Tag>() }

    // Load note and its tags
    LaunchedEffect(noteWithTags) {
        noteWithTags?.let {
            title = it.note.title
            content = it.note.content
            category = it.note.category
            selectedTags.clear()
            selectedTags.addAll(it.tags)
            isLoaded = true
        }
    }

    if (!isLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Added verticalScroll for scrollable screen
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // 👈 scrollable!
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Edit Note", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Tags",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedTags.remove(tag)
                        else selectedTags.add(tag)
                    },
                    label = { Text(tag.name) }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTagName,
                onValueChange = { newTagName = it },
                placeholder = { Text("New Tag") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            Button(
                onClick = {
                    if (newTagName.isNotBlank()) {
                        scope.launch {
                            viewModel.addTag(newTagName)
                            newTagName = ""
                        }
                    }
                },
                shape = MaterialTheme.shapes.large
            ) {
                Text("Add")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Update button
        Button(
            onClick = {
                scope.launch {
                    val updatedNote = Note(
                        id = noteId,
                        title = title,
                        content = content,
                        category = category
                    )
                    viewModel.updateNoteWithTags(
                        updatedNote,
                        selectedTags
                    )
                    onNavigateBack()
                }
            },
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6BCB77)
            )
        ) {
            Text("Update")
        }

        // Delete button
        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B)
            )
        ) {
            Text("Delete", color = Color.White)
        }

        // Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            val noteToDelete = Note(
                                id = noteId,
                                title = title,
                                content = content,
                                category = category
                            )
                            viewModel.delete(noteToDelete)
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
