package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BoosterType
import com.example.game.GameOutcome
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.components.BoosterPurchaseDialog
import com.example.ui.components.CoinPill
import com.example.ui.components.ConveyorTrackView
import com.example.ui.components.HelpDialog
import com.example.ui.components.LoseLevelDialog
import com.example.ui.components.ParkingLotCanvas
import com.example.ui.components.PauseDialog
import com.example.ui.components.PlayfieldEffectsOverlay
import com.example.ui.components.ShopDialog
import com.example.ui.components.UnlockSlotDialog
import com.example.ui.components.WaitingSlotsRow
import com.example.ui.components.WinLevelDialog
import com.example.ui.theme.AlertRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldCoinDark
import com.example.ui.theme.HudBlue
import com.example.ui.theme.HudBlueDark
import com.example.ui.theme.OutlineInk
import com.example.ui.theme.PlayGreen
import com.example.ui.theme.PlayGreenDark
import com.example.ui.theme.ShopPurple
import com.example.ui.theme.ShopPurpleDark

@Composable
fun GameScreen(
  uiState: GameUiState,
  viewModel: GameViewModel,
  onNavigateHome: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler {
    when {
      uiState.showHelpDialog -> viewModel.toggleHelp(false)
      uiState.showMainShopDialog -> viewModel.toggleMainShop(false)
      uiState.activeBoosterShop != null -> viewModel.dismissBoosterShop()
      uiState.activeSlotUnlockIndex != null -> viewModel.dismissSlotUnlock()
      uiState.outcome == GameOutcome.PLAYING -> viewModel.pauseGame()
      else -> onNavigateHome()
    }
  }

  // Smooth 60fps simulation ticker
  LaunchedEffect(uiState.outcome, uiState.levelNumber) {
    if (uiState.outcome != GameOutcome.PLAYING) return@LaunchedEffect
    var lastNanos = 0L
    while (true) {
      withFrameNanos { frameNanos ->
        if (lastNanos != 0L) {
          val dt = (frameNanos - lastNanos) / 1_000_000_000f
          viewModel.onFrame(dt)
        }
        lastNanos = frameNanos
      }
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val screenH = maxHeight

    // 1. Exact full-bleed gameplay background provided by the user
    Image(
      painter = painterResource(id = R.drawable.game_play_bg),
      contentDescription = null,
      contentScale = ContentScale.FillBounds,
      modifier = Modifier.fillMaxSize(),
    )

    // 2. ZONE 1: Passengers circulating along the background's oval road loop & left ramp
    ConveyorTrackView(
      passengers = uiState.passengers,
      conveyorOffsetPx = uiState.conveyorOffsetPx,
      remainingCount = uiState.passengers.size,
      modifier = Modifier.fillMaxSize(),
    )

    // 3. WAITING BAYS: 8 tilted bays aligned 1-to-1 with the background stand slots
    WaitingSlotsRow(
      slots = uiState.slots,
      vehicles = uiState.vehicles,
      onSlotTap = { viewModel.onSlotTapped(it) },
      modifier = Modifier.fillMaxSize(),
    )

    // 4. ZONE 2: Middle Parking Lot + Tunnels + Vehicles positioned inside the grey asphalt area
    Box(
      modifier =
        Modifier
          .offset(y = screenH * 0.436f)
          .fillMaxWidth()
          .height(screenH * 0.434f)
    ) {
      ParkingLotCanvas(
        gridCols = uiState.gridCols,
        gridRows = uiState.gridRows,
        vehicles = uiState.vehicles,
        tunnels = uiState.tunnels,
        tutorialVehicleId = uiState.tutorialVehicleId,
        onLotTap = { gx, gy -> viewModel.onLotTap(gx, gy) },
        modifier = Modifier.fillMaxSize(),
      )

      // Tutorial or status banner floating gently at top of parking lot
      val banner = uiState.statusBannerText ?: if (uiState.isTutorial) uiState.tutorialHintText else null
      AnimatedVisibility(
        visible = banner != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
      ) {
        if (banner != null) {
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.94f))
                .border(2.dp, OutlineInk, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
          ) {
            Text(
              text = banner,
              style = MaterialTheme.typography.labelSmall.copy(fontFamily = FredokaFontFamily, fontSize = 12.sp),
              color = OutlineInk,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    }

    // 5. TOP HUD: Coins (left) + Glossy Level N Banner (center) + Pause (right, replacing '?')
    Row(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .windowInsetsPadding(WindowInsets.statusBars)
          .widthIn(max = 560.dp)
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CoinPill(
        coins = uiState.coins,
        onAddClick = { viewModel.toggleMainShop(true) },
      )

      Box(
        modifier =
          Modifier
            .width(128.dp)
            .height(48.dp)
            .testTag("level_banner"),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(id = R.drawable.btn_hud_level),
          contentDescription = "Level ${uiState.levelNumber}",
          contentScale = ContentScale.FillBounds,
          modifier = Modifier.fillMaxSize(),
        )
        Text(
          text = "Level ${uiState.levelNumber}",
          style =
            MaterialTheme.typography.titleMedium.copy(
              fontFamily = FredokaFontFamily,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              shadow = Shadow(color = Color(0xFF0A4A8A), offset = Offset(0f, 3f), blurRadius = 2f),
            ),
          color = Color.White,
          maxLines = 1,
        )
      }

      Box(
        modifier =
          Modifier
            .size(48.dp)
            .clickable { viewModel.pauseGame() }
            .testTag("pause_button"),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(id = R.drawable.btn_hud_pause),
          contentDescription = "Pause",
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

    // 6. ZONE 3: Bottom Booster Bar (Board, Shuffle, VIP Slot)
    Row(
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .windowInsetsPadding(WindowInsets.navigationBars)
          .widthIn(max = 560.dp)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BoosterButton(
        title = "Board",
        icon = Icons.Filled.DirectionsRun,
        ownedCount = uiState.boardBoosters,
        topColor = PlayGreen,
        bottomColor = PlayGreenDark,
        onClick = { viewModel.onBoosterTapped(BoosterType.BOARD) },
        modifier = Modifier.weight(1f),
        testTag = "booster_board",
      )
      BoosterButton(
        title = "Shuffle",
        icon = Icons.Filled.Shuffle,
        ownedCount = uiState.refreshBoosters,
        topColor = HudBlue,
        bottomColor = HudBlueDark,
        onClick = { viewModel.onBoosterTapped(BoosterType.REFRESH) },
        modifier = Modifier.weight(1f),
        testTag = "booster_refresh",
      )
      BoosterButton(
        title = "VIP Slot",
        icon = Icons.Filled.LocalParking,
        ownedCount = uiState.vipBoosters,
        topColor = ShopPurple,
        bottomColor = ShopPurpleDark,
        onClick = { viewModel.onBoosterTapped(BoosterType.VIP_SLOT) },
        modifier = Modifier.weight(1f),
        testTag = "booster_vip",
      )
    }

    // 7. Full-bleed Playfield Effects Overlay (Driving buses + dotted trail, jumping stickmen, confetti, "+1")
    PlayfieldEffectsOverlay(
      vehicles = uiState.vehicles,
      gridCols = uiState.gridCols,
      gridRows = uiState.gridRows,
      jumpingPassengers = uiState.jumpingPassengers,
      particles = uiState.particles,
      floatingTexts = uiState.floatingTexts,
      totalSlots = uiState.slots.size,
      modifier = Modifier.fillMaxSize(),
    )
  }

  // Modal Dialogs
  if (uiState.outcome == GameOutcome.WON) {
    WinLevelDialog(
      levelNumber = uiState.levelNumber,
      stars = uiState.starsEarned,
      onNextLevel = { viewModel.nextLevel() },
      onHome = onNavigateHome,
    )
  }

  if (uiState.outcome == GameOutcome.LOST) {
    LoseLevelDialog(
      coins = uiState.coins,
      notice = uiState.shopNotice,
      onRetry = { viewModel.restartLevel() },
      onRevive = { viewModel.reviveOnLose() },
      onHome = onNavigateHome,
    )
  }

  if (uiState.outcome == GameOutcome.PAUSED) {
    PauseDialog(
      soundEnabled = uiState.soundEnabled,
      vibrationEnabled = uiState.vibrationEnabled,
      onToggleSound = { viewModel.toggleSound(it) },
      onToggleVibration = { viewModel.toggleVibration(it) },
      onResume = { viewModel.resumeGame() },
      onRestart = { viewModel.restartLevel() },
      onHome = onNavigateHome,
    )
  }

  if (uiState.showHelpDialog) {
    HelpDialog(onDismiss = { viewModel.toggleHelp(false) })
  }

  val activeBooster = uiState.activeBoosterShop
  if (activeBooster != null) {
    BoosterPurchaseDialog(
      booster = activeBooster,
      coins = uiState.coins,
      notice = uiState.shopNotice,
      onBuy = { viewModel.buyAndUseBooster(activeBooster) },
      onDismiss = { viewModel.dismissBoosterShop() },
    )
  }

  val unlockSlotIdx = uiState.activeSlotUnlockIndex
  if (unlockSlotIdx != null) {
    UnlockSlotDialog(
      coins = uiState.coins,
      notice = uiState.shopNotice,
      onConfirm = { viewModel.confirmUnlockSlot(unlockSlotIdx) },
      onDismiss = { viewModel.dismissSlotUnlock() },
    )
  }

  if (uiState.showMainShopDialog) {
    ShopDialog(
      coins = uiState.coins,
      boardCount = uiState.boardBoosters,
      refreshCount = uiState.refreshBoosters,
      vipCount = uiState.vipBoosters,
      notice = uiState.shopNotice,
      onBuyBooster = { viewModel.buyBoosterFromShop(it) },
      onClaimFreeCoins = { viewModel.addBonusCoins(100) },
      onDismiss = { viewModel.toggleMainShop(false) },
    )
  }
}

@Composable
private fun BoosterButton(
  title: String,
  icon: ImageVector,
  ownedCount: Int,
  topColor: Color,
  bottomColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  Box(modifier = modifier) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(54.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(bottomColor)
          .padding(bottom = 4.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(topColor)
          .border(2.2.dp, OutlineInk, RoundedCornerShape(14.dp))
          .clickable(onClick = onClick)
          .padding(horizontal = 8.dp)
          .testTag(testTag),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(5.dp))
      Column(horizontalAlignment = Alignment.Start) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge.copy(fontFamily = FredokaFontFamily, fontSize = 13.sp),
          color = Color.White,
          maxLines = 1,
        )
        Text(
          text = if (ownedCount > 0) "x$ownedCount" else "200",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          color = Color.White.copy(alpha = 0.9f),
        )
      }
    }

    // Red '+' badge when ownedCount == 0, or green count pill when > 0
    Box(
      modifier =
        Modifier
          .align(Alignment.TopEnd)
          .size(22.dp)
          .clip(CircleShape)
          .background(if (ownedCount == 0) AlertRed else GoldCoinDark)
          .border(1.8.dp, Color.White, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = if (ownedCount == 0) "+" else ownedCount.toString(),
        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FredokaFontFamily, fontSize = 12.sp),
        color = Color.White,
      )
    }
  }
}
