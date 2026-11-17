package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ConveyorPassenger
import com.example.game.FloatingText
import com.example.game.JumpingPassenger
import com.example.game.Particle
import com.example.game.SlotKind
import com.example.game.TunnelSide
import com.example.game.TunnelState
import com.example.game.VehicleColor
import com.example.game.VehicleEntity
import com.example.game.VehicleStage
import com.example.game.WaitingSlotState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.OutlineInk
import com.example.ui.theme.PlayGreen
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val OVAL_CENTER_X_NORM = 0.501f
private const val OVAL_CENTER_Y_NORM = 0.223f
private const val OVAL_RX_NORM = 0.238f
private const val OVAL_RY_NORM = 0.072f
private const val BOARD_PAD_Y_NORM = 0.295f
private const val SLOT_FIRST_X_NORM = 0.142f
private const val SLOT_STEP_X_NORM = 0.0982f
private const val SLOT_CENTER_Y_NORM = 0.378f

@Composable
fun rememberToySprites(): Map<VehicleColor, ImageBitmap> {
  val resources = LocalContext.current.resources
  return remember(resources) {
    mapOf(
      VehicleColor.RED to ImageBitmap.imageResource(resources, R.drawable.toy_red),
      VehicleColor.GREEN to ImageBitmap.imageResource(resources, R.drawable.toy_green),
      VehicleColor.BLUE to ImageBitmap.imageResource(resources, R.drawable.toy_blue),
      VehicleColor.CYAN to ImageBitmap.imageResource(resources, R.drawable.toy_cyan),
      VehicleColor.YELLOW to ImageBitmap.imageResource(resources, R.drawable.toy_yellow),
      VehicleColor.ORANGE to ImageBitmap.imageResource(resources, R.drawable.toy_orange),
      VehicleColor.WHITE to ImageBitmap.imageResource(resources, R.drawable.toy_white),
      VehicleColor.BROWN to ImageBitmap.imageResource(resources, R.drawable.toy_brown),
    )
  }
}

fun DrawScope.drawToySprite(
  sprite: ImageBitmap?,
  center: Offset,
  radius: Float,
  color: VehicleColor,
  alpha: Float = 1f,
) {
  if (sprite == null) {
    drawStickman(center = center, radius = radius, color = color, alpha = alpha)
    return
  }
  val h = radius * 2.55f
  val aspect = sprite.width.toFloat() / sprite.height.toFloat().coerceAtLeast(1f)
  val w = h * aspect
  // Soft drop shadow beneath toy feet
  drawOval(
    color = Color.Black.copy(alpha = 0.26f * alpha),
    topLeft = Offset(center.x - w * 0.42f, center.y + h * 0.34f),
    size = Size(w * 0.84f, h * 0.18f),
  )
  drawImage(
    image = sprite,
    dstOffset = IntOffset((center.x - w * 0.5f).roundToInt(), (center.y - h * 0.5f).roundToInt()),
    dstSize = IntSize(w.roundToInt().coerceAtLeast(1), h.roundToInt().coerceAtLeast(1)),
    alpha = alpha,
  )
}

/**
 * Top Zone Overlay: Draws 4-abreast rows of toy characters streaming down from BOTH
 * top-left and top-right roads around the oval loop toward the boarding pad.
 */
@Composable
fun ConveyorTrackView(
  passengers: List<ConveyorPassenger>,
  conveyorOffsetPx: Float,
  remainingCount: Int,
  modifier: Modifier = Modifier,
) {
  val toySprites = rememberToySprites()
  val frontColor = passengers.firstOrNull()?.color

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val boxW = maxWidth
    val boxH = maxHeight

    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // Glowing boarding pad at bottom-center of the oval road loop (0.501, 0.295)
      val boardCenter = Offset(w * OVAL_CENTER_X_NORM, h * BOARD_PAD_Y_NORM)
      drawCircle(
        color = (frontColor?.fill ?: PlayGreen).copy(alpha = 0.28f),
        radius = 18.dp.toPx(),
        center = boardCenter,
      )
      drawCircle(
        color = GoldCoin,
        radius = 15.dp.toPx(),
        center = boardCenter,
        style = Stroke(width = 2.4.dp.toPx()),
      )

      // Compute screen positions for visible passengers across both Left (0) & Right (1) curves
      val toyRadius = (w * 0.020f).coerceIn(6.dp.toPx(), 9.2.dp.toPx())
      data class PositionedToy(val p: ConveyorPassenger, val pos: Offset)
      val positioned =
        passengers
          .asSequence()
          .filter { it.trackPos <= 0.98f }
          .map { p ->
            val (basePt, normal) = ovalBranchPointAndNormal(p.trackPos, p.branch, w, h)
            val laneStep = w * (0.023f * abs(normal.x) + 0.017f * abs(normal.y))
            val colOffset = (p.colInRow - 1.5f) * laneStep
            val bob = abs(sin((conveyorOffsetPx * 0.11f + p.id * 0.7f).toDouble())).toFloat() * 2.2.dp.toPx()
            val drawPt =
              Offset(
                basePt.x + normal.x * colOffset,
                basePt.y + normal.y * colOffset - bob,
              )
            PositionedToy(p, drawPt)
          }
          .sortedBy { it.pos.y }
          .toList()

      for (item in positioned) {
        drawToySprite(
          sprite = toySprites[item.p.color],
          center = item.pos,
          radius = toyRadius,
          color = item.p.color,
        )
      }
    }

    // Remaining queue pill centered neatly in the blue water window inside the oval
    Box(
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .offset(x = boxW * OVAL_CENTER_X_NORM - 46.dp, y = boxH * OVAL_CENTER_Y_NORM - 14.dp)
          .width(92.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color.White.copy(alpha = 0.93f))
          .border(2.dp, Color(0xFFB4C9E4), RoundedCornerShape(14.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (frontColor != null) {
          Box(
            modifier =
              Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(frontColor.fill)
                .border(1.4.dp, OutlineInk, CircleShape)
          )
          Spacer(Modifier.width(5.dp))
        }
        Text(
          text = "$remainingCount left",
          style = MaterialTheme.typography.labelSmall.copy(fontFamily = FredokaFontFamily, fontSize = 12.sp),
          color = OutlineInk,
          maxLines = 1,
        )
      }
    }
  }
}

