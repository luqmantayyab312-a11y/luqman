package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.game.AssetManager
import com.example.game.BusView
import com.example.game.VehicleColor
import com.example.game.recolorGreenBusPixels
import com.example.game.recolorGreenWalkSheetPixels
import com.example.game.recolorWalkSheetPixels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PassengerColorRecolorTest {

  private fun argb(a: Int, r: Int, g: Int, b: Int): Int =
    ((a and 0xFF) shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)

  @Test
  fun recolorPreservesAlphaSpecularAndProducesAccurateHuesForAllColors() {
    val sampleRed =
      intArrayOf(
        argb(0, 0, 0, 0),         // 0: fully transparent background
        argb(252, 48, 0, 0),      // 1: dark outline / deep shadow
        argb(251, 225, 17, 0),    // 2: midtone glossy red body
        argb(252, 246, 83, 15),   // 3: glossy highlight sheen
        argb(252, 252, 254, 253), // 4: pure white specular highlight
      )

    val sampleGreen =
      intArrayOf(
        argb(0, 0, 0, 0),         // 0: fully transparent background
        argb(252, 2, 58, 1),      // 1: dark outline / deep shadow
        argb(251, 14, 212, 3),    // 2: midtone glossy green body
        argb(252, 66, 240, 5),    // 3: glossy highlight sheen
        argb(252, 252, 254, 253), // 4: pure white specular highlight
      )

    for (color in VehicleColor.entries) {
      val outLegacy = recolorWalkSheetPixels(sampleRed, color)
      assertEquals(sampleRed.size, outLegacy.size)
      assertEquals(0, outLegacy[0])

      val out = recolorGreenWalkSheetPixels(sampleGreen, color)
      assertEquals(sampleGreen.size, out.size)

      // Transparent stays transparent
      assertEquals(0, out[0])

      // Alpha is preserved on opaque pixels
      assertEquals(251, (out[2] ushr 24) and 0xFF)

      val mr = (out[2] ushr 16) and 0xFF
      val mg = (out[2] ushr 8) and 0xFF
      val mb = (out[2] and 0xFF)

      val sr = (out[4] ushr 16) and 0xFF
      val sg = (out[4] ushr 8) and 0xFF
      val sb = (out[4] and 0xFF)

      // Specular white remains bright white across all colors
      assertTrue("$color specular should stay bright white ($sr,$sg,$sb)", sr >= 245 && sg >= 245 && sb >= 245)

      // Dark outline stays dark
      val dr = (out[1] ushr 16) and 0xFF
      val dg = (out[1] ushr 8) and 0xFF
      val db = (out[1] and 0xFF)
      assertTrue("$color shadow should remain dark ($dr,$dg,$db)", maxOf(dr, maxOf(dg, db)) < 75)

      // Midtone matches expected color family
      when (color) {
        VehicleColor.PURPLE -> assertTrue("Purple ($mr,$mg,$mb)", mb > mr && mr > mg)
        VehicleColor.WHITE -> assertTrue("White ($mr,$mg,$mb)", mr >= 200 && mg >= 205 && mb >= 210)
        VehicleColor.GREEN -> assertTrue("Green ($mr,$mg,$mb)", mg > mr && mg > mb)
        VehicleColor.CYAN -> assertTrue("Cyan ($mr,$mg,$mb)", mg >= 185 && mb >= 185 && mr < 100)
        VehicleColor.RED -> assertTrue("Red ($mr,$mg,$mb)", mr >= 190 && mg < 80 && mb < 80)
        VehicleColor.BLUE -> assertTrue("Blue ($mr,$mg,$mb)", mb >= 195 && mg > mr && mg < 150)
        VehicleColor.YELLOW -> assertTrue("Yellow ($mr,$mg,$mb)", mr >= 200 && mg >= 165 && mb < 70)
        VehicleColor.ORANGE -> assertTrue("Orange ($mr,$mg,$mb)", mr >= 200 && mg in 90..150 && mb < 60)
        VehicleColor.BROWN -> assertTrue("Brown ($mr,$mg,$mb)", mr > mg && mg > mb && mr in 125..200)
      }
    }
  }

  @Test
  fun assetManagerProvidesWalkSheetForEveryVehicleColor() {
    val assets = AssetManager(ApplicationProvider.getApplicationContext())
    val green = assets.getPassengerSprite(VehicleColor.GREEN)
    assertNotNull("green_walk_sheet.png must load", green)

    for (color in VehicleColor.entries) {
      val sheet = assets.getPassengerSprite(color)
      val (cols, rows) = assets.getPassengerGrid(color)
      assertNotNull("Passenger sprite sheet missing for $color", sheet)
      assertEquals("Sheet width must divide evenly by $cols for $color", 0, sheet!!.width % cols)
      assertEquals("Sheet height must divide evenly by $rows for $color", 0, sheet.height % rows)
      assertEquals(8, cols)
      assertEquals(6, rows)
      assertEquals(green!!.width, sheet.width)
      assertEquals(green.height, sheet.height)
    }
  }

  @Test
  fun recolorGreenBusPixelsKeepsWindowsAndChevronsIntactWhileRecoloringBody() {
    val darkWindow = argb(255, 22, 42, 74)
    val whiteChevron = argb(255, 250, 252, 255)
    val yellowHeadlight = argb(255, 255, 220, 45)
    val greenBody = argb(255, 18, 228, 12)
    val transparent = argb(0, 0, 0, 0)

    val src = intArrayOf(transparent, darkWindow, whiteChevron, yellowHeadlight, greenBody)
    for (color in VehicleColor.entries) {
      val out = recolorGreenBusPixels(src, color)
      assertEquals(transparent, out[0])
      assertEquals("Window tint preserved for $color", darkWindow, out[1])
      assertEquals("White chevron preserved for $color", whiteChevron, out[2])
      assertEquals("Headlight preserved for $color", yellowHeadlight, out[3])
      if (color == VehicleColor.RED) {
        val r = (out[4] ushr 16) and 0xFF
        val g = (out[4] ushr 8) and 0xFF
        assertTrue("Red bus body should be red ($r,$g)", r >= 200 && g < 80)
      }
    }
  }

  @Test
  fun assetManagerProvidesAllBusSpritesAndAnimationSheetsForEveryColor() {
    val assets = AssetManager(ApplicationProvider.getApplicationContext())
    assertNotNull("bus_exhaust_puff_sheet.png must load", assets.getExhaustPuffSheet())

    for (color in VehicleColor.entries) {
      assertNotNull("Lot TOP_FRONT missing for $color", assets.getBusSprite(color, BusView.TOP_FRONT))
      assertNotNull("Lot TOP_REAR missing for $color", assets.getBusSprite(color, BusView.TOP_REAR))
      assertNotNull("Lot HORIZ missing for $color", assets.getBusSprite(color, BusView.HORIZ))
      assertNotNull("Lot SIDE missing for $color", assets.getBusSprite(color, BusView.SIDE))
      assertNotNull("Waiting bay bus missing for $color", assets.getWaitingBayBusSprite(color))
    }

    for (color in VehicleColor.busColors) {
      for (view in BusView.entries) {
        assertNotNull("SUPERFAST $view missing for $color", assets.getBusSprite(color, view))
      }
      assertNotNull("Exit blur/side sheet missing for $color", assets.getBusExitBlurSheet(color))
      assertNotNull("Turn/iso sheet missing for $color", assets.getBusTurnSheet(color))
      assertNotNull("Sunroof/bay sheet missing for $color", assets.getBusSunroofSheet(color))
    }
  }

  @Test
  fun conveyorPrefillsRingHoldsRightArmUntilLeftEmptiesAndBoardsFourAtATime() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.game.GameViewModel(app)
    vm.updateLaneViewport(411f, 915f)
    vm.startLevel(1)

    val initial = vm.uiState.value
    val ringRowsInitial = initial.passengers.filter { it.segment == com.example.game.ConveyorSegment.RING }.map { it.rowGroupId }.distinct().size
    val leftRowsInitial = initial.passengers.filter { it.segment == com.example.game.ConveyorSegment.LEFT_ARM }.map { it.rowGroupId }.distinct().size
    val rightRowsInitial = initial.passengers.filter { it.segment == com.example.game.ConveyorSegment.RIGHT_ARM }.map { it.rowGroupId }.distinct().size
    val initialRightDist = initial.passengers.filter { it.segment == com.example.game.ConveyorSegment.RIGHT_ARM }.map { it.distDp }

    assertTrue("Ring should start pre-filled with rows (got $ringRowsInitial)", ringRowsInitial >= 30)
    assertTrue("Left arm should hold overflow rows (got $leftRowsInitial)", leftRowsInitial > 0)
    assertTrue("Right arm should hold overflow rows (got $rightRowsInitial)", rightRowsInitial > 0)
    assertTrue(
      "Queued feeder arm rows must start standing still (not walking in place)",
      initial.passengers.filter { it.segment != com.example.game.ConveyorSegment.RING }.none { it.isWalking },
    )

    // Advance 60 frames with no parked bus: ring is full so neither arm should drain or walk
    repeat(60) { vm.onFrame(0.016f) }
    val afterWait = vm.uiState.value
    val leftRowsAfterWait = afterWait.passengers.filter { it.segment == com.example.game.ConveyorSegment.LEFT_ARM }.map { it.rowGroupId }.distinct().size
    val rightRowsAfterWait = afterWait.passengers.filter { it.segment == com.example.game.ConveyorSegment.RIGHT_ARM }.map { it.rowGroupId }.distinct().size
    assertEquals("LEFT_ARM must wait while RING is full", leftRowsInitial, leftRowsAfterWait)
    assertEquals("RIGHT_ARM must wait while RING is full", rightRowsInitial, rightRowsAfterWait)
    assertTrue(
      "Waiting LEFT_ARM and RIGHT_ARM rows must stay in standing idle pose when ring is full",
      afterWait.passengers.filter { it.segment != com.example.game.ConveyorSegment.RING }.none { it.isWalking },
    )

    // Tap first clear bus in lot and park it so boarding starts
    val clearBusId = com.example.game.CollisionEngine.firstUnblockedVehicleId(afterWait.vehicles, afterWait.gridCols, afterWait.gridRows)
    assertNotNull("Level 1 must have a clear bus", clearBusId)
    val clearBus = afterWait.vehicles.first { it.id == clearBusId }
    vm.onLotTap(clearBus.x, clearBus.y)

    // Advance until parked bus has reserved at least one full row
    var sawBoarded = false
    repeat(240) {
      vm.onFrame(0.016f)
      val cur = vm.uiState.value
      val parked = cur.vehicles.firstOrNull { it.id == clearBusId }
      if (parked != null && parked.reservedCount > 0) {
        assertEquals("Uniform 4-wide rows must reserve multiples of 4 (got ${parked.reservedCount})", 0, parked.reservedCount % 4)
        sawBoarded = true
      }
      // As long as LEFT_ARM still has rows, RIGHT_ARM must NOT drain, advance, or walk
      val remLeft = cur.passengers.count { it.segment == com.example.game.ConveyorSegment.LEFT_ARM }
      val rightGroup = cur.passengers.filter { it.segment == com.example.game.ConveyorSegment.RIGHT_ARM }
      val remRight = rightGroup.map { it.rowGroupId }.distinct().size
      if (remLeft > 0) {
        assertEquals("RIGHT_ARM must wait until LEFT_ARM is completely empty", rightRowsInitial, remRight)
        assertEquals("RIGHT_ARM positions must stay frozen until LEFT_ARM empties", initialRightDist, rightGroup.map { it.distDp })
        assertTrue("RIGHT_ARM must stand still while waiting", rightGroup.none { it.isWalking })
      }
    }
    assertTrue("Clear bus should park and board matching 4-passenger rows", sawBoarded)
  }
}
