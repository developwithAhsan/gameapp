package com.example.game

data class Particle(
    val id: Long,
    val x: Float,          // Column cell-space index (0.0f..10.0f)
    val y: Float,          // Row cell-space index (0.0f..10.0f)
    val vx: Float,         // Velocity x (cells/sec)
    val vy: Float,         // Velocity y (cells/sec)
    val colorIndex: Int,   // Color index to match block styling
    val size: Float,       // Size diameter
    val alpha: Float,      // Current opacity
    val lifespan: Float,   // How long it lives in seconds
    val age: Float        // Current age in seconds
)
