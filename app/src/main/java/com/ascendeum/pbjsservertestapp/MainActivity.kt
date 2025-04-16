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

class MainActivity : ComponentActivity() {
    private lateinit var myFrameLayout: FrameLayout
    private var bannerUnit: BannerAdUnit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createAd()
    }

    private fun createAd() {
        Log.d("PS-Sample", "createAd")

        // 1. Create BannerAdUnit
        bannerUnit = BannerAdUnit("22178-imp-Way2News-1", 300, 250)

        // 2. Configure banner parameters

        // 3. Create AdManagerAdView
        val adView = AdManagerAdView(this)
        adView.adUnitId =  "/6499/example/banner" // EXAMPLE AD UNIT
        // Request an anchored adaptive banner with a width of 360.
        adView.setAdSizes(AdSize(300,250))
        // Add GMA SDK banner view to the app UI
        myFrameLayout = findViewById(R.id.ad_view_container)
        myFrameLayout.addView(adView)

        // 4. Make a bid request to Prebid Server
        val adRequest = AdManagerAdRequest.Builder().build()
        bannerUnit?.fetchDemand(adRequest) {
            // 5. Load GAM Ad
            adView.loadAd(adRequest)
        }

        // Ad events
        adView.adListener = object : AdListener() {

            override fun onAdFailedToLoad(adError : LoadAdError) {
                Log.d("PS-Sample", "onAdFailedToLoad ${adError.message}")
            }

            override fun onAdLoaded() {
                Log.d("PS-Sample", "onAdLoaded")
            }
        }
    }
}
