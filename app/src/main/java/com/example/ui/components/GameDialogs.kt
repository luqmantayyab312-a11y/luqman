package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.BoosterType
import com.example.ui.theme.AlertRed
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.GoldCoinDark
import com.example.ui.theme.HudBlue
import com.example.ui.theme.HudBlueDark
import com.example.ui.theme.OutlineInk
import com.example.ui.theme.PlayGreen
import com.example.ui.theme.PlayGreenDark
import com.example.ui.theme.ShopPurple
import com.example.ui.theme.ShopPurpleDark

@Composable
fun GlossyButton(
  text: String,
  topColor: Color,
  bottomColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  testTag: String? = null,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring(), label = "btn_scale")

  Box(
    modifier =
      modifier
        .scale(scale)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .clip(RoundedCornerShape(18.dp))
        .background(bottomColor)
        .padding(bottom = 5.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(topColor)
        .border(2.5.dp, OutlineInk, RoundedCornerShape(16.dp))
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
        .padding(horizontal = 18.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
      if (icon != null) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontFamily = FredokaFontFamily, fontSize = 16.sp),
        color = Color.White,
        maxLines = 1,
      )
    }
  }
}

@Composable
fun CoinPill(
  coins: Int,
  onAddClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .width(124.dp)
        .height(48.dp)
        .clickable(onClick = onAddClick)
        .testTag("coin_pill"),
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.btn_hud_coin),
      contentDescription = "Coins: $coins",
      contentScale = ContentScale.Fit,
      modifier = Modifier.fillMaxSize(),
    )
    Text(
      text = coins.toString(),
      style =
        MaterialTheme.typography.labelLarge.copy(
          fontFamily = FredokaFontFamily,
          fontSize = if (coins >= 1000) 13.sp else 16.sp,
          fontWeight = FontWeight.Bold,
          shadow = Shadow(color = OutlineInk, offset = Offset(0f, 2f), blurRadius = 2f),
        ),
      color = Color.White,
      maxLines = 1,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp),
    )
  }
}

@Composable
fun CartoonPopupCard(
  title: String,
  headerColor: Color = HudBlue,
  onDismiss: () -> Unit,
  content: @Composable () -> Unit,
) {
  Dialog(onDismissRequest = onDismiss) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(26.dp))
          .background(Color.White)
          .border(3.dp, OutlineInk, RoundedCornerShape(26.dp)),
    ) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .background(headerColor)
            .padding(vertical = 14.dp, horizontal = 18.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge.copy(fontFamily = FredokaFontFamily),
          color = Color.White,
          textAlign = TextAlign.Center,
        )
      }
      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        content()
      }
    }
  }
}

@Composable
fun WinLevelDialog(
  levelNumber: Int,
  stars: Int,
  onNextLevel: () -> Unit,
  onHome: () -> Unit,
) {
  CartoonPopupCard(title = "LEVEL $levelNumber COMPLETE!", headerColor = PlayGreen, onDismiss = {}) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      for (s in 1..3) {
        Icon(
          imageVector = if (s <= stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
          contentDescription = "Star $s",
          tint = GoldCoin,
          modifier = Modifier.size(44.dp),
        )
      }
    }
    Spacer(Modifier.height(14.dp))
    Box(
      modifier =
        Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(GoldCoin.copy(alpha = 0.22f))
          .border(2.dp, GoldCoinDark, RoundedCornerShape(14.dp))
          .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      Text(
        text = "+50 Coins Reward!",
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FredokaFontFamily),
        color = OutlineInk,
      )
    }
    Spacer(Modifier.height(20.dp))
    GlossyButton(
      text = "Next Level",
      topColor = PlayGreen,
      bottomColor = PlayGreenDark,
      onClick = onNextLevel,
      modifier = Modifier.fillMaxWidth(),
      testTag = "next_level_button",
    )
    Spacer(Modifier.height(10.dp))
    GlossyButton(
      text = "Home",
      topColor = HudBlue,
      bottomColor = HudBlueDark,
      onClick = onHome,
      modifier = Modifier.fillMaxWidth(),
      testTag = "win_home_button",
    )
  }
}

