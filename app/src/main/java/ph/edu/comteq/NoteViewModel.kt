package ph.edu.comteq

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch


class NoteViewModel(application: Application): AndroidViewModel(application) { // Get an instance of the database and then the DAO from it.

private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()

    // Track what the user is searching for
    private val _searchQuery = MutableStateFlow("")

    val allNotes: Flow<List<Note>> = _searchQuery.flatMapLatest { query ->
    if (query.isBlank()) {
        noteDao.getALlNotes()  // Show everything
    } else {
        noteDao.searchNotes(query)
    }
    }
    // Call this when user types in search box
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    // Call this to clear the search
    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun insert(note: Note) = viewModelScope.launch{ noteDao. insertNote (note)
    }
    fun update(note: Note) = viewModelScope.launch { noteDao.updateNote (note)
    }
    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote (note)
    }
}