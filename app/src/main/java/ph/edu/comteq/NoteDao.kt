package ph.edu.comteq

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getALlNotes(): Flow<List<Note>>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes WHERE title " +
            "LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    fun searchNotes(searchQuery: String): Flow<List<Note>>

    // Connect a note to a tag
    @Insert(onConflict = OnConflictStrategy.IGNORE)  // Ignore if already connected
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    // Disconnect a note from a tag
    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    // Get all notes WITH their tags
    @Transaction
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotesWithTags(): Flow<List<NoteWithTags>>

    // Get a note WITH all its tags
    @Transaction  // Important: Ensures all data loads together
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithTags(noteId: Int): NoteWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)  // Ignore if already connected
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Int): Tag?

    // Get all notes that have a specific tag
    @Transaction
    @Query("""
        SELECT * FROM notes 
        INNER JOIN note_tag_cross_ref ON notes.id = note_tag_cross_ref.note_id
        WHERE note_tag_cross_ref.tag_id = :tagId
        ORDER BY updated_at DESC
    """)
    fun getNotesWithTag(tagId: Int): Flow<List<Note>>
}

