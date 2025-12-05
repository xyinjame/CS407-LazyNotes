package com.cs407.lazynotes.data

import android.content.Context
import androidx.room.*
import com.cs407.lazynotes.R

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val folderId: Int = 0,
    val userId: Int,
    val name: String,
    val lastModified: Long = System.currentTimeMillis()
)
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: Int,
    val title: String,
    val folderName: String,
    val summary: String?,
    val transcript: String?,
    val audioUri: String?
)
@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE userId = :userId ORDER BY lastModified DESC")
    suspend fun getFoldersByUser(userId: Int): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Query("UPDATE folders SET lastModified = :timestamp WHERE userId = :userId AND name = :folderName")
    suspend fun updateLastModified(userId: Int, folderName: String, timestamp: Long)

    @Query("DELETE FROM folders WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Int)

    @Query("""
        UPDATE folders
        SET name = :newName,
            lastModified = :timestamp
        WHERE userId = :userId AND name = :oldName
    """)
    suspend fun renameFolderForUser(
        userId: Int,
        oldName: String,
        newName: String,
        timestamp: Long
    )
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId AND folderName = :folderName")
    suspend fun getNotesForFolder(userId: Int, folderName: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE userId = :userId AND id = :noteId")
    suspend fun getNoteById(userId: Int, noteId: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun delete(noteId: String)

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Int)
}

@Database(entities = [FolderEntity::class, NoteEntity::class], version = 1)
abstract class AppDataDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataDatabase? = null

        fun getDatabase(context: Context): AppDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataDatabase::class.java,
                    "app_data_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}