package ph.edu.comteq

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class NoteViewModel(application: Application): AndroidViewModel(application) { // Get an instance of the database and then the DAO from it.

private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()
    val allNotes: Flow<List<Note>>
        =
    noteDao.getALlNotes()
    fun insert(note: Note) = viewModelScope.launch{ noteDao. insertNote (note)
    }
    fun update(note: Note) = viewModelScope.launch { noteDao.updateNote (note)
    }
    fun delete(note: Note) = viewModelScope.launch {
        noteDao.deleteNote (note)
    }
}