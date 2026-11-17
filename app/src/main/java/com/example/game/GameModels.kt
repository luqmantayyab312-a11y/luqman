package com.example.game

import androidx.compose.ui.graphics.Color
import kotlin.math.PI

enum class VehicleColor(
  val label: String,
  val fill: Color,
  val highlight: Color,
  val shade: Color,
  val passengerFill: Color,
) {
  RED(
    label = "Red",
    fill = Color(0xFFFF4655),
    highlight = Color(0xFFFF7B86),
    shade = Color(0xFFC82333),
    passengerFill = Color(0xFFFF4655),
  ),
  GREEN(
    label = "Green",
    fill = Color(0xFF2ECC5A),
    highlight = Color(0xFF66E489),
    shade = Color(0xFF1B963D),
    passengerFill = Color(0xFF2ECC5A),
  ),
  BLUE(
    label = "Blue",
    fill = Color(0xFF2B7FFF),
    highlight = Color(0xFF6BA7FF),
    shade = Color(0xFF1556C4),
    passengerFill = Color(0xFF2B7FFF),
  ),
  CYAN(
    label = "Cyan",
    fill = Color(0xFF16C8E8),
    highlight = Color(0xFF68E3F8),
    shade = Color(0xFF0A8FB0),
    passengerFill = Color(0xFF16C8E8),
  ),
  YELLOW(
    label = "Yellow",
    fill = Color(0xFFFFC926),
    highlight = Color(0xFFFFE073),
    shade = Color(0xFFD69A00),
    passengerFill = Color(0xFFFFC926),
  ),
  ORANGE(
    label = "Orange",
    fill = Color(0xFFFF7D26),
    highlight = Color(0xFFFFA86B),
    shade = Color(0xFFCC5200),
    passengerFill = Color(0xFFFF7D26),
  ),
  WHITE(
    label = "White",
    fill = Color(0xFFF4F7FC),
    highlight = Color(0xFFFFFFFF),
    shade = Color(0xFFC5D0E0),
    passengerFill = Color(0xFFF4F7FC),
  ),
  BROWN(
    label = "Brown",
    fill = Color(0xFFA46338),
    highlight = Color(0xFFC88A5E),
    shade = Color(0xFF75401E),
    passengerFill = Color(0xFFA46338),
  ),
}

enum class Direction(val dx: Float, val dy: Float, val angleDeg: Float) {
  RIGHT(1f, 0f, 0f),
  DOWN_RIGHT(0.70710677f, 0.70710677f, 45f),
  DOWN(0f, 1f, 90f),
  DOWN_LEFT(-0.70710677f, 0.70710677f, 135f),
  LEFT(-1f, 0f, 180f),
  UP_LEFT(-0.70710677f, -0.70710677f, 225f),
  UP(0f, -1f, 270f),
  UP_RIGHT(0.70710677f, -0.70710677f, 315f);

  val angleRad: Float
    get() = (angleDeg * PI / 180.0).toFloat()

  val isDiagonal: Boolean
    get() = this == UP_LEFT || this == UP_RIGHT || this == DOWN_LEFT || this == DOWN_RIGHT
}

enum class TunnelSide {
  LEFT,
  RIGHT,
}

enum class VehicleStage {
  IN_LOT,
  SPAWNING_FROM_TUNNEL,
  EXITING_LOT,
  DRIVING_TO_SLOT,
  PARKED,
  DEPARTING_FULL,
}

data class VehicleSpec(
  val x: Float,
  val y: Float,
  val color: VehicleColor,
  val direction: Direction,
  val capacity: Int,
)

data class TunnelVehicleSpec(
  val color: VehicleColor,
  val direction: Direction,
  val capacity: Int,
)

data class TunnelSpec(
  val side: TunnelSide,
  val row: Int,
  val queue: List<TunnelVehicleSpec>,
)

data class LevelDefinition(
  val levelNumber: Int,
  val gridCols: Int,
  val gridRows: Int,
  val vehicles: List<VehicleSpec>,
  val tunnels: List<TunnelSpec> = emptyList(),
  val passengerSequence: List<VehicleColor>,
  val conveyorSpeedDpPerSec: Float = 46f,
  val isTutorial: Boolean = false,
  val tutorialHintText: String? = null,
)

fun vehicleLengthForCapacity(capacity: Int): Float =
  when {
    capacity >= 28 -> 2.15f
    capacity >= 24 -> 1.78f
    else -> 1.44f
  }

