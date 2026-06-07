package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameStateDao: GameStateDao) {
    val gameState: Flow<GameStateEntity?> = gameStateDao.getGameState()

    suspend fun saveGameState(state: GameStateEntity) {
        gameStateDao.saveGameState(state)
    }
}
