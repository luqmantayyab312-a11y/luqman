package com.example

import com.example.game.CollisionEngine
import com.example.game.Direction
import com.example.game.LevelRepository
import com.example.game.TunnelState
import com.example.game.VehicleColor
import com.example.game.VehicleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelValidationTest {

  @Test
  fun allLevels1Through30HaveNoOverlapsExactParityAndAreSolvable() {
    val l1Count = LevelRepository.getLevel(1).vehicles.size
    val l2Count = LevelRepository.getLevel(2).vehicles.size
    assertTrue("Level 1 must have 5 to 6 buses (got $l1Count)", l1Count in 5..6)
    assertTrue("Level 2 ($l2Count) must have more buses than Level 1 ($l1Count)", l2Count > l1Count)

    for (levelNum in 1..30) {
      val def = LevelRepository.getLevel(levelNum)
      val entities =
        def.vehicles.mapIndexed { idx, spec ->
          VehicleEntity(
            id = idx + 1,
            color = spec.color,
            direction = spec.direction,
            capacity = spec.capacity,
            x = spec.x,
            y = spec.y,
          )
        }

      // 1. Check capacity >= 20 on every vehicle (lot + tunnels) and no overlaps
      for (v in def.vehicles) {
        assertTrue("Level $levelNum vehicle capacity must be >= 20 (got ${v.capacity})", v.capacity >= 20)
      }
      for (t in def.tunnels) {
        for (q in t.queue) {
          assertTrue("Level $levelNum tunnel vehicle capacity must be >= 20 (got ${q.capacity})", q.capacity >= 20)
        }
      }
      for (i in entities.indices) {
        val boxA = CollisionEngine.boxForVehicle(entities[i])
        for (j in i + 1 until entities.size) {
          val overlaps = CollisionEngine.overlaps(boxA, CollisionEngine.boxForVehicle(entities[j]))
          assertFalse(
            "Level $levelNum has overlapping vehicles $i and $j",
            overlaps,
          )
        }
      }

      // 2. Check solvability including tunnels
      val tunnels = def.tunnels.mapIndexed { idx, t -> TunnelState(idx + 1, t.side, t.row, t.queue) }
      assertTrue(
        "Level $levelNum must be solvable",
        CollisionEngine.canClearAll(entities, tunnels, def.gridCols, def.gridRows),
      )

      // 3. Check exact color parity between passenger sequence and total vehicle capacity
      val vehicleCapByColor = mutableMapOf<VehicleColor, Int>()
      for (v in def.vehicles) {
        vehicleCapByColor[v.color] = (vehicleCapByColor[v.color] ?: 0) + v.capacity
      }
      for (t in def.tunnels) {
        for (q in t.queue) {
          vehicleCapByColor[q.color] = (vehicleCapByColor[q.color] ?: 0) + q.capacity
        }
      }

      val passengersByColor = def.passengerSequence.groupingBy { it }.eachCount()
      assertEquals(
        "Level $levelNum passenger color totals must match vehicle capacities",
        vehicleCapByColor.filterValues { it > 0 },
        passengersByColor,
      )
    }
  }

  @Test
  fun diagonalSweptCollisionDetectsBlockerCorrectly() {
    val mover = VehicleEntity(1, VehicleColor.RED, Direction.UP_RIGHT, 20, 1.5f, 3.5f)
    val blocker = VehicleEntity(2, VehicleColor.BLUE, Direction.LEFT, 20, 3.0f, 2.0f)
    val sweepBlocked = CollisionEngine.checkPath(mover, listOf(mover, blocker), 5, 5)
    assertFalse("Diagonal mover should be blocked by vehicle on UP_RIGHT ray", sweepBlocked.isClear)
    assertEquals(2, sweepBlocked.blockerId)

    val sweepClear = CollisionEngine.checkPath(mover, listOf(mover), 5, 5)
    assertTrue("Diagonal mover should be clear when blocker is removed", sweepClear.isClear)
  }
}