/**
 * Maps normalized queue distance [0f..1f] along EITHER Left (branch=0) or Right (branch=1)
 * curved highway feeder + oval loop down to the bottom-center boarding spot (0.501, 0.295).
 */
private fun ovalBranchPointAndNormal(t: Float, branch: Int, w: Float, h: Float): Pair<Offset, Offset> {
  val clamped = t.coerceIn(0f, 1f)
  val isRight = branch == 1
  return if (clamped <= 0.56f) {
    val f = clamped / 0.56f
    val angle =
      if (isRight) (PI * 0.5 - f * (0.68 * PI)).toFloat()
      else (PI * 0.5 + f * (0.68 * PI)).toFloat()
    val cx = w * OVAL_CENTER_X_NORM
    val cy = h * OVAL_CENTER_Y_NORM
    val rx = w * OVAL_RX_NORM
    val ry = h * OVAL_RY_NORM
    val cosA = cos(angle)
    val sinA = sin(angle)
    val pt = Offset(cx + cosA * rx, cy + sinA * ry)
    val len = hypot(cosA, sinA).coerceAtLeast(0.001f)
    Pair(pt, Offset(cosA / len, sinA / len))
  } else {
    val u = (clamped - 0.56f) / 0.44f
    val inv = 1f - u
    val p0x = w * (if (isRight) 0.702f else 0.300f)
    val p0y = h * 0.184f
    val p1x = w * (if (isRight) 0.905f else 0.095f)
    val p1y = h * 0.155f
    val p2x = w * (if (isRight) 0.975f else 0.025f)
    val p2y = h * 0.020f
    val x = inv * inv * p0x + 2f * inv * u * p1x + u * u * p2x
    val y = inv * inv * p0y + 2f * inv * u * p1y + u * u * p2y
    val dx = 2f * inv * (p1x - p0x) + 2f * u * (p2x - p1x)
    val dy = 2f * inv * (p1y - p0y) + 2f * u * (p2y - p1y)
    val len = hypot(dx, dy).coerceAtLeast(0.001f)
    Pair(Offset(x, y), Offset(-dy / len, dx / len))
  }
}

/**
 * Draws a plump, glossy 3D hyper-casual vinyl toy passenger figure (no harsh black outlines),
 * matching the toy passengers on the road and inside open buses in the reference video.
 */
fun DrawScope.drawStickman(center: Offset, radius: Float, color: VehicleColor, alpha: Float = 1f) {
  val topGloss = lerp(color.highlight, Color.White, 0.55f)
  val rimColor = color.shade.copy(alpha = 0.72f * alpha)

  // Soft ground shadow
  drawOval(
    color = Color.Black.copy(alpha = 0.24f * alpha),
    topLeft = Offset(center.x - radius * 0.85f, center.y + radius * 0.55f),
    size = Size(radius * 1.7f, radius * 0.62f),
  )

  // Stubby 3D rounded shoulders / arms
  val shoulderLeft = Offset(center.x - radius * 0.76f, center.y + radius * 0.04f)
  val shoulderSize = Size(radius * 1.52f, radius * 0.88f)
  drawRoundRect(
    brush =
      Brush.radialGradient(
        colors = listOf(topGloss.copy(alpha = alpha), color.fill.copy(alpha = alpha), color.shade.copy(alpha = alpha)),
        center = Offset(center.x - radius * 0.25f, center.y + radius * 0.15f),
        radius = radius * 1.15f,
      ),
    topLeft = shoulderLeft,
    size = shoulderSize,
    cornerRadius = CornerRadius(radius * 0.44f),
  )
  drawRoundRect(
    color = rimColor,
    topLeft = shoulderLeft,
    size = shoulderSize,
    cornerRadius = CornerRadius(radius * 0.44f),
    style = Stroke(width = 0.8.dp.toPx()),
  )

  // Glossy 3D spherical head
  val headRadius = radius * 0.64f
  val headCenter = Offset(center.x, center.y - radius * 0.36f)
  drawCircle(
    brush =
      Brush.radialGradient(
        colors = listOf(
          Color.White.copy(alpha = 0.92f * alpha),
          color.highlight.copy(alpha = alpha),
          color.fill.copy(alpha = alpha),
          color.shade.copy(alpha = alpha),
        ),
        center = Offset(headCenter.x - headRadius * 0.32f, headCenter.y - headRadius * 0.34f),
        radius = headRadius * 1.35f,
      ),
    radius = headRadius,
    center = headCenter,
  )
  drawCircle(
    color = rimColor,
    radius = headRadius,
    center = headCenter,
    style = Stroke(width = 0.75.dp.toPx()),
  )
  // Tiny crisp vinyl shine dot
  drawCircle(
    color = Color.White.copy(alpha = 0.68f * alpha),
    radius = headRadius * 0.22f,
    center = Offset(headCenter.x - headRadius * 0.32f, headCenter.y - headRadius * 0.32f),
  )
}

/**
 * Aligned Waiting Bays Overlay: Maps the 8 game slots directly onto the 8 tilted stand bays
 * in game_play_bg.png (Slot 0 = Gold VIP, Slots 1..4 = 4 Normal, Slots 5..7 = 3 Locked '+').
 */
