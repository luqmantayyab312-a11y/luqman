package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerProgress
import com.example.game.BoosterType
import com.example.game.VehicleColor
import com.example.ui.components.CoinPill
import com.example.ui.components.PromoModalDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShopDialog
import com.example.ui.components.drawToySprite
import com.example.ui.components.rememberToySprites
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AsphaltBase
import com.example.ui.theme.CottageRoof
import com.example.ui.theme.CottageTeal
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.GoldCoinDark
import com.example.ui.theme.GrassBright
import com.example.ui.theme.HudBlue
import com.example.ui.theme.HudBlueDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OutlineInk
import com.example.ui.theme.PlayGreen
import com.example.ui.theme.PlayGreenDark
import com.example.ui.theme.SandWarm
import com.example.ui.theme.ShopPurple
import com.example.ui.theme.ShopPurpleDark
import com.example.ui.theme.SkyBottom
import com.example.ui.theme.SkyTop
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HomeScreen(
  progress: PlayerProgress,
  shopNotice: String?,
  onPlayLevel: (Int) -> Unit,
  onToggleSound: (Boolean) -> Unit,
  onToggleVibration: (Boolean) -> Unit,
  onBuyBooster: (BoosterType) -> Unit,
  onClaimFreeCoins: () -> Unit,
  onClaimStarterPack: () -> Unit,
  onUnlockNoAds: () -> Unit,
  onResetToLevel1: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showSettings by remember { mutableStateOf(false) }
  var showShop by remember { mutableStateOf(false) }
  var showStarterPack by remember { mutableStateOf(false) }
  var showNoAds by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    BeachHomeCanvas(modifier = Modifier.fillMaxSize())

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .widthIn(max = 560.dp)
          .align(Alignment.Center)
          .windowInsetsPadding(WindowInsets.statusBars)
          .windowInsetsPadding(WindowInsets.navigationBars)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top HUD: Title + Coin Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(18.dp))
              .background(HudBlue)
              .border(2.5.dp, OutlineInk, RoundedCornerShape(18.dp))
              .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
          Text(
            text = "BUS JAM",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = FredokaFontFamily, fontSize = 20.sp),
            color = Color.White,
          )
        }

        CoinPill(coins = progress.coins, onAddClick = { showShop = true })
      }

      // Left-side round promo buttons (No Ads & Starter Pack)
      Column(
        modifier = Modifier.padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
      ) {
        SideCircleBadge(
          label = if (progress.noAdsUnlocked) "AD FREE" else "NO ADS",
          icon = Icons.Filled.Block,
          bgColor = if (progress.noAdsUnlocked) PlayGreen else AlertRed,
          onClick = { showNoAds = true },
          testTag = "no_ads_button",
        )
        SideCircleBadge(
          label = if (progress.starterPackClaimed) "CLAIMED" else "STARTER",
          icon = Icons.Filled.CardGiftcard,
          bgColor = GoldCoinDark,
          onClick = { showStarterPack = true },
          testTag = "starter_pack_button",
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // Bottom Control Bar: Settings | LEVEL N Play | Shop
      Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GlossyIconSquare(
          icon = Icons.Filled.Settings,
          contentDescription = "Settings",
          topColor = HudBlue,
          bottomColor = HudBlueDark,
          onClick = { showSettings = true },
          testTag = "home_settings_button",
        )

        val levelBtnInteraction = remember { MutableInteractionSource() }
        val levelBtnPressed by levelBtnInteraction.collectIsPressedAsState()
        val levelBtnScale by animateFloatAsState(if (levelBtnPressed) 0.92f else 1f, spring(), label = "stage_btn_scale")

        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(66.dp)
              .scale(levelBtnScale)
              .clickable(interactionSource = levelBtnInteraction, indication = null) {
                onPlayLevel(progress.currentLevel)
              }
              .testTag("play_level_button"),
          contentAlignment = Alignment.Center,
        ) {
          Image(
            painter = painterResource(id = R.drawable.btn_stage_level),
            contentDescription = "LEVEL ${progress.currentLevel}",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
          )
          Text(
            text = "LEVEL ${progress.currentLevel}",
            style =
              MaterialTheme.typography.titleLarge.copy(
                fontFamily = FredokaFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                shadow = Shadow(color = Color(0xFF042D3E), offset = Offset(0f, 3f), blurRadius = 2f),
              ),
            color = Color.White,
            maxLines = 1,
          )
        }

        GlossyIconSquare(
          icon = Icons.Filled.ShoppingCart,
          contentDescription = "Shop",
          topColor = ShopPurple,
          bottomColor = ShopPurpleDark,
          onClick = { showShop = true },
          testTag = "home_shop_button",
        )
      }
    }
  }

  if (showSettings) {
    SettingsDialog(
      soundEnabled = progress.soundEnabled,
      vibrationEnabled = progress.vibrationEnabled,
      onToggleSound = onToggleSound,
      onToggleVibration = onToggleVibration,
      onResetToLevel1 = {
        onResetToLevel1()
        showSettings = false
      },
      onDismiss = { showSettings = false },
    )
  }

  if (showShop) {
    ShopDialog(
      coins = progress.coins,
      boardCount = progress.boardBoosters,
      refreshCount = progress.refreshBoosters,
      vipCount = progress.vipBoosters,
      notice = shopNotice,
      onBuyBooster = onBuyBooster,
      onClaimFreeCoins = onClaimFreeCoins,
      onDismiss = { showShop = false },
    )
  }

  if (showStarterPack) {
    PromoModalDialog(
      title = "STARTER PACK",
      description =
        if (progress.starterPackClaimed) {
          "You already claimed your Starter Pack! Enjoy your coins and boosters."
        } else {
          "Claim a free Starter Pack gift: +250 Coins and +1 of every Booster (Board, Shuffle, VIP Slot)!"
        },
      buttonText = "Claim Free Gift!",
      alreadyClaimed = progress.starterPackClaimed,
      onAction = {
        onClaimStarterPack()
        showStarterPack = false
      },
      onDismiss = { showStarterPack = false },
    )
  }

  if (showNoAds) {
    PromoModalDialog(
      title = "NO ADS PASS",
      description =
        if (progress.noAdsUnlocked) {
          "Ad-Free Pass is active! Enjoy uninterrupted puzzle sessions."
        } else {
          "Bus Jam plays completely ad-free! Activate your Ad-Free badge right now."
        },
      buttonText = "Activate Ad-Free",
      alreadyClaimed = progress.noAdsUnlocked,
      onAction = {
        onUnlockNoAds()
        showNoAds = false
      },
      onDismiss = { showNoAds = false },
    )
  }
}

