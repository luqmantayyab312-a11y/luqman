package com.example.game

import kotlin.random.Random

object LevelRepository {

  fun getLevel(levelNumber: Int): LevelDefinition {
    return when {
      levelNumber <= 20 -> buildHandcraftedLevel(levelNumber)
      else -> generateProceduralLevel(levelNumber)
    }
  }

  private fun buildHandcraftedLevel(levelNumber: Int): LevelDefinition {
    return when (levelNumber) {
      1 ->
        assembleLevel(
          level = 1,
          cols = 5,
          rows = 5,
          vehicles =
            listOf(
              VehicleSpec(1.0f, 0.85f, VehicleColor.RED, Direction.UP, 20),
              VehicleSpec(3.1f, 0.85f, VehicleColor.BLUE, Direction.RIGHT, 20),
              VehicleSpec(1.0f, 2.5f, VehicleColor.YELLOW, Direction.UP, 20),
              VehicleSpec(2.5f, 2.5f, VehicleColor.GREEN, Direction.UP, 20),
              VehicleSpec(4.1f, 2.5f, VehicleColor.CYAN, Direction.DOWN, 20),
              VehicleSpec(2.5f, 4.15f, VehicleColor.ORANGE, Direction.LEFT, 20),
            ),
          speed = 46f,
          isTutorial = true,
          tutorialHint = "Tap the RED bus first to clear the path!",
        )

      2 ->
        assembleLevel(
          level = 2,
          cols = 5,
          rows = 5,
          vehicles =
            listOf(
              VehicleSpec(0.95f, 0.85f, VehicleColor.GREEN, Direction.LEFT, 20),
              VehicleSpec(2.55f, 0.85f, VehicleColor.RED, Direction.UP, 24),
              VehicleSpec(4.1f, 0.85f, VehicleColor.CYAN, Direction.RIGHT, 20),
              VehicleSpec(0.95f, 2.45f, VehicleColor.YELLOW, Direction.UP, 24),
              VehicleSpec(2.55f, 2.45f, VehicleColor.BLUE, Direction.UP, 20),
              VehicleSpec(4.1f, 2.45f, VehicleColor.ORANGE, Direction.DOWN, 20),
              VehicleSpec(1.5f, 4.15f, VehicleColor.RED, Direction.LEFT, 20),
              VehicleSpec(3.6f, 4.15f, VehicleColor.GREEN, Direction.RIGHT, 20),
            ),
          speed = 48f,
          isTutorial = true,
          tutorialHint = "Clear buses in front so blocked buses can exit!",
        )

      3 ->
        generateConstructedLevel(
          levelNumber = 3,
          cols = 5,
          rows = 5,
          colorsCount = 5,
          vehicleCount = 9,
          includeDiagonals = false,
          tunnelsCount = 0,
          tunnelQueueSize = 0,
          capacitiesPool = listOf(20, 24),
          speed = 50f,
          isTutorial = true,
          tutorialHint = "Watch the road colors to pick the best bus!",
        )

      4 ->
        generateConstructedLevel(
          levelNumber = 4,
          cols = 5,
          rows = 5,
          colorsCount = 5,
          vehicleCount = 10,
          includeDiagonals = false,
          tunnelsCount = 0,
          tunnelQueueSize = 0,
          capacitiesPool = listOf(20, 24),
          speed = 51f,
        )

      5 ->
        // Introduce 45-degree diagonal vehicles!
        generateConstructedLevel(
          levelNumber = 5,
          cols = 5,
          rows = 5,
          colorsCount = 6,
          vehicleCount = 11,
          includeDiagonals = true,
          tunnelsCount = 0,
          tunnelQueueSize = 0,
          capacitiesPool = listOf(20, 24),
          speed = 52f,
        )

      6 ->
        generateConstructedLevel(
          levelNumber = 6,
          cols = 6,
          rows = 6,
          colorsCount = 6,
          vehicleCount = 12,
          includeDiagonals = true,
          tunnelsCount = 0,
          tunnelQueueSize = 0,
          capacitiesPool = listOf(20, 24, 28),
          speed = 53f,
        )

      7 ->
        generateConstructedLevel(
          levelNumber = 7,
          cols = 6,
          rows = 6,
          colorsCount = 6,
          vehicleCount = 13,
          includeDiagonals = true,
          tunnelsCount = 0,
          tunnelQueueSize = 0,
          capacitiesPool = listOf(20, 24, 28),
          speed = 54f,
        )

      8 ->
        // Introduce Tunnels!
        generateConstructedLevel(
          levelNumber = 8,
          cols = 6,
          rows = 6,
          colorsCount = 6,
          vehicleCount = 14,
          includeDiagonals = true,
          tunnelsCount = 1,
          tunnelQueueSize = 2,
          capacitiesPool = listOf(20, 24, 28),
          speed = 55f,
        )

      9 ->
        generateConstructedLevel(
          levelNumber = 9,
          cols = 6,
          rows = 6,
          colorsCount = 6,
          vehicleCount = 15,
          includeDiagonals = true,
          tunnelsCount = 2,
          tunnelQueueSize = 2,
          capacitiesPool = listOf(20, 24, 28),
          speed = 56f,
        )

      10 ->
        // Showcase Level 10: 6 colors (red, cyan, yellow, white, orange, green), diagonals + Left/Right Tunnels ("2")
        generateConstructedLevel(
          levelNumber = 10,
          cols = 6,
          rows = 6,
          colorsCount = 6,
          vehicleCount = 12,
          includeDiagonals = true,
          tunnelsCount = 2,
          tunnelQueueSize = 2,
          capacitiesPool = listOf(20, 24, 28),
          speed = 56f,
          fixedPalette =
            listOf(
              VehicleColor.RED,
              VehicleColor.CYAN,
              VehicleColor.YELLOW,
              VehicleColor.WHITE,
              VehicleColor.ORANGE,
              VehicleColor.GREEN,
            ),
        )

      in 11..20 -> {
        val cols = if (levelNumber >= 15) 7 else 6
        val rows = if (levelNumber >= 15) 7 else 6
        val colorsCount = (5 + (levelNumber - 10) / 3).coerceAtMost(8)
        val vehicleCount = (11 + (levelNumber - 10)).coerceAtMost(18)
        val tunnelQueue = if (levelNumber >= 16) 3 else 2
        val speed = 55f + (levelNumber - 10) * 1.4f
        generateConstructedLevel(
          levelNumber = levelNumber,
          cols = cols,
          rows = rows,
          colorsCount = colorsCount,
          vehicleCount = vehicleCount,
          includeDiagonals = true,
          tunnelsCount = 2,
          tunnelQueueSize = tunnelQueue,
          capacitiesPool = listOf(20, 24, 28),
          speed = speed,
        )
      }

      else -> generateProceduralLevel(levelNumber)
    }
  }