data class VehicleEntity(
  val id: Int,
  val color: VehicleColor,
  val direction: Direction,
  val capacity: Int,
  val x: Float,
  val y: Float,
  val stage: VehicleStage = VehicleStage.IN_LOT,
  val slotIndex: Int = -1,
  val boardedCount: Int = 0,
  val reservedCount: Int = 0,
  val bumpOffset: Float = 0f,
  val shakePhase: Float = 0f,
  val flashRedTimer: Float = 0f,
  val animProgress: Float = 0f,
  val startX: Float = x,
  val startY: Float = y,
  val targetX: Float = x,
  val targetY: Float = y,
  val bounceTimer: Float = 0f,
  val squashFactor: Float = 1f,
) {
  val lengthInCells: Float
    get() = vehicleLengthForCapacity(capacity)

  val widthInCells: Float
    get() = if (direction.isDiagonal) 0.70f else 0.76f
}

data class TunnelState(
  val id: Int,
  val side: TunnelSide,
  val row: Int,
  val queue: List<TunnelVehicleSpec>,
)

enum class SlotKind {
  VIP,
  NORMAL,
  LOCKED,
  TEMP_REVIVE,
}

data class WaitingSlotState(
  val index: Int,
  val kind: SlotKind,
  val unlocked: Boolean,
  val occupantVehicleId: Int? = null,
)

data class ConveyorPassenger(
  val id: Int,
  val color: VehicleColor,
  val queueIndex: Int,
  val trackPos: Float, // normalized distance along its curved side branch
  val rowGroupId: Int = 0,
  val branch: Int = 0, // 0 = left feeder + oval curve, 1 = right feeder + oval curve
  val colInRow: Int = 0, // 0..3 across the 4-toy row
)

data class JumpingPassenger(
  val id: Int,
  val color: VehicleColor,
  val targetVehicleId: Int,
  val targetSlotIndex: Int,
  val startNormX: Float,
  val startNormY: Float,
  val progress: Float = 0f, // 0f..1f
  val speed: Float = 2.6f,
)

data class Particle(
  val id: Int,
  val x: Float, // normalized 0..1 within playfield
  val y: Float,
  val vx: Float,
  val vy: Float,
  val color: Color,
  val sizeDp: Float,
  val life: Float,
  val maxLife: Float,
  val isConfetti: Boolean = false,
)

data class FloatingText(
  val id: Int,
  val x: Float,
  val y: Float,
  val text: String,
  val color: Color,
  val life: Float,
  val maxLife: Float,
)

enum class BoosterType(
  val title: String,
  val description: String,
  val costCoins: Int,
) {
  BOARD(
    title = "Board Passengers",
    description = "Instantly pulls matching passengers from anywhere on the track into parked buses.",
    costCoins = 200,
  ),
  REFRESH(
    title = "Shuffle Lot",
    description = "Reshuffles all vehicles still in the parking lot into a fresh solvable layout.",
    costCoins = 200,
  ),
  VIP_SLOT(
    title = "VIP Parking Slot",
    description = "Unlocks an extra parking slot immediately so more buses can wait to board.",
    costCoins = 200,
  ),
}

enum class GameOutcome {
  PLAYING,
  PAUSED,
  WON,
  LOST,
}

data class GameUiState(
  val levelNumber: Int = 1,
  val gridCols: Int = 5,
  val gridRows: Int = 5,
  val vehicles: List<VehicleEntity> = emptyList(),
  val tunnels: List<TunnelState> = emptyList(),
  val slots: List<WaitingSlotState> = emptyList(),
  val passengers: List<ConveyorPassenger> = emptyList(),
  val jumpingPassengers: List<JumpingPassenger> = emptyList(),
  val particles: List<Particle> = emptyList(),
  val floatingTexts: List<FloatingText> = emptyList(),
  val totalPassengersInLevel: Int = 0,
  val boardedPassengersTotal: Int = 0,
  val conveyorOffsetPx: Float = 0f,
  val outcome: GameOutcome = GameOutcome.PLAYING,
  val starsEarned: Int = 3,
  val isTutorial: Boolean = false,
  val tutorialVehicleId: Int? = null,
  val tutorialHintText: String? = null,
  val statusBannerText: String? = null,
  val coins: Int = 160,
  val boardBoosters: Int = 0,
  val refreshBoosters: Int = 0,
  val vipBoosters: Int = 0,
  val soundEnabled: Boolean = true,
  val vibrationEnabled: Boolean = true,
  val activeBoosterShop: BoosterType? = null,
  val activeSlotUnlockIndex: Int? = null,
  val showHelpDialog: Boolean = false,
  val showMainShopDialog: Boolean = false,
  val shopNotice: String? = null,
  val usedRevive: Boolean = false,
  val bumpsCount: Int = 0,
)