@Composable
private fun SideCircleBadge(
  label: String,
  icon: ImageVector,
  bgColor: Color,
  onClick: () -> Unit,
  testTag: String,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick).testTag(testTag),
  ) {
    Box(
      modifier =
        Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(bgColor)
          .border(2.5.dp, OutlineInk, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
    }
    Spacer(Modifier.height(3.dp))
    Box(
      modifier =
        Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(Color.White.copy(alpha = 0.9f))
          .border(1.5.dp, OutlineInk, RoundedCornerShape(8.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FredokaFontFamily),
        color = OutlineInk,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun GlossyIconSquare(
  icon: ImageVector,
  contentDescription: String,
  topColor: Color,
  bottomColor: Color,
  onClick: () -> Unit,
  testTag: String,
) {
  Box(
    modifier =
      Modifier
        .size(62.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(bottomColor)
        .padding(bottom = 5.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(topColor)
        .border(2.5.dp, OutlineInk, RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    contentAlignment = Alignment.Center,
  ) {
    Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(28.dp))
  }
}

@Composable
private fun BeachHomeCanvas(modifier: Modifier = Modifier) {
  val resources = LocalContext.current.resources
  val homeBusBitmap = remember(resources) { ImageBitmap.imageResource(resources, R.drawable.img_home_bus) }
  val toySprites = rememberToySprites()
  val inf = rememberInfiniteTransition(label = "beach_home")
  val phase by
    inf.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
      label = "beach_phase",
    )

  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Sky
    drawRect(brush = Brush.verticalGradient(listOf(SkyTop, SkyBottom)), size = size)

    // Warm Sun
    drawCircle(
      color = GoldCoin.copy(alpha = 0.25f),
      radius = 58.dp.toPx(),
      center = Offset(w * 0.80f, h * 0.16f),
    )
    drawCircle(
      color = Color(0xFFFFEA70),
      radius = 36.dp.toPx(),
      center = Offset(w * 0.80f, h * 0.16f),
    )

    // Drifting Clouds
    val cloudDrift = phase * w * 0.18f
    drawCloud(Offset((w * 0.22f + cloudDrift) % (w * 1.15f) - w * 0.1f, h * 0.14f), 26.dp.toPx())
    drawCloud(Offset((w * 0.62f + cloudDrift * 0.6f) % (w * 1.15f) - w * 0.1f, h * 0.21f), 20.dp.toPx())

    // Ocean band
    val oceanTop = h * 0.36f
    drawRect(
      color = OceanBlue,
      topLeft = Offset(0f, oceanTop),
      size = Size(w, h * 0.16f),
    )
    // Gentle animated wave foam
    val waveShift = sin(phase * 2 * PI).toFloat() * 8.dp.toPx()
    drawLine(
      color = Color.White.copy(alpha = 0.65f),
      start = Offset(w * 0.08f + waveShift, oceanTop + h * 0.05f),
      end = Offset(w * 0.36f + waveShift, oceanTop + h * 0.05f),
      strokeWidth = 4.dp.toPx(),
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color.White.copy(alpha = 0.55f),
      start = Offset(w * 0.56f - waveShift, oceanTop + h * 0.09f),
      end = Offset(w * 0.86f - waveShift, oceanTop + h * 0.09f),
      strokeWidth = 4.dp.toPx(),
      cap = StrokeCap.Round,
    )

    // Sandy beach
    val sandTop = h * 0.47f
    drawRect(color = SandWarm, topLeft = Offset(0f, sandTop), size = Size(w, h * 0.14f))

    // Green Coastal Lawn
    val lawnTop = h * 0.55f
    drawRect(color = GrassBright, topLeft = Offset(0f, lawnTop), size = Size(w, h - lawnTop))

    // Teal Two-Storey Cottage
    drawBeachCottage(centerX = w * 0.55f, baseY = lawnTop + h * 0.06f)

    // Swaying Palm Trees
    val sway = sin(phase * 2 * PI).toFloat() * 4f
    drawPalmTree(base = Offset(w * 0.14f, lawnTop + h * 0.05f), scale = 1.0f, swayDeg = sway)
    drawPalmTree(base = Offset(w * 0.88f, lawnTop + h * 0.06f), scale = 0.85f, swayDeg = -sway)

    // White Picket Fence along lawn
    val fenceY = lawnTop + h * 0.065f
    drawRect(color = Color.White, topLeft = Offset(0f, fenceY + 8.dp.toPx()), size = Size(w, 6.dp.toPx()))
    var fx = 10.dp.toPx()
    while (fx < w) {
      drawRoundRect(
        color = Color.White,
        topLeft = Offset(fx, fenceY),
        size = Size(10.dp.toPx(), 24.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx()),
      )
      drawRoundRect(
        color = OutlineInk.copy(alpha = 0.35f),
        topLeft = Offset(fx, fenceY),
        size = Size(10.dp.toPx(), 24.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx()),
      )
      fx += 22.dp.toPx()
    }

    // Asphalt Street
    val roadTop = h * 0.67f
    val roadH = h * 0.17f
    drawRect(color = AsphaltBase, topLeft = Offset(0f, roadTop), size = Size(w, roadH))
    drawRect(color = Color(0xFFD2DCE8), topLeft = Offset(0f, roadTop), size = Size(w, 5.dp.toPx()))
    // Dashed road line
    var rx = 12.dp.toPx()
    while (rx < w) {
      drawRoundRect(
        color = Color.White.copy(alpha = 0.7f),
        topLeft = Offset(rx, roadTop + roadH * 0.5f),
        size = Size(24.dp.toPx(), 4.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx()),
      )
      rx += 44.dp.toPx()
    }

    // Parked Yellow School Bus Sprite on the Street
    val busW = 166.dp.toPx()
    val busH = busW * (homeBusBitmap.height.toFloat() / homeBusBitmap.width.toFloat())
    val busLeft = w * 0.13f
    val busTop = roadTop + roadH * 0.08f
    drawOval(
      color = Color.Black.copy(alpha = 0.28f),
      topLeft = Offset(busLeft + 10.dp.toPx(), busTop + busH - 12.dp.toPx()),
      size = Size(busW - 20.dp.toPx(), 16.dp.toPx()),
    )
    drawImage(
      image = homeBusBitmap,
      dstOffset = IntOffset(busLeft.roundToInt(), busTop.roundToInt()),
      dstSize = IntSize(busW.roundToInt(), busH.roundToInt()),
    )

    // Bus Stop Sign
    val signX = w * 0.68f
    val signBaseY = roadTop + 4.dp.toPx()
    drawLine(
      color = OutlineInk,
      start = Offset(signX, signBaseY),
      end = Offset(signX, signBaseY - 48.dp.toPx()),
      strokeWidth = 4.dp.toPx(),
    )
    drawCircle(color = HudBlue, radius = 14.dp.toPx(), center = Offset(signX, signBaseY - 54.dp.toPx()))
    drawCircle(
      color = OutlineInk,
      radius = 14.dp.toPx(),
      center = Offset(signX, signBaseY - 54.dp.toPx()),
      style = Stroke(width = 2.dp.toPx()),
    )

    // Cheerful Bouncing Toy Passengers waiting at Bus Stop
    val waitingColors = listOf(VehicleColor.RED, VehicleColor.CYAN, VehicleColor.GREEN, VehicleColor.YELLOW)
    waitingColors.forEachIndexed { idx, vc ->
      val bounce = abs(sin((phase * 2 * PI + idx * 0.9)).toFloat()) * 6.dp.toPx()
      drawToySprite(
        sprite = toySprites[vc],
        center = Offset(w * 0.65f + idx * 24.dp.toPx(), roadTop + roadH * 0.36f - bounce),
        radius = 11.dp.toPx(),
        color = vc,
      )
    }
  }
}

private fun DrawScope.drawCloud(center: Offset, r: Float) {
  drawCircle(color = Color.White.copy(alpha = 0.92f), radius = r, center = center)
  drawCircle(color = Color.White.copy(alpha = 0.92f), radius = r * 0.78f, center = Offset(center.x - r * 0.85f, center.y + r * 0.18f))
  drawCircle(color = Color.White.copy(alpha = 0.92f), radius = r * 0.72f, center = Offset(center.x + r * 0.85f, center.y + r * 0.22f))
}

private fun DrawScope.drawBeachCottage(centerX: Float, baseY: Float) {
  val houseW = 148.dp.toPx()
  val houseH = 120.dp.toPx()
  val left = centerX - houseW * 0.5f
  val top = baseY - houseH

  // Main two-storey teal body
  drawRoundRect(
    color = CottageTeal,
    topLeft = Offset(left, top + 28.dp.toPx()),
    size = Size(houseW, houseH - 28.dp.toPx()),
    cornerRadius = CornerRadius(10.dp.toPx()),
  )
  drawRoundRect(
    color = OutlineInk,
    topLeft = Offset(left, top + 28.dp.toPx()),
    size = Size(houseW, houseH - 28.dp.toPx()),
    cornerRadius = CornerRadius(10.dp.toPx()),
    style = Stroke(width = 2.5.dp.toPx()),
  )

  // Pitched coral roof
  val roof =
    Path().apply {
      moveTo(left - 12.dp.toPx(), top + 34.dp.toPx())
      lineTo(centerX, top - 8.dp.toPx())
      lineTo(left + houseW + 12.dp.toPx(), top + 34.dp.toPx())
      close()
    }
  drawPath(roof, color = CottageRoof)
  drawPath(roof, color = OutlineInk, style = Stroke(width = 2.5.dp.toPx()))

  // Upstairs windows
  for (wx in listOf(left + 22.dp.toPx(), left + houseW - 48.dp.toPx())) {
    drawRoundRect(
      color = Color(0xFFFFF4B8),
      topLeft = Offset(wx, top + 44.dp.toPx()),
      size = Size(26.dp.toPx(), 22.dp.toPx()),
      cornerRadius = CornerRadius(5.dp.toPx()),
    )
    drawRoundRect(
      color = OutlineInk,
      topLeft = Offset(wx, top + 44.dp.toPx()),
      size = Size(26.dp.toPx(), 22.dp.toPx()),
      cornerRadius = CornerRadius(5.dp.toPx()),
      style = Stroke(width = 2.dp.toPx()),
    )
  }

  // Balcony stripe
  drawRoundRect(
    color = Color.White,
    topLeft = Offset(left + 10.dp.toPx(), top + 74.dp.toPx()),
    size = Size(houseW - 20.dp.toPx(), 6.dp.toPx()),
    cornerRadius = CornerRadius(3.dp.toPx()),
  )

  // Front Door
  drawRoundRect(
    color = Color(0xFF9C5328),
    topLeft = Offset(centerX - 14.dp.toPx(), baseY - 34.dp.toPx()),
    size = Size(28.dp.toPx(), 34.dp.toPx()),
    cornerRadius = CornerRadius(6.dp.toPx()),
  )
  drawRoundRect(
    color = OutlineInk,
    topLeft = Offset(centerX - 14.dp.toPx(), baseY - 34.dp.toPx()),
    size = Size(28.dp.toPx(), 34.dp.toPx()),
    cornerRadius = CornerRadius(6.dp.toPx()),
    style = Stroke(width = 2.dp.toPx()),
  )
}

private fun DrawScope.drawPalmTree(base: Offset, scale: Float, swayDeg: Float) {
  val trunkH = 90.dp.toPx() * scale
  rotate(swayDeg, pivot = base) {
    drawLine(
      color = Color(0xFF8C5A32),
      start = base,
      end = Offset(base.x + 8.dp.toPx() * scale, base.y - trunkH),
      strokeWidth = 11.dp.toPx() * scale,
      cap = StrokeCap.Round,
    )
    val crown = Offset(base.x + 8.dp.toPx() * scale, base.y - trunkH)
    for (angle in listOf(-140f, -65f, -15f, 35f, 155f)) {
      rotate(angle, pivot = crown) {
        drawOval(
          color = Color(0xFF2CB54B),
          topLeft = Offset(crown.x, crown.y - 8.dp.toPx() * scale),
          size = Size(44.dp.toPx() * scale, 16.dp.toPx() * scale),
        )
      }
    }
  }
}