@Composable
fun WaitingSlotsRow(
  slots: List<WaitingSlotState>,
  vehicles: List<VehicleEntity>,
  onSlotTap: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val toySprites = rememberToySprites()
  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val w = maxWidth
    val h = maxHeight
    val bayWidth = w * 0.098f
    val bayHeight = (h * 0.064f).coerceAtLeast(48.dp)

    for (slot in slots) {
      val occupant = slot.occupantVehicleId?.let { id -> vehicles.firstOrNull { it.id == id } }
      val cx = w * (SLOT_FIRST_X_NORM + slot.index * SLOT_STEP_X_NORM)
      val cy = h * SLOT_CENTER_Y_NORM

      Box(
        modifier =
          Modifier
            .offset(x = cx - bayWidth * 0.5f, y = cy - bayHeight * 0.5f)
            .width(bayWidth)
            .height(bayHeight)
            .clickable(enabled = !slot.unlocked) { onSlotTap(slot.index) }
            .testTag("waiting_slot_${slot.index}"),
        contentAlignment = Alignment.Center,
      ) {
        // Cover baked green '+' with open normal bay once a locked slot is unlocked
        if (slot.kind == SlotKind.LOCKED && slot.unlocked) {
          Image(
            painter = painterResource(id = R.drawable.img_slot_normal),
            contentDescription = "Unlocked Waiting Slot",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
          )
        } else if (slot.kind == SlotKind.VIP && slot.unlocked && occupant == null) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(PlayGreen)
                .border(1.2.dp, Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = "OPEN",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp, fontFamily = FredokaFontFamily),
              color = Color.White,
            )
          }
        }

        val isParkedOrDeparting =
          occupant != null &&
            (occupant.stage == VehicleStage.PARKED || occupant.stage == VehicleStage.DEPARTING_FULL)
        if (isParkedOrDeparting && occupant != null) {
          val isDeparting = occupant.stage == VehicleStage.DEPARTING_FULL
          val departP = if (isDeparting) occupant.animProgress.coerceIn(0f, 1f) else 0f
          val lidCloseFrac = if (isDeparting) (departP / 0.30f).coerceIn(0f, 1f) else 0f
          val driveOutFrac = if (isDeparting && departP > 0.30f) ((departP - 0.30f) / 0.70f).coerceIn(0f, 1f) else 0f
          val departLift = driveOutFrac * driveOutFrac * 88f
          val busAlpha = (1f - (driveOutFrac - 0.55f).coerceAtLeast(0f) / 0.45f).coerceIn(0f, 1f)
          val arriveScale = occupant.squashFactor

          Canvas(modifier = Modifier.fillMaxSize()) {
            val lenPx = size.height * 0.96f * arriveScale
            val widPx = size.width * 0.78f * arriveScale
            val departLiftPx = departLift.dp.toPx()

            withTransform({
              translate(size.width * 0.5f, size.height * 0.5f)
              rotate(-105f, pivot = Offset.Zero)
              translate(departLiftPx, 0f)
            }) {
              drawHyperCasualBus(
                color = occupant.color,
                capacity = occupant.capacity,
                lenPx = lenPx,
                widPx = widPx,
                openCabin = true,
                boardedCount = occupant.boardedCount,
                lidCloseFraction = lidCloseFrac,
                alpha = busAlpha,
                toySprite = toySprites[occupant.color],
              )
            }
          }
        }
      }

      if (occupant != null && (occupant.stage == VehicleStage.PARKED || occupant.stage == VehicleStage.DEPARTING_FULL)) {
        val remaining = (occupant.capacity - occupant.boardedCount).coerceAtLeast(0)
        val progress = (occupant.boardedCount.toFloat() / occupant.capacity.coerceAtLeast(1)).coerceIn(0f, 1f)
        val badgeTextColor =
          if (occupant.color == VehicleColor.WHITE || occupant.color == VehicleColor.YELLOW) OutlineInk
          else Color.White
        Column(
          modifier =
            Modifier
              .offset(x = cx - bayWidth * 0.5f, y = cy + bayHeight * 0.52f)
              .width(bayWidth),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(occupant.color.fill)
                .border(1.4.dp, OutlineInk, RoundedCornerShape(7.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp),
          ) {
            Text(
              text = remaining.toString(),
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontFamily = FredokaFontFamily),
              color = badgeTextColor,
              maxLines = 1,
            )
          }
          Spacer(Modifier.height(2.dp))
          Box(
            modifier =
              Modifier
                .fillMaxWidth(0.76f)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF283454))
          ) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth(progress)
                  .fillMaxHeight()
                  .background(PlayGreen)
            )
          }
        }
      }
    }
  }
}

/**
 * Middle Zone: Grey Asphalt Parking Lot with Tunnel Garages, Glossy Buses/Vans, and Tutorial Hand.
 */
