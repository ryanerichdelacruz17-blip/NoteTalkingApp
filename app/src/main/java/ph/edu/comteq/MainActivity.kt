package ph.edu.comteq

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.MutableStateFlow
import ph.edu.comteq.ui.theme.NoteTalkingAppTheme

import kotlinx.coroutines.launch

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
    // For search: filter list if needed
    val filteredNotesWithTags = if (isSearchActive && searchQuery.isNotBlank()) {
        notesWithTags.filter { nwt ->
            nwt.note.title.contains(searchQuery, ignoreCase = true) ||
            nwt.note.content.contains(searchQuery, ignoreCase = true) ||
            nwt.tags.any { it.name.contains(searchQuery, ignoreCase = true) }
        }
    } else notesWithTags
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
                        if (filteredNotesWithTags.isEmpty()) {
                            item {
                                Text(
                                    text = "No notes found",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            items(filteredNotesWithTags) { noteWithTags ->
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
            notes = filteredNotesWithTags, // pass filtered list!
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

@Composable
fun NoteEditScreen(
    noteId: Int?,
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit
) {
    val inEditMode = noteId != null
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }
    var showTagInput by remember { mutableStateOf(false) }

    val allTags by viewModel.allTags.collectAsState(initial = emptyList())
    val selectedTags = remember { mutableStateListOf<Tag>() }

    //  Collect the note + tags Flow safely
    val noteWithTags by remember(noteId) {
        if (noteId != null) viewModel.getNoteWithTags(noteId)
        else MutableStateFlow<NoteWithTags?>(null)
    }.collectAsState(initial = null)

    //  Populate UI fields once the note is loaded
    LaunchedEffect(noteWithTags) {
        noteWithTags?.let {
            title = it.note.title
            content = it.note.content
            category = it.note.category
            selectedTags.clear()
            selectedTags.addAll(it.tags)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(if (inEditMode) "Edit Note" else "Add Note", style = MaterialTheme.typography.titleLarge)

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

        //  Tags
        Text("Tags", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            allTags.forEach { tag ->
                val selected = selectedTags.any { it.id == tag.id }
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (selected) selectedTags.removeAll { it.id == tag.id }
                        else selectedTags.add(tag)
                    },
                    label = { Text(tag.name) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            AssistChip(onClick = { showTagInput = true }, label = { Text("+ New Tag") })
        }

        if (showTagInput) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("New Tag Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (newTagName.isNotBlank()) {
                        scope.launch {
                            viewModel.insertTag(Tag(name = newTagName.trim()))
                            newTagName = ""
                            showTagInput = false
                        }
                    }
                }) {
                    Text("Add")
                }
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = { showTagInput = false }) { Text("Cancel") }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Save button
        Button(
            onClick = {
                scope.launch {
                    val note = Note(
                        id = noteId ?: 0,
                        title = title,
                        content = content,
                        category = category
                    )
                    if (inEditMode) {
                        viewModel.update(note)
                    } else {
                        viewModel.insertNoteWithTagsSuspend(note, selectedTags)
                    }
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
            Text("Save")
        }

        if (inEditMode) {
            OutlinedButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}


@Composable
fun NoteCard(note: Note, modifier: Modifier = Modifier, tags: List<Tag> = emptyList()) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val intent = Intent(context, EditNote::class.java)
                intent.putExtra("noteId", note.id)
                context.startActivity(intent)
            },
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
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
