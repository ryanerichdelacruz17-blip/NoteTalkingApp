package ph.edu.comteq

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.selects.SelectInstance


@Database(
    entities = [
        Note::class
    ],
    version = 1,
    exportSchema = true  // export to JSON OPTIOANAL
)
abstract class AppDatabase: RoomDatabase(){
    abstract fun noteDao(): NoteDao

    companion object{
            @Volatile
            private var instance: AppDatabase? = null

            fun getDatabase(context: Context): AppDatabase{
                return instance ?: synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "note_database"
                    ).build()
                    this.instance = instance
                    instance
                }
            }
        }
    }