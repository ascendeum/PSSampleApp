package com.ascendeum.pbjsservertestapp.Ads

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError

class AdManager private constructor() {

    private val myTAG = "[Nexx360]"

    // Key: Prebid Config ID, Value: BannerAdUnit
    // This map ensures the AdUnit stays in memory for auto-refresh to work
    private val activeAdUnits = mutableMapOf<String, BannerAdUnit>()

    companion object {
        val instance: AdManager by lazy { AdManager() }
    }

    fun getAd(
        context: Context,
        placement: AdPlacement,
        onAdViewCreated: (AdManagerAdView, AdSize) -> Unit,
        onAdLoaded: (AdSize) -> Unit
    ) {
        val config = placement.config
        Log.d(myTAG, "Fetching ad for: $config")

        // 1. Initialize GAM AdView
        val adView = AdManagerAdView(context).apply {
            adUnitId = config.gamID
        }
        onAdViewCreated(adView, config.primarySize)

        // 2. Initialize and Store Prebid BannerAdUnit
        val bannerUnit = BannerAdUnit(config.prebidID, config.primaryWidth, config.primaryHeight)
        config.prebidAdditionalSizes.forEach { size ->
            bannerUnit.addAdditionalSize(size.width, size.height)
        }
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
        val adListener = object : AdListener() {
            override fun onAdLoaded() {
                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        val prebidSize = AdSize(width, height)
                        Log.d(myTAG, "Prebid creative size detected: ${width}x${height}")
                        onAdLoaded(prebidSize)
                    }
                    override fun failure(error: PbFindSizeError) {
                        // Error 240 is common. It just means a standard GAM ad loaded, not a Prebid one.
                        // Use GAM's rendered size for the container instead of resizing the loaded ad view.
                        val renderedSize = adView.adSize ?: config.sizes.first()
                        Log.d(myTAG, "Standard GAM ad detected. Using GAM size: $renderedSize")
                        onAdLoaded(renderedSize)
                    }
                })
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(myTAG, "failed : ${error.message}")
            }
        }
        adView.adListener = adListener

        // 4. Fetch Demand and Load
        val requestBuilder = AdManagerAdRequest.Builder()
        var adSizesConfigured = false
        bannerUnit.fetchDemand(requestBuilder) { resultCode ->
            Log.d(myTAG, "Prebid fetchDemand result: $resultCode")
            val request = requestBuilder.build()
            Log.d(myTAG, "Custom KV : ${request.customTargeting}")

            if (!adSizesConfigured) {
                val prebidSize = request.customTargeting.getString("hb_size")?.toAdSize()
                val sizes = prebidSize?.let { arrayOf(it) } ?: config.sizes.toTypedArray()
                adView.setAdSizes(*sizes)
                adSizesConfigured = true
                Log.d(myTAG, "Configured GAM sizes: ${sizes.joinToString()}")
            }

            adView.loadAd(request)
        }
    }

    private fun String.toAdSize(): AdSize? {
        val parts = split("x")
        if (parts.size != 2) return null

        val width = parts[0].toIntOrNull() ?: return null
        val height = parts[1].toIntOrNull() ?: return null

        return AdSize(width, height)
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