@Composable
fun ParkingLotCanvas(
  gridCols: Int,
  gridRows: Int,
  vehicles: List<VehicleEntity>,
  tunnels: List<TunnelState>,
  tutorialVehicleId: Int?,
  onLotTap: (Float, Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  val infiniteTransition = rememberInfiniteTransition(label = "tutorial_pulse")
  val handBob by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(650, easing = LinearEasing), RepeatMode.Reverse),
      label = "hand_bob",
    )

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    Canvas(
      modifier =
        Modifier
          .fillMaxSize()
          .testTag("parking_lot_canvas")
          .pointerInput(gridCols, gridRows) {
            detectTapGestures { offset ->
              val padH = size.width * 0.085f
              val padV = size.height * 0.055f
              val lotW = size.width - padH * 2f
              val lotH = size.height - padV * 2f
              val cellSize = min(lotW / gridCols, lotH / gridRows)
              val originX = (size.width - cellSize * gridCols) * 0.5f
              val originY = (size.height - cellSize * gridRows) * 0.5f
              val gx = (offset.x - originX) / cellSize
              val gy = (offset.y - originY) / cellSize
              onLotTap(gx, gy)
            }
          },
    ) {
      val padH = size.width * 0.085f
      val padV = size.height * 0.055f
      val lotW = size.width - padH * 2f
      val lotH = size.height - padV * 2f
      val cellSize = min(lotW / gridCols, lotH / gridRows)
      val boardW = cellSize * gridCols
      val boardH = cellSize * gridRows
      val originX = (size.width - boardW) * 0.5f
      val originY = (size.height - boardH) * 0.5f

      // Subtle dashed parking grid lines directly over the background asphalt
      val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f)
      for (c in 1 until gridCols) {
        val x = originX + c * cellSize
        drawLine(
          color = Color.White.copy(alpha = 0.16f),
          start = Offset(x, originY + 8.dp.toPx()),
          end = Offset(x, originY + boardH - 8.dp.toPx()),
          strokeWidth = 1.5.dp.toPx(),
          pathEffect = dash,
        )
      }
      for (r in 1 until gridRows) {
        val y = originY + r * cellSize
        drawLine(
          color = Color.White.copy(alpha = 0.16f),
          start = Offset(originX + 8.dp.toPx(), y),
          end = Offset(originX + boardW - 8.dp.toPx(), y),
          strokeWidth = 1.5.dp.toPx(),
          pathEffect = dash,
        )
      }

      // Draw Tunnels on Left & Right edges
      for (tunnel in tunnels) {
        drawTunnelPortal(
          tunnel = tunnel,
          originX = originX,
          originY = originY,
          boardW = boardW,
          cellSize = cellSize,
          textMeasurer = textMeasurer,
        )
      }

      // Draw vehicles in lot / spawning / exiting
      val lotVehicles =
        vehicles.filter {
          it.stage == VehicleStage.IN_LOT ||
            it.stage == VehicleStage.SPAWNING_FROM_TUNNEL ||
            it.stage == VehicleStage.EXITING_LOT
        }

      for (v in lotVehicles) {
        val shakeJitter =
          if (v.shakePhase > 0f) sin(v.shakePhase * 28.0).toFloat() * 3.dp.toPx() * v.shakePhase else 0f
        val cx =
          originX + (v.x + v.direction.dx * v.bumpOffset) * cellSize - v.direction.dy * shakeJitter
        val cy =
          originY + (v.y + v.direction.dy * v.bumpOffset) * cellSize + v.direction.dx * shakeJitter

        // Subtle dotted tire trail behind bus exiting the parking grid
        if (v.stage == VehicleStage.EXITING_LOT) {
          val sx = originX + v.startX * cellSize
          val sy = originY + v.startY * cellSize
          drawLine(
            color = Color.White.copy(alpha = 0.72f),
            start = Offset(sx, sy),
            end = Offset(cx, cy),
            strokeWidth = 3.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 6.dp.toPx()), 0f),
          )
        }

        drawLotVehicle(
          v = v,
          center = Offset(cx, cy),
          cellSize = cellSize,
        )

        // Tutorial pointing hand & pulsing ring
        if (tutorialVehicleId == v.id && v.stage == VehicleStage.IN_LOT) {
          drawCircle(
            color = GoldCoin.copy(alpha = 0.7f - 0.4f * handBob),
            radius = cellSize * (0.55f + 0.18f * handBob),
            center = Offset(cx, cy),
            style = Stroke(width = 3.5.dp.toPx()),
          )
          drawTutorialHand(center = Offset(cx + 8.dp.toPx(), cy + 12.dp.toPx() - 7.dp.toPx() * handBob))
        }
      }
    }
  }
}

private fun DrawScope.drawTunnelPortal(
  tunnel: TunnelState,
  originX: Float,
  originY: Float,
  boardW: Float,
  cellSize: Float,
  textMeasurer: TextMeasurer,
) {
  val isLeft = tunnel.side == TunnelSide.LEFT
  val portalW = 24.dp.toPx()
  val portalH = cellSize * 0.86f
  val px = if (isLeft) originX - portalW + 4.dp.toPx() else originX + boardW - 4.dp.toPx()
  val py = originY + (tunnel.row + 0.5f) * cellSize - portalH * 0.5f
  val closed = tunnel.queue.isEmpty()

  // Garage frame
  drawRoundRect(
    color = if (closed) Color(0xFF7D8899) else Color(0xFFB35334),
    topLeft = Offset(px, py),
    size = Size(portalW, portalH),
    cornerRadius = CornerRadius(8.dp.toPx()),
  )
  drawRoundRect(
    color = OutlineInk,
    topLeft = Offset(px, py),
    size = Size(portalW, portalH),
    cornerRadius = CornerRadius(8.dp.toPx()),
    style = Stroke(width = 2.2.dp.toPx()),
  )
  // Dark interior or shutter
  drawRoundRect(
    color = if (closed) Color(0xFF5D6675) else Color(0xFF1A202C),
    topLeft = Offset(px + 4.dp.toPx(), py + 5.dp.toPx()),
    size = Size(portalW - 8.dp.toPx(), portalH - 10.dp.toPx()),
    cornerRadius = CornerRadius(5.dp.toPx()),
  )

  // Count badge ("2")
  val badgeCenter = Offset(px + portalW * 0.5f, py - 2.dp.toPx())
  val badgeColor = if (closed) Color(0xFF9AA5B4) else AlertRed
  drawCircle(color = badgeColor, radius = 10.dp.toPx(), center = badgeCenter)
  drawCircle(color = Color.White, radius = 10.dp.toPx(), center = badgeCenter, style = Stroke(width = 2.dp.toPx()))
  val countLayout =
    textMeasurer.measure(
      text = tunnel.queue.size.toString(),
      style =
        TextStyle(
          fontFamily = FredokaFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = Color.White,
        ),
    )
  drawText(
    textLayoutResult = countLayout,
    topLeft = Offset(badgeCenter.x - countLayout.size.width / 2f, badgeCenter.y - countLayout.size.height / 2f),
  )
}

