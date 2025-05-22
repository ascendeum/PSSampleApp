package com.ascendeum.pbjsservertestapp

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.PrebidMobile

class MainActivity : ComponentActivity() {
    private lateinit var adContainer: FrameLayout
    private lateinit var adView: AdManagerAdView
    private var bannerUnit: BannerAdUnit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupAd()
    }

    private fun setupAd() {
        Log.d("PS-Sample", "Initializing ad components")
        // 1. Initialize GAM AdView
        adView = AdManagerAdView(this).apply {
            adUnitId = "/1006418/theChive_Android_Latest_300x250_Instream"
            setAdSizes(AdSize(300, 250))
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d("PS-Sample", "Ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("PS-Sample", "Ad failed to load: ${error.message}")
                }
            }
        }

        // 2. Add GAM AdView to layout
        adContainer = findViewById(R.id.ad_view_container)
        adContainer.addView(adView)

        // 3. Create and configure Prebid BannerAdUnit
        bannerUnit = BannerAdUnit("22178-imp-Chive_Android_S2S-Latest_300x250_Instream", 300, 250).apply {
            // Start auto-refresh every 30 seconds
            setAutoRefreshPeriodMillis(30000)
        }

        // 4. Fetch demand and load GAM ad
        loadPrebidAd()
    }

    private fun loadPrebidAd() {
        val adRequestBuilder = AdManagerAdRequest.Builder()

        bannerUnit?.fetchDemand(adRequestBuilder) { resultCode ->
            Log.d("PS-Sample", "Prebid fetchDemand result: $resultCode")
            adView.loadAd(adRequestBuilder.build())
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop refresh when app goes to background
        bannerUnit?.stopAutoRefersh()
        Log.d("PS-Sample", "Auto-refresh stopped")
    }

    override fun onResume() {
        super.onResume()
        // Resume refresh when app comes to foreground
        bannerUnit?.setAutoRefreshPeriodMillis(30000)
        Log.d("PS-Sample", "Auto-refresh restarted")
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerUnit?.stopAutoRefersh()
        bannerUnit = null
        adView.destroy()
        Log.d("PS-Sample", "Banner destroyed and auto-refresh stopped")
    }
}