@Composable
fun LoseLevelDialog(
  coins: Int,
  notice: String?,
  onRetry: () -> Unit,
  onRevive: () -> Unit,
  onHome: () -> Unit,
) {
  CartoonPopupCard(title = "LEVEL FAILED", headerColor = AlertRed, onDismiss = {}) {
    Text(
      text = "All waiting slots are full and no parked bus matches the next passenger!",
      style = MaterialTheme.typography.bodyLarge,
      color = OutlineInk,
      textAlign = TextAlign.Center,
    )
    if (notice != null) {
      Spacer(Modifier.height(8.dp))
      Text(
        text = notice,
        style = MaterialTheme.typography.labelLarge,
        color = AlertRed,
      )
    }
    Spacer(Modifier.height(16.dp))
    GlossyButton(
      text = "Revive (+1 Slot) • 200",
      topColor = GoldCoin,
      bottomColor = GoldCoinDark,
      onClick = onRevive,
      modifier = Modifier.fillMaxWidth(),
      testTag = "revive_button",
    )
    Spacer(Modifier.height(10.dp))
    GlossyButton(
      text = "Retry Level",
      topColor = HudBlue,
      bottomColor = HudBlueDark,
      onClick = onRetry,
      modifier = Modifier.fillMaxWidth(),
      testTag = "retry_button",
    )
    Spacer(Modifier.height(10.dp))
    GlossyButton(
      text = "Home",
      topColor = Color(0xFF7B8BA3),
      bottomColor = Color(0xFF566376),
      onClick = onHome,
      modifier = Modifier.fillMaxWidth(),
      testTag = "lose_home_button",
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = "Your Coins: $coins",
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF5C6C84),
    )
  }
}

@Composable
fun PauseDialog(
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  onToggleSound: (Boolean) -> Unit,
  onToggleVibration: (Boolean) -> Unit,
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onHome: () -> Unit,
) {
  CartoonPopupCard(title = "PAUSED", headerColor = HudBlue, onDismiss = onResume) {
    SettingToggleRow(label = "Sound Effects", checked = soundEnabled, onCheckedChange = onToggleSound)
    Spacer(Modifier.height(8.dp))
    SettingToggleRow(label = "Vibration", checked = vibrationEnabled, onCheckedChange = onToggleVibration)
    Spacer(Modifier.height(18.dp))
    GlossyButton(
      text = "Resume",
      topColor = PlayGreen,
      bottomColor = PlayGreenDark,
      onClick = onResume,
      modifier = Modifier.fillMaxWidth(),
      testTag = "resume_button",
    )
    Spacer(Modifier.height(10.dp))
    GlossyButton(
      text = "Restart",
      topColor = GoldCoin,
      bottomColor = GoldCoinDark,
      onClick = onRestart,
      modifier = Modifier.fillMaxWidth(),
      testTag = "restart_button",
    )
    Spacer(Modifier.height(10.dp))
    GlossyButton(
      text = "Home",
      topColor = ShopPurple,
      bottomColor = ShopPurpleDark,
      onClick = onHome,
      modifier = Modifier.fillMaxWidth(),
      testTag = "pause_home_button",
    )
  }
}

@Composable
fun SettingToggleRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFFF1F6FC))
        .padding(horizontal = 14.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyLarge, color = OutlineInk)
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
  CartoonPopupCard(title = "HOW TO PLAY", headerColor = HudBlue, onDismiss = onDismiss) {
    HelpStepRow(
      step = "1",
      title = "Tap Clear Buses",
      desc = "Vehicles drive in the direction of their roof arrow. Tap unblocked vehicles to send them to the waiting bay.",
    )
    Spacer(Modifier.height(10.dp))
    HelpStepRow(
      step = "2",
      title = "Board Matching Passengers",
      desc = "Stickmen ride the top conveyor belt and jump into waiting buses of the same color.",
    )
    Spacer(Modifier.height(10.dp))
    HelpStepRow(
      step = "3",
      title = "Watch Slots & Tunnels",
      desc = "Don't fill all waiting slots with mismatched buses! Tunnels spawn extra vehicles once space opens.",
    )
    Spacer(Modifier.height(18.dp))
    GlossyButton(
      text = "Got It!",
      topColor = PlayGreen,
      bottomColor = PlayGreenDark,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
      testTag = "help_close_button",
    )
  }
}