private fun DrawScope.drawLotVehicle(
  v: VehicleEntity,
  center: Offset,
  cellSize: Float,
) {
  val lenPx = v.lengthInCells * cellSize * v.squashFactor
  val widPx = v.widthInCells * cellSize / v.squashFactor
  withTransform({
    translate(center.x, center.y)
    rotate(v.direction.angleDeg, pivot = Offset.Zero)
  }) {
    drawHyperCasualBus(
      color = v.color,
      capacity = v.capacity,
      lenPx = lenPx,
      widPx = widPx,
      flashRed = v.flashRedTimer > 0f,
      showRoofArrow = true,
    )
  }
}

/**
 * Shared 3D glossy hyper-casual bus/van renderer matching the video's clean, rounded,
 * brightly-shaded toy buses (no realistic pen-line sketch artifacts or roof AC boxes).
 */
private fun DrawScope.drawHyperCasualBus(
  color: VehicleColor,
  capacity: Int,
  lenPx: Float,
  widPx: Float,
  flashRed: Boolean = false,
  showRoofArrow: Boolean = false,
  openCabin: Boolean = false,
  boardedCount: Int = 0,
  lidCloseFraction: Float = 0f,
  alpha: Float = 1f,
  toySprite: ImageBitmap? = null,
) {
  val left = -lenPx * 0.5f
  val top = -widPx * 0.5f
  val bodyRadius = widPx * 0.25f

  val hi = (if (flashRed) Color(0xFFFF8894) else color.highlight).copy(alpha = alpha)
  val mid = (if (flashRed) Color(0xFFFF3A4A) else color.fill).copy(alpha = alpha)
  val sh = (if (flashRed) Color(0xFFB31724) else color.shade).copy(alpha = alpha)

  // 1. Soft ground drop shadow
  drawRoundRect(
    color = Color.Black.copy(alpha = 0.26f * alpha),
    topLeft = Offset(left + 1.5.dp.toPx(), top + 3.5.dp.toPx()),
    size = Size(lenPx, widPx),
    cornerRadius = CornerRadius(bodyRadius),
  )

  // 2. Four rounded tires peeking neatly at side wheel arches
  val wheelColor = Color(0xFF1F2735).copy(alpha = alpha)
  val wheelLen = (lenPx * 0.16f).coerceIn(5.dp.toPx(), 12.dp.toPx())
  val wheelH = 3.2.dp.toPx()
  val rearWx = left + lenPx * 0.20f
  val frontWx = left + lenPx * 0.68f
  for (wx in listOf(rearWx, frontWx)) {
    drawRoundRect(
      color = wheelColor,
      topLeft = Offset(wx, top - wheelH * 0.45f),
      size = Size(wheelLen, wheelH),
      cornerRadius = CornerRadius(wheelH * 0.45f),
    )
    drawRoundRect(
      color = wheelColor,
      topLeft = Offset(wx, top + widPx - wheelH * 0.55f),
      size = Size(wheelLen, wheelH),
      cornerRadius = CornerRadius(wheelH * 0.45f),
    )
  }

  // 3. Lower 3D shaded base/bumper rim giving chunky 3D height
  drawRoundRect(
    color = sh,
    topLeft = Offset(left, top + 2.5.dp.toPx()),
    size = Size(lenPx, widPx),
    cornerRadius = CornerRadius(bodyRadius),
  )

  // 4. Main glossy rounded body shell
  drawRoundRect(
    brush =
      Brush.linearGradient(
        colors = listOf(hi, mid, sh),
        start = Offset(0f, top),
        end = Offset(0f, top + widPx),
      ),
    topLeft = Offset(left, top),
    size = Size(lenPx, widPx),
    cornerRadius = CornerRadius(bodyRadius),
  )
  drawRoundRect(
    color = Color(0xFF182236).copy(alpha = 0.55f * alpha),
    topLeft = Offset(left, top),
    size = Size(lenPx, widPx),
    cornerRadius = CornerRadius(bodyRadius),
    style = Stroke(width = 1.4.dp.toPx()),
  )

  // 5. Sleek dark-slate side windows along upper & lower body bevels
  val winCount =
    when {
      capacity >= 28 -> 4
      capacity >= 24 -> 3
      else -> 2
    }
  val winZoneLeft = left + lenPx * 0.10f
  val winZoneW = lenPx * 0.54f
  val winGap = 2.dp.toPx()
  val singleWinW = ((winZoneW - winGap * (winCount - 1)) / winCount).coerceAtLeast(3.dp.toPx())
  val winH = widPx * 0.10f
  val glassColor = Color(0xFF1D3854).copy(alpha = alpha)
  for (wIdx in 0 until winCount) {
    val wx = winZoneLeft + wIdx * (singleWinW + winGap)
    drawRoundRect(
      color = glassColor,
      topLeft = Offset(wx, top + widPx * 0.055f),
      size = Size(singleWinW, winH),
      cornerRadius = CornerRadius(winH * 0.45f),
    )
    drawRoundRect(
      color = glassColor,
      topLeft = Offset(wx, top + widPx * 0.845f),
      size = Size(singleWinW, winH),
      cornerRadius = CornerRadius(winH * 0.45f),
    )
  }

  // 6. Sleek curved dark-blue glossy front windshield (+X nose)
  val wsLeft = left + lenPx * 0.72f
  val wsW = lenPx * 0.16f
  val wsTop = top + widPx * 0.15f
  val wsH = widPx * 0.70f
  drawRoundRect(
    brush =
      Brush.linearGradient(
        colors = listOf(Color(0xFF336D99).copy(alpha = alpha), Color(0xFF18314A).copy(alpha = alpha)),
        start = Offset(wsLeft, wsTop),
        end = Offset(wsLeft + wsW, wsTop + wsH),
      ),
    topLeft = Offset(wsLeft, wsTop),
    size = Size(wsW, wsH),
    cornerRadius = CornerRadius(wsW * 0.35f),
  )
  // Glass shine highlight stripe
  drawLine(
    color = Color(0xFF8FD6FF).copy(alpha = 0.65f * alpha),
    start = Offset(wsLeft + wsW * 0.25f, wsTop + wsH * 0.18f),
    end = Offset(wsLeft + wsW * 0.75f, wsTop + wsH * 0.42f),
    strokeWidth = 1.6.dp.toPx(),
    cap = StrokeCap.Round,
  )

  // 7. Front warm headlights + tiny rear taillights
  val hlR = (widPx * 0.07f).coerceIn(1.5.dp.toPx(), 3.dp.toPx())
  drawCircle(color = Color(0xFFFFF4B8).copy(alpha = alpha), radius = hlR, center = Offset(left + lenPx * 0.94f, top + widPx * 0.25f))
  drawCircle(color = Color(0xFFFFF4B8).copy(alpha = alpha), radius = hlR, center = Offset(left + lenPx * 0.94f, top + widPx * 0.75f))
  drawCircle(color = Color(0xFFFF5252).copy(alpha = alpha), radius = hlR * 0.75f, center = Offset(left + lenPx * 0.045f, top + widPx * 0.26f))
  drawCircle(color = Color(0xFFFF5252).copy(alpha = alpha), radius = hlR * 0.75f, center = Offset(left + lenPx * 0.045f, top + widPx * 0.74f))

  // 8. Elevated glossy roof OR open-top passenger cabin (>=20 seats!)
  val roofLeft = left + lenPx * 0.06f
  val roofTop = top + widPx * 0.13f
  val roofW = lenPx * 0.63f
  val roofH = widPx * 0.74f
  val roofCorner = CornerRadius(widPx * 0.16f)

  if (openCabin) {
    if (lidCloseFraction < 0.99f) {
      drawRoundRect(
        color = Color(0xFF1C263E).copy(alpha = alpha),
        topLeft = Offset(roofLeft, roofTop),
        size = Size(roofW, roofH),
        cornerRadius = roofCorner,
      )
      drawRoundRect(
        color = sh,
        topLeft = Offset(roofLeft, roofTop),
        size = Size(roofW, roofH),
        cornerRadius = roofCorner,
        style = Stroke(width = 1.3.dp.toPx()),
      )

      val (seatCols, seatRows) =
        when {
          capacity >= 28 -> 7 to 4
          capacity >= 24 -> 6 to 4
          capacity >= 20 -> 5 to 4
          capacity >= 12 -> 4 to 3
          capacity >= 8 -> 4 to 2
          capacity >= 6 -> 3 to 2
          else -> 2 to 2
        }
      val cellW = roofW / seatCols
      val cellH = roofH / seatRows
      val seatRadius = (min(cellW, cellH) * 0.40f).coerceAtLeast(1.4.dp.toPx())
      for (sIdx in 0 until capacity) {
        val c = sIdx % seatCols
        val r = sIdx / seatCols
        if (r >= seatRows) break
        val sx = roofLeft + roofW - (c + 0.5f) * cellW
        val sy = roofTop + (r + 0.5f) * cellH
        if (sIdx < boardedCount) {
          if (toySprite != null) {
            val th = (min(cellW, cellH) * 1.15f).coerceAtLeast(4.5.dp.toPx())
            val aspect = toySprite.width.toFloat() / toySprite.height.toFloat().coerceAtLeast(1f)
            val tw = th * aspect
            rotate(degrees = 90f, pivot = Offset(sx, sy)) {
              drawImage(
                image = toySprite,
                dstOffset = IntOffset((sx - tw * 0.5f).roundToInt(), (sy - th * 0.5f).roundToInt()),
                dstSize = IntSize(tw.roundToInt().coerceAtLeast(1), th.roundToInt().coerceAtLeast(1)),
                alpha = alpha,
              )
            }
          } else {
            drawCircle(
              brush =
                Brush.radialGradient(
                  colors = listOf(Color.White.copy(alpha = 0.85f * alpha), hi, mid, sh),
                  center = Offset(sx - seatRadius * 0.3f, sy - seatRadius * 0.3f),
                  radius = seatRadius * 1.35f,
                ),
              radius = seatRadius * 1.15f,
              center = Offset(sx, sy),
            )
          }
        } else {
          drawCircle(
            color = Color(0xFF334363).copy(alpha = alpha),
            radius = seatRadius * 0.72f,
            center = Offset(sx, sy),
          )
        }
      }
    }

    if (lidCloseFraction > 0f) {
      val lidW = roofW * lidCloseFraction
      drawRoundRect(
        brush = Brush.linearGradient(colors = listOf(hi, mid), start = Offset(0f, roofTop), end = Offset(0f, roofTop + roofH)),
        topLeft = Offset(roofLeft, roofTop),
        size = Size(lidW, roofH),
        cornerRadius = roofCorner,
      )
      drawRoundRect(
        color = Color.White.copy(alpha = 0.38f * alpha),
        topLeft = Offset(roofLeft + lidW * 0.08f, roofTop + roofH * 0.16f),
        size = Size(lidW * 0.84f, roofH * 0.22f),
        cornerRadius = CornerRadius(widPx * 0.08f),
      )
    }
  } else {
    // Glossy 3D elevated roof deck
    drawRoundRect(
      brush = Brush.linearGradient(colors = listOf(hi, mid), start = Offset(0f, roofTop), end = Offset(0f, roofTop + roofH)),
      topLeft = Offset(roofLeft, roofTop),
      size = Size(roofW, roofH),
      cornerRadius = roofCorner,
    )
    drawRoundRect(
      color = sh.copy(alpha = 0.45f * alpha),
      topLeft = Offset(roofLeft, roofTop),
      size = Size(roofW, roofH),
      cornerRadius = roofCorner,
      style = Stroke(width = 1.dp.toPx()),
    )
    // Specular glossy shine along top edge of roof
    drawRoundRect(
      color = Color.White.copy(alpha = 0.34f * alpha),
      topLeft = Offset(roofLeft + roofW * 0.08f, roofTop + roofH * 0.12f),
      size = Size(roofW * 0.84f, roofH * 0.18f),
      cornerRadius = CornerRadius(widPx * 0.06f),
    )

    if (showRoofArrow) {
      val arrowCenterX = roofLeft + roofW * 0.50f
      val arrowHalf = (roofH * 0.32f).coerceAtMost(roofW * 0.24f)
      val arrowPath =
        Path().apply {
          moveTo(arrowCenterX - arrowHalf * 0.85f, -arrowHalf * 0.34f)
          lineTo(arrowCenterX + arrowHalf * 0.08f, -arrowHalf * 0.34f)
          lineTo(arrowCenterX + arrowHalf * 0.08f, -arrowHalf * 0.86f)
          lineTo(arrowCenterX + arrowHalf * 1.05f, 0f)
          lineTo(arrowCenterX + arrowHalf * 0.08f, arrowHalf * 0.86f)
          lineTo(arrowCenterX + arrowHalf * 0.08f, arrowHalf * 0.34f)
          lineTo(arrowCenterX - arrowHalf * 0.85f, arrowHalf * 0.34f)
          close()
        }
      val arrowShadowOffsetPx = 1.2.dp.toPx()
      withTransform({ translate(0f, arrowShadowOffsetPx) }) {
        drawPath(arrowPath, color = sh.copy(alpha = 0.65f * alpha))
      }
      drawPath(arrowPath, color = Color.White.copy(alpha = alpha))
    }
  }
}

