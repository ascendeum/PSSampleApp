package com.ascendeum.pbjsservertestapp.Ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError

class AdManager private constructor() {

    private val myTAG = "[ASC-AdManager]"

    // Key: Prebid Config ID, Value: BannerAdUnit
    // This map ensures the AdUnit stays in memory for auto-refresh to work
    private val activeAdUnits = mutableMapOf<String, BannerAdUnit>()

    companion object {
        val instance: AdManager by lazy { AdManager() }
    }

    fun getAd(
        context: Context,
        placement: AdPlacement,
        onAdLoaded: (AdManagerAdView) -> Unit
    ) {
        val config = placement.config
        Log.d(myTAG, "Fetching ad for: $config")

        // 1. Initialize GAM AdView
        val adView = AdManagerAdView(context).apply {
            adUnitId = config.gamID
            setAdSizes(*config.sizes.toTypedArray())
        }

        // 2. Initialize and Store Prebid BannerAdUnit
        val bannerUnit = BannerAdUnit(config.prebidID, config.primaryWidth, config.primaryHeight)
        bannerUnit.setAutoRefreshInterval(30) // 30 seconds

//         OPTIONAL - Impression-level ORTB config
//         Prebid SDK allows the customization of the OpenRTB request on the impression level using the setImpORTBConfig()
//         The parameter passed to setImpOrtbConfig() will be merged into the respective imp object for this Ad Unit.
//         the below example will add the $.imp[0].bidfloor and $.imp[0].banner.battr parameters to the bid request.
//        bannerUnit?.impOrtbConfig = "{" +
//                "  \"bidfloor\": 0.01," +
//                "  \"banner\": {" +
//                "    \"battr\": [1,2,3,4]" +
//                "  }" +
//                "}";
//         To empty out a previously provided impression config, just set it to the empty string:
//         bannerUnit?.setImpOrtbConfig("")

        activeAdUnits[config.prebidID] = bannerUnit

        // 3. Set up listeners for creative size detection
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        Log.d(myTAG, "Prebid creative size detected: ${width}x${height}")
                    }
                    override fun failure(error: PbFindSizeError) {
                        // Error 240 is common. It just means a standard GAM ad loaded, not a Prebid one.
                        // No action is needed here because GAM handles the standard sizes automatically.
                        Log.d(myTAG, "Standard GAM ad detected (No resize needed).")
                    }
                })
                onAdLoaded(adView)
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(myTAG, "failed : ${error.message}")
            }
        }

        // 4. Fetch Demand and Load
        val requestBuilder = AdManagerAdRequest.Builder()
        bannerUnit.fetchDemand(requestBuilder) { resultCode ->
            Log.d(myTAG, "Prebid fetchDemand result: $resultCode")
            Log.d(myTAG, "Custom KV : ${requestBuilder.build().customTargeting}")
            adView.loadAd(requestBuilder.build())
        }
    }

    // Stop refresh when app goes to background
    fun pauseAd(placement: AdPlacement) {
        val key = placement.config.prebidID
        activeAdUnits[key]?.stopAutoRefresh()
        Log.d(myTAG, "Paused refresh for: $key")
    }

    // Resume refresh when app returns to foreground
    fun resumeAd(placement: AdPlacement) {
        val key = placement.config.prebidID
        activeAdUnits[key]?.resumeAutoRefresh()
        Log.d(myTAG, "Resumed refresh for: $key")
    }

    fun destroyAd(placement: AdPlacement) {
        Log.d(myTAG,"destroyAd")
        val key = placement.config.prebidID
        activeAdUnits[key]?.stopAutoRefresh()
        activeAdUnits.remove(key)
    }
}