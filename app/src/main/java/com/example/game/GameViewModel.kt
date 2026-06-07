package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.GameStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GameScreen {
    MAIN_MENU,
    GAMEPLAY,
    SETTINGS,
    LEVEL_SELECTION
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GameDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameStateDao)

    private val _currentPlayMode = MutableStateFlow("ENDLESS") // "ENDLESS" or "LEVELS"
    val currentPlayMode: StateFlow<String> = _currentPlayMode.asStateFlow()

    private val _selectedLevel = MutableStateFlow(1)
    val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

    private val _maxUnlockedLevel = MutableStateFlow(1)
    val maxUnlockedLevel: StateFlow<Int> = _maxUnlockedLevel.asStateFlow()

    private val _levelCompleted = MutableStateFlow(false)
    val levelCompleted: StateFlow<Boolean> = _levelCompleted.asStateFlow()

    val soundManager = SoundManager()

    private val _board = MutableStateFlow<List<List<Int>>>(List(10) { List(10) { 0 } })
    val board: StateFlow<List<List<Int>>> = _board.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _tray = MutableStateFlow<List<BlockShape?>>(listOf(null, null, null))
    val tray: StateFlow<List<BlockShape?>> = _tray.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _activeScreen = MutableStateFlow(GameScreen.MAIN_MENU)
    val activeScreen: StateFlow<GameScreen> = _activeScreen.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    // Used for showing flash effect on cleared lines
    private val _clearedRowsFlash = MutableStateFlow<Set<Int>>(emptySet())
    val clearedRowsFlash: StateFlow<Set<Int>> = _clearedRowsFlash.asStateFlow()

    private val _clearedColsFlash = MutableStateFlow<Set<Int>>(emptySet())
    val clearedColsFlash: StateFlow<Set<Int>> = _clearedColsFlash.asStateFlow()

    private var isLoaded = false
    private var lastTickTime = System.currentTimeMillis()

    init {
        // Load Game State and Highscore from DB
        viewModelScope.launch {
            repository.gameState.collect { entity ->
                if (entity != null) {
                    _highScore.value = entity.highScore
                    _soundEnabled.value = entity.soundEnabled
                    soundManager.isMuted = !entity.soundEnabled
                    _maxUnlockedLevel.value = entity.maxUnlockedLevel
                    _currentPlayMode.value = entity.currentPlayMode
                    _selectedLevel.value = entity.selectedLevel
                    
                    if (!isLoaded) {
                        isLoaded = true
                        if (entity.boardData.isNotEmpty() && !entity.isGameOver) {
                            _board.value = deserializeBoard(entity.boardData)
                            _score.value = entity.currentScore
                            _comboCount.value = entity.comboCount
                            _tray.value = deserializePieces(entity.activePiecesData)
                            _gameOver.value = entity.isGameOver
                            
                            if (entity.currentPlayMode == "LEVELS") {
                                val target = getTargetScoreForLevel(entity.selectedLevel)
                                _levelCompleted.value = entity.currentScore >= target
                            }
                        } else {
                            // If first run or state was gameover, prepare a new game board on background
                            startNewGame(saveToDb = false)
                        }
                    }
                } else {
                    if (!isLoaded) {
                        isLoaded = true
                        startNewGame(saveToDb = true)
                    }
                }
                soundManager.startBackgroundMusic()
            }
        }

        // Particle update loop runs continuously
        viewModelScope.launch {
            lastTickTime = System.currentTimeMillis()
            while (true) {
                val now = System.currentTimeMillis()
                val dt = (now - lastTickTime) / 1000f
                lastTickTime = now
                tickParticles(dt)
                delay(16) // ~60 FPS
            }
        }
    }

    private fun tickParticles(dt: Float) {
        val current = _particles.value
        if (current.isEmpty()) return
        val updated = current.map { p ->
            val newAge = p.age + dt
            val progress = (newAge / p.lifespan).coerceIn(0f, 1f)
            val alpha = (1.0f - progress).coerceIn(0f, 1f)
            p.copy(
                x = p.x + p.vx * dt,
                y = p.y + p.vy * dt,
                alpha = alpha,
                age = newAge
            )
        }.filter { it.age < it.lifespan }
        _particles.value = updated
    }

    fun startNewGame(saveToDb: Boolean = true) {
        _board.value = List(10) { List(10) { 0 } }
        _score.value = 0
        _comboCount.value = 0
        _gameOver.value = false
        _levelCompleted.value = false
        _clearedRowsFlash.value = emptySet()
        _clearedColsFlash.value = emptySet()
        _tray.value = listOf(
            BlockShapeFactory.createRandomShape(),
            BlockShapeFactory.createRandomShape(),
            BlockShapeFactory.createRandomShape()
        )
        if (saveToDb) {
            saveStateToDb()
        }
    }

    fun startLevelGame(levelNo: Int) {
        _currentPlayMode.value = "LEVELS"
        _selectedLevel.value = levelNo
        _levelCompleted.value = false
        startNewGame(saveToDb = true)
        setScreen(GameScreen.GAMEPLAY)
    }

    fun startEndlessGame() {
        _currentPlayMode.value = "ENDLESS"
        _levelCompleted.value = false
        startNewGame(saveToDb = true)
        setScreen(GameScreen.GAMEPLAY)
    }

    fun advanceToNextLevel() {
        val nextLevel = _selectedLevel.value + 1
        if (nextLevel <= levelsList.size) {
            _selectedLevel.value = nextLevel
            _levelCompleted.value = false
            startNewGame(saveToDb = true)
        } else {
            setScreen(GameScreen.LEVEL_SELECTION)
        }
    }

    fun toggleSound() {
        val nextVal = !_soundEnabled.value
        _soundEnabled.value = nextVal
        soundManager.isMuted = !nextVal
        saveStateToDb()
    }

    fun setScreen(screen: GameScreen) {
        _activeScreen.value = screen
        if (screen == GameScreen.GAMEPLAY && _gameOver.value) {
            startNewGame()
        }
    }

    // Helper to check placement suitability
    fun canPlaceShape(shape: BlockShape, gridRow: Int, gridCol: Int, currentBoard: List<List<Int>>): Boolean {
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.grid[r][c] == 1) {
                    val targetRow = gridRow + r
                    val targetCol = gridCol + c
                    if (targetRow !in 0..9 || targetCol !in 0..9) {
                        return false // Out of board bounds
                    }
                    if (currentBoard[targetRow][targetCol] != 0) {
                        return false // Grid cell already occupied
                    }
                }
            }
        }
        return true
    }

    // Plays the user's dragged piece, processes points, line clears, and updates DB
    fun placeShape(shapeId: Int, gridRow: Int, gridCol: Int): Boolean {
        if (_gameOver.value) return false
        
        val currentTray = _tray.value.toMutableList()
        val indexInTray = currentTray.indexOfFirst { it?.id == shapeId }
        if (indexInTray == -1) return false
        
        val shape = currentTray[indexInTray] ?: return false
        val currentBoard = _board.value
        
        if (!canPlaceShape(shape, gridRow, gridCol, currentBoard)) {
            return false // Invalid coordinate placement
        }
        
        // 1. Mutate the board
        val newBoard = currentBoard.map { it.toMutableList() }
        var blocksPlacedCount = 0
        for (r in 0 until shape.height) {
            for (c in 0 until shape.width) {
                if (shape.grid[r][c] == 1) {
                    newBoard[gridRow + r][gridCol + c] = shape.colorIndex
                    blocksPlacedCount++
                }
            }
        }
        
        // 2. Consume from tray
        currentTray[indexInTray] = null
        
        // 3. Check and process completed rows and columns
        val completedRows = mutableListOf<Int>()
        val completedCols = mutableListOf<Int>()
        
        // Check rows
        for (r in 0..9) {
            if (newBoard[r].all { it != 0 }) {
                completedRows.add(r)
            }
        }
        
        // Check cols
        for (c in 0..9) {
            var completeCol = true
            for (r in 0..9) {
                if (newBoard[r][c] == 0) {
                    completeCol = false
                    break
                }
            }
            if (completeCol) {
                completedCols.add(c)
            }
        }
        
        // 4. Calculate points
        var pointsGained = blocksPlacedCount
        val linesCleared = completedRows.size + completedCols.size
        
        var clearOccured = false
        if (linesCleared > 0) {
            clearOccured = true
            val combo = _comboCount.value + 1
            _comboCount.value = combo
            
            val baseClearPoints = when (linesCleared) {
                1 -> 100
                2 -> 300
                3 -> 600
                4 -> 1000
                else -> 1500
            }
            // Add points with combo bonuses
            pointsGained += baseClearPoints + (combo - 1) * 50
            
            // Set trigger arrays to flash rows and columns
            _clearedRowsFlash.value = completedRows.toSet()
            _clearedColsFlash.value = completedCols.toSet()
            
            // Spawn explosions for cells being cleared!
            // Gather all cells included in cleared rows and columns
            val clearedCellsSet = mutableSetOf<Pair<Int, Int>>()
            for (r in completedRows) {
                for (colIdx in 0..9) clearedCellsSet.add(r to colIdx)
            }
            for (c in completedCols) {
                for (rowIdx in 0..9) clearedCellsSet.add(rowIdx to c)
            }
            
            for ((r, c) in clearedCellsSet) {
                val origColor = newBoard[r][c]
                spawnLineClearParticles(r, c, if (origColor != 0) origColor else shape.colorIndex)
                newBoard[r][c] = 0 // perform clearance
            }
            
            // Play Audio chord
            if (combo > 1) {
                soundManager.playComboSound(combo)
            } else {
                soundManager.playClearSound()
            }
        } else {
            // Reset combo multiplier on non-clearing placements
            _comboCount.value = 0
            soundManager.playPlaceSound()
        }
        
        // Update states
        _board.value = newBoard.map { it.toList() }
        _score.value += pointsGained
        
        if (_score.value > _highScore.value) {
            _highScore.value = _score.value
        }

        // Check for level completion
        if (_currentPlayMode.value == "LEVELS" && !_levelCompleted.value) {
            val target = getTargetScoreForLevel(_selectedLevel.value)
            if (_score.value >= target) {
                _levelCompleted.value = true
                soundManager.playComboSound(4) // happy celebratory tone
                if (_selectedLevel.value == _maxUnlockedLevel.value && _maxUnlockedLevel.value < levelsList.size) {
                    _maxUnlockedLevel.value = _maxUnlockedLevel.value + 1
                }
            }
        }
        
        // If clear occured, clear the flash cells slowly
        if (clearOccured) {
            viewModelScope.launch {
                delay(320)
                _clearedRowsFlash.value = emptySet()
                _clearedColsFlash.value = emptySet()
            }
        }
        
        // If all 3 tray units are empty, refill
        if (currentTray.all { it == null }) {
            currentTray[0] = BlockShapeFactory.createRandomShape()
            currentTray[1] = BlockShapeFactory.createRandomShape()
            currentTray[2] = BlockShapeFactory.createRandomShape()
        }
        _tray.value = currentTray
        
        // Check if user is out of moves
        checkIfGameOver()
        
        // Persist
        saveStateToDb()
        return true
    }

    private fun spawnLineClearParticles(row: Int, col: Int, colorIndex: Int) {
        val pList = mutableListOf<Particle>()
        val baseId = System.nanoTime()
        // Generate sparkling trails expanding radially
        for (i in 0 until 10) {
            val angle = (Math.random() * 2 * Math.PI).toFloat()
            val speed = (1.5f + Math.random() * 4.5f).toFloat() 
            val vx = kotlin.math.cos(angle) * speed
            val vy = kotlin.math.sin(angle) * speed
            val size = (6f + Math.random() * 9f).toFloat()
            val lifespan = (0.35f + Math.random() * 0.4f).toFloat()
            
            pList.add(
                Particle(
                    id = baseId + i,
                    x = col + 0.5f,
                    y = row + 0.5f,
                    vx = vx,
                    vy = vy,
                    colorIndex = colorIndex,
                    size = size,
                    alpha = 1.0f,
                    lifespan = lifespan,
                    age = 0f
                )
            )
        }
        _particles.value = _particles.value + pList
    }

    private fun checkIfGameOver() {
        val currentTray = _tray.value
        val currentBoard = _board.value
        val nonNullPieces = currentTray.filterNotNull()
        
        if (nonNullPieces.isEmpty()) return
        
        var canPlaceAny = false
        for (shape in nonNullPieces) {
            for (r in 0..9) {
                for (c in 0..9) {
                    if (canPlaceShape(shape, r, c, currentBoard)) {
                        canPlaceAny = true
                        break
                    }
                }
                if (canPlaceAny) break
            }
            if (canPlaceAny) break
        }
        
        if (!canPlaceAny) {
            _gameOver.value = true
            soundManager.playGameOverSound()
            saveStateToDb()
        }
    }

    private fun saveStateToDb() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entity = GameStateEntity(
                    id = 1,
                    highScore = _highScore.value,
                    currentScore = _score.value,
                    boardData = serializeBoard(_board.value),
                    activePiecesData = serializePieces(_tray.value),
                    comboCount = _comboCount.value,
                    isGameOver = _gameOver.value,
                    soundEnabled = _soundEnabled.value,
                    maxUnlockedLevel = _maxUnlockedLevel.value,
                    currentPlayMode = _currentPlayMode.value,
                    selectedLevel = _selectedLevel.value
                )
                repository.saveGameState(entity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Direct String Board Serializations
    private fun serializeBoard(board: List<List<Int>>): String {
        return board.flatMap { it }.joinToString(",")
    }

    private fun deserializeBoard(str: String): List<List<Int>> {
        if (str.isEmpty()) return List(10) { List(10) { 0 } }
        val vals = str.split(",").map { it.toIntOrNull() ?: 0 }
        if (vals.size != 100) return List(10) { List(10) { 0 } }
        return List(10) { r -> List(10) { c -> vals[r * 10 + c] } }
    }

    private fun serializePieces(pieces: List<BlockShape?>): String {
        return pieces.joinToString("#") { BlockShapeFactory.serializeShape(it) }
    }

    private fun deserializePieces(str: String): List<BlockShape?> {
        if (str.isEmpty()) return listOf(null, null, null)
        val parts = str.split("#")
        return List(3) { i ->
            if (i < parts.size) BlockShapeFactory.deserializeShape(parts[i]) else null
        }
    }

    fun reviveGame() {
        // Clear 3 random rows completely to free up space for more plays
        val currentBoard = _board.value.map { it.toMutableList() }
        val rowsToClear = (0..9).shuffled().take(3)
        for (r in rowsToClear) {
            for (c in 0..10 - 1) {
                currentBoard[r][c] = 0
            }
        }
        _board.value = currentBoard.map { it.toList() }

        // Refill game slots tray with new randomly formulated layout blocks
        _tray.value = listOf(
            BlockShapeFactory.createRandomShape(),
            BlockShapeFactory.createRandomShape(),
            BlockShapeFactory.createRandomShape()
        )

        _gameOver.value = false
        saveStateToDb()
    }

    fun doubleFinalScore() {
        _score.value = _score.value * 2
        if (_score.value > _highScore.value) {
            _highScore.value = _score.value
        }
        saveStateToDb()
    }

    companion object {
        data class LevelInfo(val levelNo: Int, val targetScore: Int, val description: String)
        val levelsList = (1..1010).map { i ->
            val targetScore = when {
                i == 1 -> 300
                i == 2 -> 650
                i == 3 -> 1100
                i == 4 -> 1800
                i == 5 -> 2600
                i == 6 -> 3500
                i == 7 -> 4500
                i == 8 -> 6000
                i == 9 -> 8000
                i == 10 -> 10000
                else -> 10000 + (i - 10) * 1500
            }
            val desc = when {
                i == 1 -> "Classic Starter"
                i == 2 -> "Line Crusher"
                i == 3 -> "Block Pro"
                i == 4 -> "Grid Master"
                i == 5 -> "Vortex Clears"
                i == 6 -> "Diamond Tactics"
                i == 7 -> "Penta Challenger"
                i == 8 -> "Apex Solver"
                i == 9 -> "Grandmaster Gate"
                i == 10 -> "Block Supreme"
                else -> "Challenge Level $i"
            }
            LevelInfo(i, targetScore, desc)
        }
    }

    fun getTargetScoreForLevel(levelNo: Int): Int {
        return levelsList.firstOrNull { it.levelNo == levelNo }?.targetScore ?: 1000
    }
    
    fun getLevelDescription(levelNo: Int): String {
        return levelsList.firstOrNull { it.levelNo == levelNo }?.description ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopBackgroundMusic()
    }
}