private fun DrawScope.drawTutorialHand(center: Offset) {
  // Simple cute white cartoon pointing finger with outline
  drawCircle(color = OutlineInk, radius = 13.dp.toPx(), center = center)
  drawCircle(color = Color.White, radius = 10.5.dp.toPx(), center = center)
  drawRoundRect(
    color = OutlineInk,
    topLeft = Offset(center.x - 5.dp.toPx(), center.y - 22.dp.toPx()),
    size = Size(10.dp.toPx(), 18.dp.toPx()),
    cornerRadius = CornerRadius(5.dp.toPx()),
  )
  drawRoundRect(
    color = Color.White,
    topLeft = Offset(center.x - 3.dp.toPx(), center.y - 20.dp.toPx()),
    size = Size(6.dp.toPx(), 15.dp.toPx()),
    cornerRadius = CornerRadius(3.dp.toPx()),
  )
}

/**
 * Full-screen playfield overlay for driving buses around the perimeter road (with dotted trails),
 * jumping stickman arcs, confetti particles, and "+1" floating text.
 */
@Composable
fun PlayfieldEffectsOverlay(
  vehicles: List<VehicleEntity>,
  gridCols: Int,
  gridRows: Int,
  jumpingPassengers: List<JumpingPassenger>,
  particles: List<Particle>,
  floatingTexts: List<FloatingText>,
  totalSlots: Int,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  val toySprites = rememberToySprites()

  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height

    // 1. Buses smoothly driving along the outer road loop around the lot -> curving into their waiting bay
    val lotTop = h * 0.436f
    val lotH = h * 0.434f
    val padH = w * 0.085f
    val padV = lotH * 0.055f
    val cellSize = min((w - padH * 2f) / gridCols, (lotH - padV * 2f) / gridRows)
    val originX = (w - cellSize * gridCols) * 0.5f
    val originY = lotTop + (lotH - cellSize * gridRows) * 0.5f

    val topLaneY = h * 0.422f
    val leftLaneX = w * 0.048f
    val rightLaneX = w * 0.952f
    val botLaneY = h * 0.875f

    for (v in vehicles) {
      if (v.stage != VehicleStage.DRIVING_TO_SLOT || v.slotIndex < 0) continue

      val lotStart = Offset(originX + v.startX * cellSize, originY + v.startY * cellSize)
      val exitPt = Offset(originX + v.targetX * cellSize, originY + v.targetY * cellSize)
      val slotCx = (SLOT_FIRST_X_NORM + v.slotIndex * SLOT_STEP_X_NORM) * w
      val slotCy = SLOT_CENTER_Y_NORM * h

      val waypoints = buildList {
        add(exitPt)
        val exitedTop = v.targetY <= 0.1f && exitPt.y <= topLaneY + 36.dp.toPx()
        val exitedBot = v.targetY >= gridRows - 0.1f
        val exitedLeft = v.targetX <= 0.1f
        val exitedRight = v.targetX >= gridCols - 0.1f

        when {
          exitedTop -> {
            add(Offset(exitPt.x, topLaneY))
            add(Offset(slotCx, topLaneY))
          }
          exitedBot -> {
            val goLeft = exitPt.x < w * 0.5f
            val sideX = if (goLeft) leftLaneX else rightLaneX
            add(Offset(exitPt.x, botLaneY))
            add(Offset(sideX, botLaneY))
            add(Offset(sideX, topLaneY))
            add(Offset(slotCx, topLaneY))
          }
          exitedLeft -> {
            add(Offset(leftLaneX, exitPt.y.coerceAtMost(botLaneY)))
            add(Offset(leftLaneX, topLaneY))
            add(Offset(slotCx, topLaneY))
          }
          exitedRight -> {
            add(Offset(rightLaneX, exitPt.y.coerceAtMost(botLaneY)))
            add(Offset(rightLaneX, topLaneY))
            add(Offset(slotCx, topLaneY))
          }
          else -> {
            val sideX = if (exitPt.x < w * 0.5f) leftLaneX else rightLaneX
            add(Offset(sideX, exitPt.y.coerceIn(topLaneY, botLaneY)))
            add(Offset(sideX, topLaneY))
            add(Offset(slotCx, topLaneY))
          }
        }
        add(Offset(slotCx, slotCy))
      }

      val p = v.animProgress.coerceIn(0f, 1f)
      val sampled = samplePolyline(waypoints, p)
      val curPos = sampled.first
      var curAngle = sampled.second
      if (p > 0.78f) {
        val blend = ((p - 0.78f) / 0.22f).coerceIn(0f, 1f)
        curAngle = lerpAngleDeg(curAngle, -105f, blend)
      }

      // Draw fading curved dotted trail behind the driving bus
      val trailPath = Path().apply {
        moveTo(lotStart.x, lotStart.y)
        for (pt in sampled.third) {
          lineTo(pt.x, pt.y)
        }
        lineTo(curPos.x, curPos.y)
      }
      drawPath(
        path = trailPath,
        color = Color.White.copy(alpha = (0.78f * (1f - p * 0.65f)).coerceIn(0f, 1f)),
        style =
          Stroke(
            width = 3.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 6.dp.toPx()), 0f),
            join = StrokeJoin.Round,
          ),
      )

      // Draw the driving 3D bus at current position along perimeter road
      val bayW = w * 0.098f
      val bayH = h * 0.064f
      val lotLen = v.lengthInCells * cellSize
      val lotWid = v.widthInCells * cellSize
      val lenPx = lotLen + (bayH * 0.90f - lotLen) * p
      val widPx = lotWid + (bayW * 0.68f - lotWid) * p

      withTransform({
        translate(curPos.x, curPos.y)
        rotate(curAngle, pivot = Offset.Zero)
      }) {
        drawHyperCasualBus(
          color = v.color,
          capacity = v.capacity,
          lenPx = lenPx,
          widPx = widPx,
        )
      }
    }

    // 2. Jumping toy characters arc from boarding spot directly into target open bus
    for (j in jumpingPassengers) {
      val sx = j.startNormX * w
      val sy = j.startNormY * h
      val tx = (SLOT_FIRST_X_NORM + j.targetSlotIndex * SLOT_STEP_X_NORM) * w
      val ty = h * SLOT_CENTER_Y_NORM
      val p = j.progress.coerceIn(0f, 1f)
      val arcLift = sin(p * PI).toFloat() * 34.dp.toPx()
      val cx = sx + (tx - sx) * p
      val cy = sy + (ty - sy) * p - arcLift
      drawToySprite(
        sprite = toySprites[j.color],
        center = Offset(cx, cy),
        radius = 8.5.dp.toPx(),
        color = j.color,
      )
    }

    // 3. Confetti & dust particles
    for (pt in particles) {
      val alpha = (pt.life / pt.maxLife).coerceIn(0f, 1f)
      val px = pt.x * w
      val py = pt.y * h
      if (pt.isConfetti) {
        rotate(degrees = pt.life * 360f, pivot = Offset(px, py)) {
          drawRect(
            color = pt.color.copy(alpha = alpha),
            topLeft = Offset(px - pt.sizeDp.dp.toPx() * 0.5f, py - pt.sizeDp.dp.toPx() * 0.35f),
            size = Size(pt.sizeDp.dp.toPx(), pt.sizeDp.dp.toPx() * 0.65f),
          )
        }
      } else {
        drawCircle(
          color = pt.color.copy(alpha = alpha * 0.85f),
          radius = pt.sizeDp.dp.toPx() * 0.5f,
          center = Offset(px, py),
        )
      }
    }

    // 4. Floating "+1" pop texts
    for (ft in floatingTexts) {
      val alpha = (ft.life / ft.maxLife).coerceIn(0f, 1f)
      val layout =
        textMeasurer.measure(
          text = ft.text,
          style =
            TextStyle(
              fontFamily = FredokaFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = ft.color.copy(alpha = alpha),
            ),
        )
      drawText(
        textLayoutResult = layout,
        topLeft = Offset(ft.x * w - layout.size.width / 2f, ft.y * h - layout.size.height / 2f),
      )
    }
  }
}