@Composable
private fun HelpStepRow(step: String, title: String, desc: String) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFFF3F8FF))
        .padding(10.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      modifier = Modifier.size(26.dp).clip(CircleShape).background(HudBlue),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = step,
        style = MaterialTheme.typography.labelLarge.copy(fontFamily = FredokaFontFamily),
        color = Color.White,
      )
    }
    Spacer(Modifier.width(10.dp))
    Column {
      Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), color = OutlineInk)
      Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4A5870))
    }
  }
}

@Composable
fun BoosterPurchaseDialog(
  booster: BoosterType,
  coins: Int,
  notice: String?,
  onBuy: () -> Unit,
  onDismiss: () -> Unit,
) {
  val icon =
    when (booster) {
      BoosterType.BOARD -> Icons.Filled.DirectionsRun
      BoosterType.REFRESH -> Icons.Filled.Shuffle
      BoosterType.VIP_SLOT -> Icons.Filled.LocalParking
    }
  CartoonPopupCard(title = booster.title, headerColor = ShopPurple, onDismiss = onDismiss) {
    Box(
      modifier =
        Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(ShopPurple.copy(alpha = 0.14f))
          .border(2.5.dp, ShopPurple, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(icon, contentDescription = null, tint = ShopPurple, modifier = Modifier.size(34.dp))
    }
    Spacer(Modifier.height(12.dp))
    Text(
      text = booster.description,
      style = MaterialTheme.typography.bodyLarge,
      color = OutlineInk,
      textAlign = TextAlign.Center,
    )
    if (notice != null) {
      Spacer(Modifier.height(8.dp))
      Text(text = notice, style = MaterialTheme.typography.labelLarge, color = AlertRed)
    }
    Spacer(Modifier.height(16.dp))
    GlossyButton(
      text = "Get for ${booster.costCoins} Coins",
      topColor = PlayGreen,
      bottomColor = PlayGreenDark,
      onClick = onBuy,
      modifier = Modifier.fillMaxWidth(),
      testTag = "buy_booster_confirm",
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = "Your balance: $coins coins",
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF5A6A80),
    )
  }
}

@Composable
fun UnlockSlotDialog(
  coins: Int,
  notice: String?,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  CartoonPopupCard(title = "UNLOCK EXTRA SLOT", headerColor = PlayGreen, onDismiss = onDismiss) {
    Text(
      text = "Open an extra waiting slot permanently for this level to hold more buses at once!",
      style = MaterialTheme.typography.bodyLarge,
      color = OutlineInk,
      textAlign = TextAlign.Center,
    )
    if (notice != null) {
      Spacer(Modifier.height(8.dp))
      Text(text = notice, style = MaterialTheme.typography.labelLarge, color = AlertRed)
    }
    Spacer(Modifier.height(16.dp))
    GlossyButton(
      text = "Unlock • 300 Coins",
      topColor = GoldCoin,
      bottomColor = GoldCoinDark,
      onClick = onConfirm,
      modifier = Modifier.fillMaxWidth(),
      testTag = "confirm_unlock_slot",
    )
    Spacer(Modifier.height(8.dp))
    Text(text = "Your balance: $coins coins", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5A6A80))
  }
}

