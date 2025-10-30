package ph.edu.comteq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ph.edu.comteq.ui.theme.NoteTalkingAppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteTalkingAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "notes_list"
                ) {
                    composable("notes_list") {
                        NotesListScreenWithSearch(
                            viewModel = viewModel,
                            onAddNote = { navController.navigate("note_edit/new") },
                            onEditNote = { noteId -> navController.navigate("note_edit/$noteId") }
                        )
                    }
                    composable(
                        route = "note_edit/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val noteIdString = backStackEntry.arguments?.getString("noteId")
                        val noteId = if (noteIdString == "new") null else noteIdString?.toIntOrNull()
                        NoteEditScreen(
                            noteId = noteId,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

// Separate the main screen into its own composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesListScreenWithSearch(
    viewModel: NoteViewModel,
    onAddNote: () -> Unit,
    onEditNote: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val notesWithTags by viewModel.allNotesWithTags.collectAsState(initial = emptyList())
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.updateSearchQuery(it)
                    },
                    onSearch = {},
                    active = true,
                    onActiveChange = { shouldExpand ->
                        if (!shouldExpand) {
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
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search"
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.clearSearch()
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        if (notesWithTags.isEmpty()) {
                            item {
                                Text(
                                    text = "No notes found",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            items(notesWithTags) { noteWithTags ->
                                NoteCard(
                                    note = noteWithTags.note,
                                    tags = noteWithTags.tags,
                                    modifier = Modifier.clickable(onClick = { onEditNote(noteWithTags.note.id) })
                                )
                            }
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
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Filled.Add, "Add Note")
            }
        }
    ) { innerPadding ->
        NotesListScreen(
            notes = notesWithTags,
            modifier = Modifier.padding(innerPadding),
            onEditNote = onEditNote
        )
    }
}

@Composable
fun NotesListScreen(
    notes: List<NoteWithTags>,
    modifier: Modifier = Modifier,
    onEditNote: (Int) -> Unit = {}
) {
    LazyColumn(modifier = modifier) {
        items(notes) { noteWithTags ->
            NoteCard(note = noteWithTags.note, tags = noteWithTags.tags, modifier = Modifier.clickable { onEditNote(noteWithTags.note.id) })
        }
    }
}

// Simple NoteEditScreen stub for navigation to work
@Composable
fun NoteEditScreen(
    noteId: Int?,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    // TODO: Implement edit/add note UI
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = if (noteId == null) "Add Note" else "Edit Note")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) { Text("Back") }
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
