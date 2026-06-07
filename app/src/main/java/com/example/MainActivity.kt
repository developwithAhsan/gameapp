package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import com.example.game.AdManager
import com.example.game.GameViewModel
import com.example.game.MainGameApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private var isColdStart = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize AdMob Manager and pre-load Banner, Interstitial, Rewarded, and Native Ads
    AdManager.init(this)

    setContent {
      MyApplicationTheme {
        val viewModel: GameViewModel = viewModel()
        MainGameApp(viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (isColdStart) {
      isColdStart = false
      // Pre-load the ad so it is ready for the next resume event
      AdManager.loadAppOpenAd(this)
    } else {
      // Present App Open Ad on background transitions
      AdManager.showAppOpenIfAvailable(this)
    }
  }
}


