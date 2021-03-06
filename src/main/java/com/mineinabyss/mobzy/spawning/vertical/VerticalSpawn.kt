package com.mineinabyss.mobzy.spawning.vertical

import org.bukkit.Location

/**
 * A vertical strip inside a chunk, with x and z located at [loc], spanning from [minY] to [maxY].
 *
 * Will generate a list of [SpawnArea]s, from all air gaps within this vertical strip.
 */
class VerticalSpawn(private val loc: Location, private var minY: Int, private var maxY: Int) {
    val spawnAreas: List<SpawnArea> = findBlockPairs()

    private fun findBlockPairs(): List<SpawnArea> {
        maxY.coerceAtMost(255)
        minY.coerceAtLeast(0)

        val locations: MutableList<SpawnArea> = mutableListOf()
        val highest = loc.world?.getHighestBlockAt(loc)?.location?.apply { y = y.coerceAtLeast(0.0) }
            ?: return emptyList()

        if (highest.blockY !in minY..maxY) return locations
        //add a gap from this location to the sky
        if (highest.blockY != maxY)
            locations.add(SpawnArea(highest.clone().apply { y = maxY.toDouble() }, highest.clone().add(0.0, 1.0, 0.0)))

        //everything below is by far the slowest part of the task, as it gets repeated a lot
        val snapshot = highest.chunk.chunkSnapshot

        //search for gaps and add them to the list as we go down
        var top = highest //the top block of a section
        val x = (highest.blockX % 16).let { if (it < 0) it + 16 else it }
        val z = (highest.blockZ % 16).let { if (it < 0) it + 16 else it }

        var currentBlock = snapshot.getBlockType(x, highest.blockY, z)
        (highest.blockY downTo minY).forEach { y ->
            val nextBlock = snapshot.getBlockType(x, y, z)
            if (currentBlock.isSolid) { //if went from solid to air
                if (!nextBlock.isSolid)
                    top = highest.clone().apply { this.y = y.toDouble() }
            } else if (nextBlock.isSolid || highest.blockY == 1) //if went back to solid or reached the bottom of the world
                locations.add(SpawnArea(top, highest.clone().apply { this.y = y.toDouble() + 1 }))
            currentBlock = nextBlock
        }
        return locations
    }
}

fun Location.checkDown(maxI: Int): Location? {
    var l = clone()
    for (i in 0 until maxI) {
        l = l.add(0.0, -1.0, 0.0)
        if (l.y < 10) return null
        if (l.y >= 256) l.y = 255.0
        if (l.block.type.isSolid) return l.add(0.0, 1.0, 0.0)
    }
    return null
}

fun Location.checkUp(maxI: Int): Location? {
    var l = clone()
    for (i in 0 until maxI) {
        l = l.add(0.0, 1.0, 0.0)
        if (!l.block.type.isSolid) {
            return l
        }
        if (l.y >= 256) return null
        if (l.y < 10) l.y = 10.0
    }
    return null
}
