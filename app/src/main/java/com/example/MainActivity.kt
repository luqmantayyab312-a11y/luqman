package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.BusJamTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BusJamTheme {
        val gameViewModel: GameViewModel = viewModel()
        val progress by gameViewModel.progress.collectAsStateWithLifecycle()
        val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
        var inGame by rememberSaveable { mutableStateOf(false) }

        Crossfade(targetState = inGame, label = "screen_transition") { playing ->
          if (!playing) {
            HomeScreen(
              progress = progress,
              shopNotice = uiState.shopNotice,
              onPlayLevel = { level ->
                gameViewModel.soundEngine.playClick()
                gameViewModel.startLevel(level)
                inGame = true
              },
              onToggleSound = { gameViewModel.toggleSound(it) },
              onToggleVibration = { gameViewModel.toggleVibration(it) },
              onBuyBooster = { gameViewModel.buyBoosterFromShop(it) },
              onClaimFreeCoins = { gameViewModel.addBonusCoins(100) },
              onClaimStarterPack = { gameViewModel.claimStarterPack() },
              onUnlockNoAds = { gameViewModel.unlockNoAds() },
              onResetToLevel1 = {
                gameViewModel.startLevel(1)
                inGame = true
              },
            )
          } else {
            GameScreen(
              uiState = uiState,
              viewModel = gameViewModel,
              onNavigateHome = {
                gameViewModel.soundEngine.playClick()
                inGame = false
              },
            )
          }
        }
      }
    }
  }
}