@Composable
fun ShopDialog(
  coins: Int,
  boardCount: Int,
  refreshCount: Int,
  vipCount: Int,
  notice: String?,
  onBuyBooster: (BoosterType) -> Unit,
  onClaimFreeCoins: () -> Unit,
  onDismiss: () -> Unit,
) {
  CartoonPopupCard(title = "BUS JAM SHOP", headerColor = ShopPurple, onDismiss = onDismiss) {
    Text(
      text = "Coins: $coins",
      style = MaterialTheme.typography.titleMedium,
      color = GoldCoinDark,
    )
    if (notice != null) {
      Spacer(Modifier.height(6.dp))
      Text(text = notice, style = MaterialTheme.typography.labelLarge, color = PlayGreenDark)
    }
    Spacer(Modifier.height(10.dp))
    ShopItemRow(BoosterType.BOARD, boardCount, onBuy = { onBuyBooster(BoosterType.BOARD) })
    Spacer(Modifier.height(8.dp))
    ShopItemRow(BoosterType.REFRESH, refreshCount, onBuy = { onBuyBooster(BoosterType.REFRESH) })
    Spacer(Modifier.height(8.dp))
    ShopItemRow(BoosterType.VIP_SLOT, vipCount, onBuy = { onBuyBooster(BoosterType.VIP_SLOT) })
    Spacer(Modifier.height(14.dp))
    GlossyButton(
      text = "Free Coin Chest (+100)",
      topColor = GoldCoin,
      bottomColor = GoldCoinDark,
      onClick = onClaimFreeCoins,
      modifier = Modifier.fillMaxWidth(),
      testTag = "claim_free_coins",
    )
    Spacer(Modifier.height(8.dp))
    GlossyButton(
      text = "Close",
      topColor = HudBlue,
      bottomColor = HudBlueDark,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ShopItemRow(
  type: BoosterType,
  owned: Int,
  onBuy: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFFF3F7FD))
        .border(1.5.dp, Color(0xFFD0DCEB), RoundedCornerShape(14.dp))
        .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = "${type.title} ($owned)", style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), color = OutlineInk)
      Text(text = "${type.costCoins} Coins", style = MaterialTheme.typography.labelSmall, color = GoldCoinDark)
    }
    Spacer(Modifier.width(8.dp))
    GlossyButton(
      text = "Buy",
      topColor = PlayGreen,
      bottomColor = PlayGreenDark,
      onClick = onBuy,
    )
  }
}

@Composable
fun SettingsDialog(
  soundEnabled: Boolean,
  vibrationEnabled: Boolean,
  onToggleSound: (Boolean) -> Unit,
  onToggleVibration: (Boolean) -> Unit,
  onResetToLevel1: () -> Unit,
  onDismiss: () -> Unit,
) {
  CartoonPopupCard(title = "SETTINGS", headerColor = HudBlue, onDismiss = onDismiss) {
    SettingToggleRow(label = "Sound Effects", checked = soundEnabled, onCheckedChange = onToggleSound)
    Spacer(Modifier.height(8.dp))
    SettingToggleRow(label = "Vibration", checked = vibrationEnabled, onCheckedChange = onToggleVibration)
    Spacer(Modifier.height(14.dp))
    GlossyButton(
      text = "Replay From Level 1",
      topColor = GoldCoin,
      bottomColor = GoldCoinDark,
      onClick = onResetToLevel1,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    GlossyButton(
      text = "Done",
      topColor = PlayGreen,
      bottomColor = PlayGreenDark,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
fun PromoModalDialog(
  title: String,
  description: String,
  buttonText: String,
  alreadyClaimed: Boolean,
  onAction: () -> Unit,
  onDismiss: () -> Unit,
) {
  CartoonPopupCard(title = title, headerColor = ShopPurple, onDismiss = onDismiss) {
    Icon(
      imageVector = Icons.Filled.DirectionsBus,
      contentDescription = null,
      tint = GoldCoinDark,
      modifier = Modifier.size(54.dp),
    )
    Spacer(Modifier.height(10.dp))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyLarge,
      color = OutlineInk,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    if (!alreadyClaimed) {
      GlossyButton(
        text = buttonText,
        topColor = PlayGreen,
        bottomColor = PlayGreenDark,
        onClick = onAction,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(10.dp))
    }
    GlossyButton(
      text = "Close",
      topColor = HudBlue,
      bottomColor = HudBlueDark,
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