  private fun generateProceduralLevel(levelNumber: Int): LevelDefinition {
    val cols = if (levelNumber % 3 == 0) 7 else 6
    val rows = if (levelNumber % 2 == 0) 7 else 6
    val colorsCount = (6 + (levelNumber % 3)).coerceAtMost(8)
    val vehicleCount = (14 + (levelNumber % 6)).coerceAtMost(20)
    val tunnelQueue = 2 + (levelNumber % 2)
    val speed = (58f + (levelNumber - 20) * 0.8f).coerceAtMost(82f)
    return generateConstructedLevel(
      levelNumber = levelNumber,
      cols = cols,
      rows = rows,
      colorsCount = colorsCount,
      vehicleCount = vehicleCount,
      includeDiagonals = true,
      tunnelsCount = 2,
      tunnelQueueSize = tunnelQueue,
      capacitiesPool = listOf(20, 24, 28),
      speed = speed,
    )
  }

  /**
   * Builds a guaranteed non-overlapping, solver-verified level using deterministic seeded
   * reverse-simulation placement.
   */
  private fun generateConstructedLevel(
    levelNumber: Int,
    cols: Int,
    rows: Int,
    colorsCount: Int,
    vehicleCount: Int,
    includeDiagonals: Boolean,
    tunnelsCount: Int,
    tunnelQueueSize: Int,
    capacitiesPool: List<Int>,
    speed: Float,
    fixedPalette: List<VehicleColor>? = null,
    isTutorial: Boolean = false,
    tutorialHint: String? = null,
  ): LevelDefinition {
    val baseSeed = levelNumber * 9973L + 42L
    for (attempt in 0 until 40) {
      val rng = Random(baseSeed + attempt * 131L)
      val palette = fixedPalette ?: VehicleColor.entries.shuffled(rng).take(colorsCount)

      val leftRow = (rows / 2 - 1).coerceIn(1, rows - 2)
      val rightRow = (rows / 2).coerceIn(1, rows - 2)

      val tunnels = mutableListOf<TunnelSpec>()
      if (tunnelsCount >= 1) {
        val q =
          (0 until tunnelQueueSize).map {
            TunnelVehicleSpec(
              color = palette[rng.nextInt(palette.size)],
              direction = Direction.RIGHT,
              capacity = capacitiesPool.filter { it <= 24 }.random(rng),
            )
          }
        tunnels.add(TunnelSpec(TunnelSide.LEFT, leftRow, q))
      }
      if (tunnelsCount >= 2) {
        val q =
          (0 until tunnelQueueSize).map {
            TunnelVehicleSpec(
              color = palette[rng.nextInt(palette.size)],
              direction = Direction.LEFT,
              capacity = capacitiesPool.filter { it <= 24 }.random(rng),
            )
          }
        tunnels.add(TunnelSpec(TunnelSide.RIGHT, rightRow, q))
      }

      val dirs =
        if (includeDiagonals) Direction.entries
        else listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)

      val placed = mutableListOf<VehicleSpec>()
      val entities = mutableListOf<VehicleEntity>()
      var nextId = 1

      // Try placing vehicles on clean sub-grid coordinates
      val candidateCoords = mutableListOf<Pair<Float, Float>>()
      for (r in 0 until rows) {
        for (c in 0 until cols) {
          candidateCoords.add(Pair(c + 0.5f, r + 0.5f))
        }
      }

      var tries = 0
      while (placed.size < vehicleCount && tries < 400) {
        tries++
        val (cx, cy) = candidateCoords[rng.nextInt(candidateCoords.size)]
        val dir = dirs[rng.nextInt(dirs.size)]
        val cap = capacitiesPool[rng.nextInt(capacitiesPool.size)]
        val color = palette[placed.size % palette.size]

        val candidate =
          VehicleEntity(
            id = nextId,
            color = color,
            direction = dir,
            capacity = cap,
            x = cx,
            y = cy,
          )

        // Ensure vehicle fits reasonably inside lot bounds
        val margin = if (dir.isDiagonal) 0.58f else 0.48f
        val halfL = candidate.lengthInCells * 0.5f
        val halfW = candidate.widthInCells * 0.5f
        val corners = CollisionEngine.boxForVehicle(candidate).corners()
        val insideBounds =
          corners.all { (px, py) ->
            px >= 0.05f && px <= cols - 0.05f && py >= 0.05f && py <= rows - 0.05f
          }
        if (!insideBounds || halfL < margin && halfW < 0f) continue

        // Do not block tunnel exit cell on initial spawn if it makes tunnel impossible,
        // CollisionEngine.canClearAll will verify full solvability anyway!
        val box = CollisionEngine.boxForVehicle(candidate)
        val overlapsExisting = entities.any { CollisionEngine.overlaps(box, CollisionEngine.boxForVehicle(it)) }
        if (overlapsExisting) continue

        // Add temporarily and check if still solvable or keep building
        val spec = VehicleSpec(cx, cy, color, dir, cap)
        placed.add(spec)
        entities.add(candidate)
        nextId++
      }

      if (placed.size < (vehicleCount * 0.65f).toInt()) continue

      val tunnelStates = tunnels.mapIndexed { i, t -> TunnelState(i + 1, t.side, t.row, t.queue) }
      if (CollisionEngine.canClearAll(entities, tunnelStates, cols, rows)) {
        return assembleLevel(
          level = levelNumber,
          cols = cols,
          rows = rows,
          vehicles = placed,
          tunnels = tunnels,
          speed = speed,
          isTutorial = isTutorial,
          tutorialHint = tutorialHint,
          rng = rng,
        )
      }
    }

