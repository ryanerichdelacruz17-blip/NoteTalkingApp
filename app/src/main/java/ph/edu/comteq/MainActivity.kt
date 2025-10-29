package ph.edu.comteq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.comteq.ui.theme.NoteTalkingAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteTalkingAppTheme {
                var searchQuery by remember { mutableStateOf("") }
                var isSearchActive by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (isSearchActive) {
                            SearchBar(
                                modifier = Modifier.fillMaxWidth(),
                                query = searchQuery,
                                onQueryChange = {
                                    searchQuery = it
                                    viewModel.updateSearchQuery(it)
                                },
                                onSearch = { isSearchActive = false },
                                active = true,
                                onActiveChange = {
                                    if (!it) {
                                        isSearchActive = false
                                        searchQuery = ""
                                        viewModel.clearSearch()
                                    }
                                },
                                placeholder = { Text("Search notes...") },
                                leadingIcon = {
                                    IconButton(onClick = {
                                        isSearchActive = false
                                        searchQuery = ""
                                        viewModel.clearSearch()
                                    }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
                                    }
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            viewModel.clearSearch()
                                        }) {
                                            Icon(Icons.Default.Clear, "Clear search")
                                        }
                                    }
                                }
                            ) {
                                val notes by viewModel.allNotes.collectAsState(initial = emptyList())
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp)
                                ) {
                                    items(notes) { note ->
                                        NoteCard(note = note, tags = emptyList())
                                    }
                                }
                            }
                        } else {
                            TopAppBar(
                                title = { Text("Notes") },
                                actions = {
                                    IconButton(onClick = { isSearchActive = true }) {
                                        Icon(Icons.Filled.Search, "Search")
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            val sampleNote = Note(
                                title = "Test Title",
                                content = "This is a test note content",
                                category = "Test Category"
                            )
                            val sampleTags = listOf(
                                Tag(name = "Project"),
                                Tag(name = "Android"),
                                Tag(name = "Kotlin")
                            )
                            viewModel.insertNoteWithTags(sampleNote, sampleTags)
                        }) {
                            Icon(Icons.Filled.Add, "Add note")
                        }
                    }
                ) { innerPadding ->
                    NoteListScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NoteListScreen(viewModel: NoteViewModel, modifier: Modifier = Modifier) {
    val notesWithTags by viewModel.allNotesWithTags.collectAsState(initial = emptyList())
    LazyColumn(modifier = modifier) {
        items(notesWithTags) { noteWithTags ->
            NoteCard(note = noteWithTags.note, tags = noteWithTags.tags)
        }
    }
}

@Composable
fun NoteCard(note: Note, modifier: Modifier = Modifier, tags: List<Tag> = emptyList()) {
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = DateUtils.formatDateTime(note.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (note.category.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = note.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Text(
                text = note.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = tag.name,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
