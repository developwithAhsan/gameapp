package com.example.game

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Real Ad Unit IDs provided by the user
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-1152982626113459/4188585655"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1152982626113459/1382989144"
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-1152982626113459/7581035752"
    private const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1152982626113459/8948968068"
    private const val NATIVE_AD_UNIT_ID = "ca-app-pub-1152982626113459/8375242677"
    private const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-1152982626113459/6925439956"

    // Fallback official test IDs if real IDs are running in dry/test mode to guarantee loading
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"

    // Use AdMob Test IDs automatically in debug builds (e.g., emulator/preview) to guarantee fast loads and 100% fill
    var useTestIds = com.example.BuildConfig.DEBUG

    fun getBannerId() = if (useTestIds) TEST_BANNER else BANNER_AD_UNIT_ID
    fun getInterstitialId() = if (useTestIds) TEST_INTERSTITIAL else INTERSTITIAL_AD_UNIT_ID
    fun getRewardedId() = if (useTestIds) TEST_REWARDED else REWARDED_AD_UNIT_ID
    fun getRewardedInterstitialId() = if (useTestIds) TEST_REWARDED_INTERSTITIAL else REWARDED_INTERSTITIAL_AD_UNIT_ID
    fun getNativeId() = if (useTestIds) TEST_NATIVE else NATIVE_AD_UNIT_ID
    fun getAppOpenId() = if (useTestIds) TEST_APP_OPEN else APP_OPEN_AD_UNIT_ID

    // Loaded Ads References
    private var mAppOpenAd: AppOpenAd? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null
    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null
    private var loadedNativeAd: NativeAd? = null

    // Timed and Counter tracking
    private var lastAdShowTimeMs = 0L
    private const val AD_MIN_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes in milliseconds

    var onNativeAdLoaded: ((NativeAd) -> Unit)? = null

    /**
     * Initializes AdMob, triggers initial preloading.
     */
    fun init(context: Context) {
        Log.d(TAG, "Initializing Google Mobile Ads SDK...")
        lastAdShowTimeMs = System.currentTimeMillis() // initialize on launch
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "MobileAds initialized: $status")
            // Load Ads early in the background
            preloadAllAds(context.applicationContext)
        }
    }

    fun preloadAllAds(context: Context) {
        loadAppOpenAd(context)
        loadInterstitial(context)
        loadRewarded(context)
        loadRewardedInterstitial(context)
        loadNativeAd(context)
    }

    /**
     * Tracks a click or action, displaying an ad periodically.
     */
    fun recordClickValue(activity: Activity) {
        val currentTime = System.currentTimeMillis()
        if (lastAdShowTimeMs == 0L) {
            lastAdShowTimeMs = currentTime
            return
        }
        val elapsed = currentTime - lastAdShowTimeMs
        Log.d(TAG, "Time elapsed since last interstitial: ${elapsed / 1000} seconds")
        if (elapsed >= AD_MIN_INTERVAL_MS) {
            showInterstitial(activity) {
                Log.d(TAG, "Interstitial completed after 5 minutes minimum interval")
            }
        }
    }

    // ==========================================
    // 1. BANNER AD CONFIGURATION
    // ==========================================
    @Composable
    fun BannerAdView(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = getBannerId()
                    loadAd(AdRequest.Builder().build())
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "Banner ad loaded successfully")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(TAG, "Banner ad failed to load: ${error.message}. Retrying with test ID.")
                            if (!useTestIds) {
                                useTestIds = true
                                adUnitId = getBannerId()
                                loadAd(AdRequest.Builder().build())
                            }
                        }
                    }
                }
            }
        )
    }

    // ==========================================
    // 2. INTERSTITIAL AD CONFIGURATION
    // ==========================================
    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            getInterstitialId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad loaded successfully")
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    mInterstitialAd = null
                    // If failed and not using test ids, retry once with test ID
                    if (!useTestIds) {
                        useTestIds = true
                        loadInterstitial(context)
                    }
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = mInterstitialAd
        if (ad != null) {
            lastAdShowTimeMs = System.currentTimeMillis() // Reset timer early
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    mInterstitialAd = null
                    lastAdShowTimeMs = System.currentTimeMillis() // Reset timer on dismiss
                    loadInterstitial(activity.applicationContext) // Reload for next use
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad null or not loaded yet. Loading in background.")
            loadInterstitial(activity.applicationContext)
            onAdDismissed()
        }
    }

    // ==========================================
    // 3. REWARDED AD CONFIGURATION
    // ==========================================
    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            getRewardedId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad loaded successfully")
                    mRewardedAd = rewardedAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    mRewardedAd = null
                    if (!useTestIds) {
                        useTestIds = true
                        loadRewarded(context)
                    }
                }
            }
        )
    }

    fun showRewarded(activity: Activity, onRewardEarned: () -> Unit) {
        val ad = mRewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    mRewardedAd = null
                    loadRewarded(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    mRewardedAd = null
                    loadRewarded(activity.applicationContext)
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User watched rewarded ad. Amount: ${rewardItem.amount}")
                onRewardEarned()
            }
        } else {
            Toast.makeText(activity, "Loaded ads are preparing, please try again!", Toast.LENGTH_SHORT).show()
            loadRewarded(activity.applicationContext)
        }
    }

    // ==========================================
    // 4. REWARDED INTERSTITIAL CONFIGURATION
    // ==========================================
    fun loadRewardedInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            getRewardedInterstitialId(),
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    Log.d(TAG, "Rewarded Interstitial format loaded successfully")
                    mRewardedInterstitialAd = rewardedInterstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Rewarded Interstitial ad failing to load: ${loadAdError.message}")
                    mRewardedInterstitialAd = null
                    if (!useTestIds) {
                        useTestIds = true
                        loadRewardedInterstitial(context)
                    }
                }
            }
        )
    }

    fun showRewardedInterstitial(activity: Activity, onRewardEarned: () -> Unit) {
        val ad = mRewardedInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Interstitial dismissed")
                    mRewardedInterstitialAd = null
                    loadRewardedInterstitial(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded Interstitial failed to show: ${adError.message}")
                    mRewardedInterstitialAd = null
                    loadRewardedInterstitial(activity.applicationContext)
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User rewarded interstitial earned: ${rewardItem.amount}")
                onRewardEarned()
            }
        } else {
            Toast.makeText(activity, "Preloading Reward Ads...", Toast.LENGTH_SHORT).show()
            loadRewardedInterstitial(activity.applicationContext)
        }
    }

    // ==========================================
    // 5. NATIVE ADVANCED AD CONFIGURATION
    // ==========================================
    fun loadNativeAd(context: Context) {
        val adLoader = AdLoader.Builder(context, getNativeId())
            .forNativeAd { nativeAd ->
                Log.d(TAG, "Native Ad loaded successfully")
                loadedNativeAd = nativeAd
                onNativeAdLoaded?.invoke(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad failed to load: ${error.message}")
                    if (!useTestIds) {
                        useTestIds = true
                        loadNativeAd(context)
                    }
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    /**
     * Composable Native Ad container creating custom elegant UI structure for displaying the Ad content completely programmatically.
     */
    @Composable
    fun NativeAdCard(modifier: Modifier = Modifier) {
        var nativeAdState by remember { mutableStateOf<NativeAd?>(loadedNativeAd) }

        DisposableEffect(Unit) {
            val listener = { ad: NativeAd ->
                nativeAdState = ad
            }
            onNativeAdLoaded = listener
            onDispose {
                if (onNativeAdLoaded == listener) {
                    onNativeAdLoaded = null
                }
            }
        }

        val ad = nativeAdState
        if (ad == null) {
            // Placeholder loading badge styled with slate
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Color(0x15FFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AD PROGRAM SPONSOR",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
            return
        }

        // Render Native Ad programmatically using NativeAdView so it supports compliance clicks
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                val nativeAdView = NativeAdView(context)

                val container = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16, 16, 16, 16)
                    val bgDrawable = android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.parseColor("#1E293B")) // Slate 800-ish
                        cornerRadius = 24f
                        setStroke(2, android.graphics.Color.parseColor("#3322D3EE")) // Neon cyan accent border
                    }
                    background = bgDrawable
                }

                // Row for Icon, Title and Ad Label
                val headerRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Ad Badge Indicator
                val adTag = TextView(context).apply {
                    text = "Ad"
                    setTextColor(android.graphics.Color.parseColor("#06B6D4")) // Cyan text
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    textSize = 12f
                    setPadding(10, 2, 10, 2)
                    val tagBg = android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.parseColor("#1500C6FF"))
                        cornerRadius = 8f
                    }
                    background = tagBg
                }
                headerRow.addView(adTag)

                // Title
                val titleView = TextView(context).apply {
                    text = ad.headline
                    setTextColor(android.graphics.Color.WHITE)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    textSize = 15f
                    setPadding(16, 0, 0, 0)
                }
                headerRow.addView(titleView)
                container.addView(headerRow)

                // MediaView/Image (large asset if available) - wrap in FrameLayout
                if (ad.mediaContent != null) {
                    val mediaFrame = FrameLayout(context).apply {
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 320
                        )
                        params.setMargins(0, 12, 0, 12)
                        layoutParams = params
                    }
                    val mediaView = MediaView(context).apply {
                        setMediaContent(ad.mediaContent!!)
                    }
                    mediaFrame.addView(mediaView)
                    container.addView(mediaFrame)
                    nativeAdView.mediaView = mediaView
                }

                // Body text
                val bodyView = TextView(context).apply {
                    text = ad.body
                    setTextColor(android.graphics.Color.parseColor("#94A3B8")) // Slate-400
                    textSize = 12f
                    setPadding(0, 8, 0, 12)
                }
                container.addView(bodyView)
                nativeAdView.bodyView = bodyView

                // CTA (Call to action button)
                val ctaButton = Button(context).apply {
                    text = ad.callToAction ?: "LEARN MORE"
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 13f
                    val btnBg = android.graphics.drawable.GradientDrawable().apply {
                        setColor(android.graphics.Color.parseColor("#06B6D4")) // Cyan 500
                        cornerRadius = 16f
                    }
                    background = btnBg
                    isClickable = false // Let NativeAdView handle the click
                }
                container.addView(ctaButton)
                nativeAdView.callToActionView = ctaButton

                nativeAdView.addView(container)
                nativeAdView.setNativeAd(ad)

                nativeAdView
            }
        )
    }

    // ==========================================
    // 6. APP OPEN AD CONFIGURATION
    // ==========================================
    fun loadAppOpenAd(context: Context) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            getAppOpenId(),
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    Log.d(TAG, "App Open Ad loaded successfully")
                    mAppOpenAd = appOpenAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    mAppOpenAd = null
                    if (!useTestIds) {
                        useTestIds = true
                        loadAppOpenAd(context)
                    }
                }
            }
        )
    }

    fun showAppOpenIfAvailable(activity: Activity) {
        val ad = mAppOpenAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App Open dismissed")
                    mAppOpenAd = null
                    loadAppOpenAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "App Open failed to show: ${adError.message}")
                    mAppOpenAd = null
                    loadAppOpenAd(activity.applicationContext)
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "App Open Ad is not loaded yet. Loading in background.")
            loadAppOpenAd(activity.applicationContext)
        }
    }
}