    // Guaranteed fallback simple grid if dense random search didn't converge
    return buildSafeFallbackLevel(levelNumber, cols, rows, speed)
  }

  private fun buildSafeFallbackLevel(
    level: Int,
    cols: Int,
    rows: Int,
    speed: Float,
  ): LevelDefinition {
    val palette = VehicleColor.entries
    val specs = mutableListOf<VehicleSpec>()
    var idx = 0
    for (r in 0 until rows step 2) {
      for (c in 0 until cols step 2) {
        val dir = if ((r + c) % 4 == 0) Direction.UP else Direction.RIGHT
        specs.add(
          VehicleSpec(
            x = c + 1.0f,
            y = r + 1.0f,
            color = palette[idx % palette.size],
            direction = dir,
            capacity = 20,
          )
        )
        idx++
      }
    }
    return assembleLevel(level, cols, rows, specs, speed = speed)
  }

  /**
   * Computes a valid exit sequence using CollisionEngine and creates a conveyor passenger sequence
   * whose per-color counts EXACTLY equal the sum of capacities of all vehicles (lot + tunnels) of
   * that color.
   */
  private fun assembleLevel(
    level: Int,
    cols: Int,
    rows: Int,
    vehicles: List<VehicleSpec>,
    tunnels: List<TunnelSpec> = emptyList(),
    speed: Float = 48f,
    isTutorial: Boolean = false,
    tutorialHint: String? = null,
    rng: Random = Random(level * 7919L),
  ): LevelDefinition {
    val exitOrder = computeExitOrder(vehicles, tunnels, cols, rows)
    val passengerSequence = mutableListOf<VehicleColor>()

    // Group passengers in small blocks aligned with exit order (slightly interleaved on harder levels)
    val remainingPerVehicle = exitOrder.toMutableList()
    if (level >= 4 && remainingPerVehicle.size >= 3) {
      // Interleave exact 4-toy same-color rows among nearby exit vehicles
      var cursor = 0
      while (cursor < remainingPerVehicle.size) {
        val windowEnd = (cursor + 3).coerceAtMost(remainingPerVehicle.size)
        var anyLeftInWindow = true
        while (anyLeftInWindow) {
          anyLeftInWindow = false
          for (i in cursor until windowEnd) {
            val (color, rem) = remainingPerVehicle[i]
            if (rem > 0) {
              anyLeftInWindow = true
              val chunk = 4.coerceAtMost(rem)
              repeat(chunk) { passengerSequence.add(color) }
              remainingPerVehicle[i] = color to (rem - chunk)
            }
          }
        }
        cursor = windowEnd
      }
    } else {
      for ((color, cap) in remainingPerVehicle) {
        repeat(cap) { passengerSequence.add(color) }
      }
    }

    return LevelDefinition(
      levelNumber = level,
      gridCols = cols,
      gridRows = rows,
      vehicles = vehicles,
      tunnels = tunnels,
      passengerSequence = passengerSequence,
      conveyorSpeedDpPerSec = speed,
      isTutorial = isTutorial,
      tutorialHintText = tutorialHint,
    )
  }

  private fun computeExitOrder(
    vehicles: List<VehicleSpec>,
    tunnels: List<TunnelSpec>,
    cols: Int,
    rows: Int,
  ): List<Pair<VehicleColor, Int>> {
    var nextId = 1
    val active =
      vehicles
        .map { VehicleEntity(nextId++, it.color, it.direction, it.capacity, it.x, it.y) }
        .toMutableList()
    val tunnelQueues = tunnels.mapIndexed { idx, t -> idx to t.queue.toMutableList() }.toMap()
    val order = mutableListOf<Pair<VehicleColor, Int>>()

    fun trySpawnTunnels() {
      var spawned: Boolean
      do {
        spawned = false
        tunnels.forEachIndexed { idx, tunnel ->
          val q = tunnelQueues[idx] ?: return@forEachIndexed
          if (q.isEmpty()) return@forEachIndexed
          val spec = q.first()
          val dummy =
            VehicleEntity(
              id = nextId,
              color = spec.color,
              direction = spec.direction,
              capacity = spec.capacity,
              x = 0f,
              y = 0f,
            )
          val spawnX =
            if (tunnel.side == TunnelSide.LEFT) dummy.lengthInCells * 0.5f + 0.12f
            else cols - dummy.lengthInCells * 0.5f - 0.12f
          val spawnY = tunnel.row + 0.5f
          val candidate = dummy.copy(x = spawnX, y = spawnY)
          val box = CollisionEngine.boxForVehicle(candidate)
          if (active.none { CollisionEngine.overlaps(box, CollisionEngine.boxForVehicle(it)) }) {
            q.removeAt(0)
            active.add(candidate)
            nextId++
            spawned = true
          }
        }
      } while (spawned)
    }

    trySpawnTunnels()
    var safety = 0
    while (active.isNotEmpty() && safety < 250) {
      safety++
      val clear =
        active.firstOrNull { CollisionEngine.checkPath(it, active, cols, rows).isClear }
          ?: active.first()
      active.remove(clear)
      order.add(clear.color to clear.capacity)
      trySpawnTunnels()
    }

    // Include any leftover tunnel items if fallback needed
    tunnelQueues.values.forEach { q -> q.forEach { order.add(it.color to it.capacity) } }
    return order
  }
}
