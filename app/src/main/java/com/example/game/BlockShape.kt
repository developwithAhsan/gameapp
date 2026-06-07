package com.example.game

data class BlockShape(
    val id: Int,
    val grid: Array<IntArray>,
    val colorIndex: Int
) {
    val height: Int = grid.size
    val width: Int = if (grid.isNotEmpty()) grid[0].size else 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockShape) return false
        if (id != other.id) return false
        if (colorIndex != other.colorIndex) return false
        if (!grid.contentDeepEquals(other.grid)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + grid.contentDeepHashCode()
        result = 31 * result + colorIndex
        return result
    }
}

object BlockShapeFactory {
    private var nextId = 1

    fun createRandomShape(): BlockShape {
        val colorIndex = (1..6).random()
        val shapeType = (0..18).random()
        val grid = when (shapeType) {
            0 -> arrayOf(intArrayOf(1)) // 1x1 single cube
            1 -> arrayOf(intArrayOf(1, 1)) // 1x2 Horizontal block
            2 -> arrayOf(intArrayOf(1), intArrayOf(1)) // 2x1 Vertical block
            3 -> arrayOf(intArrayOf(1, 1, 1)) // 1x3 Horizontal line
            4 -> arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1)) // 3x1 Vertical line
            5 -> arrayOf(intArrayOf(1, 1, 1, 1)) // 1x4 Horizontal line
            6 -> arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)) // 4x1 Vertical line
            7 -> arrayOf(intArrayOf(1, 1, 1, 1, 1)) // 1x5 Horizontal line
            8 -> arrayOf(intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1), intArrayOf(1)) // 5x1 Vertical line
            9 -> arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)) // 2x2 Square
            
            // Corner / L-Shapes 2x2
            10 -> arrayOf(intArrayOf(1, 0), intArrayOf(1, 1)) // L-Shape small Bottom-Left
            11 -> arrayOf(intArrayOf(0, 1), intArrayOf(1, 1)) // L-Shape small Bottom-Right
            12 -> arrayOf(intArrayOf(1, 1), intArrayOf(1, 0)) // L-Shape small Top-Left
            13 -> arrayOf(intArrayOf(1, 1), intArrayOf(0, 1)) // L-Shape small Top-Right
            
            // Corners / Large L-shapes 3x3
            14 -> arrayOf(intArrayOf(1, 0, 0), intArrayOf(1, 0, 0), intArrayOf(1, 1, 1)) // L-Shape large Left
            15 -> arrayOf(intArrayOf(0, 0, 1), intArrayOf(0, 0, 1), intArrayOf(1, 1, 1)) // L-Shape large Right
            
            // T-shapes
            16 -> arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 0)) // Playful T-Shape
            
            // Z, S shapes
            17 -> arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)) // Z-Shape
            18 -> arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)) // S-Shape
            else -> arrayOf(intArrayOf(1))
        }

        return BlockShape(
            id = nextId++,
            grid = grid,
            colorIndex = colorIndex
        )
    }

    // Direct String Serialization for local Room DB saving
    fun serializeShape(shape: BlockShape?): String {
        if (shape == null) return "null"
        val sb = StringBuilder()
        sb.append("${shape.id};${shape.colorIndex};")
        sb.append(shape.grid.size).append("-").append(shape.grid[0].size).append(":")
        for (r in 0 until shape.grid.size) {
            for (c in 0 until shape.grid[0].size) {
                sb.append(shape.grid[r][c])
                if (c < shape.grid[0].size - 1) sb.append(",")
            }
            if (r < shape.grid.size - 1) sb.append("|")
        }
        return sb.toString()
    }

    // Direct Deserialization from Room String
    fun deserializeShape(str: String): BlockShape? {
        if (str == "null" || str.isEmpty()) return null
        return try {
            val parts = str.split(";")
            val id = parts[0].toInt()
            val colorIndex = parts[1].toInt()
            val matrixPart = parts[2]
            
            val headerAndBody = matrixPart.split(":")
            val dimensions = headerAndBody[0].split("-")
            val rows = dimensions[0].toInt()
            val cols = dimensions[1].toInt()
            
            val rStrings = headerAndBody[1].split("|")
            val grid = Array(rows) { r ->
                val cStrings = rStrings[r].split(",")
                IntArray(cols) { c -> cStrings[c].toInt() }
            }
            BlockShape(id, grid, colorIndex)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