private fun samplePolyline(
  points: List<Offset>,
  fraction: Float,
): Triple<Offset, Float, List<Offset>> {
  if (points.size <= 1) return Triple(points.firstOrNull() ?: Offset.Zero, -90f, points)
  val segLens = FloatArray(points.size - 1)
  var totalLen = 0f
  for (i in 0 until points.size - 1) {
    val d = hypot(points[i + 1].x - points[i].x, points[i + 1].y - points[i].y)
    segLens[i] = d
    totalLen += d
  }
  if (totalLen <= 0.001f) return Triple(points.last(), -90f, points)

  val targetDist = fraction.coerceIn(0f, 1f) * totalLen
  var acc = 0f
  val passed = mutableListOf<Offset>()
  for (i in 0 until points.size - 1) {
    passed.add(points[i])
    val seg = segLens[i]
    if (acc + seg >= targetDist || i == points.size - 2) {
      val localT = if (seg > 0.001f) ((targetDist - acc) / seg).coerceIn(0f, 1f) else 1f
      val pos =
        Offset(
          points[i].x + (points[i + 1].x - points[i].x) * localT,
          points[i].y + (points[i + 1].y - points[i].y) * localT,
        )
      val angle =
        (atan2((points[i + 1].y - points[i].y).toDouble(), (points[i + 1].x - points[i].x).toDouble()) * 180.0 / PI)
          .toFloat()
      return Triple(pos, angle, passed)
    }
    acc += seg
  }
  return Triple(points.last(), -90f, points)
}

private fun lerpAngleDeg(from: Float, to: Float, t: Float): Float {
  var diff = ((to - from + 540f) % 360f) - 180f
  return from + diff * t
}
