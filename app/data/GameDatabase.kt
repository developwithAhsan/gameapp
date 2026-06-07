package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val highScore: Int = 0,
    val currentScore: Int = 0,
    val boardData: String = "", // e.g., "0,1,0,0,..." 100 values
    val activePiecesData: String = "", // Serialized representation
    val comboCount: Int = 0,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val maxUnlockedLevel: Int = 1,
    val currentPlayMode: String = "ENDLESS",
    val selectedLevel: Int = 1
)

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE id = 1 LIMIT 1")
    fun getGameState(): Flow<GameStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)
}

@Database(entities = [GameStateEntity::class], version = 2, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract val gameStateDao: GameStateDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "block_puzzle_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
