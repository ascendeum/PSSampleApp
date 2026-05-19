package com.ascendeum.pbjsservertestapp.Ads

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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

    private val activeAdUnits = mutableMapOf<String, BannerAdUnit>()
    private val activeAdViews = mutableMapOf<String, AdManagerAdView>()
    private val activeAdContainers = mutableMapOf<String, FrameLayout>()

    companion object {
        val instance: AdManager by lazy { AdManager() }
    }

    fun getAd(
        context: Context,
        placement: AdPlacement,
        onAdViewCreated: (View) -> Unit
    ) {
        val config = placement.config
        Log.d(myTAG, "Fetching ad for: $config")
        destroyAd(placement)

        // 1. Create the outer parent container
        val adContainer = FrameLayout(context).apply {
            setBackgroundColor(0x00000000)
        }

        // 2. Initialize GAM AdView with flexible parameters
        val adView = AdManagerAdView(context).apply {
            adUnitId = config.gamID
            setAdSizes(*config.validSizes.toTypedArray())
        }
        Log.d(myTAG, "Configured GAM valid sizes: ${config.validSizes.joinToString()}")

        // Set initial primary sizing constraints on the container view
        applyContainerSizing(context, adContainer, adView, config.primarySize)

        // Add GAM view to container with instructions to fill the space natively
        val adViewParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        adContainer.addView(adView, adViewParams)
        onAdViewCreated(adContainer)

        // 3. Initialize and Store Prebid BannerAdUnit
        val bannerUnit = BannerAdUnit(config.prebidID, config.primaryWidth, config.primaryHeight)
        config.prebidAdditionalSizes.forEach { size ->
            bannerUnit.addAdditionalSize(size.width, size.height)
        }
        bannerUnit.setAutoRefreshInterval(30)

        activeAdUnits[config.prebidID] = bannerUnit
        activeAdViews[config.prebidID] = adView
        activeAdContainers[config.prebidID] = adContainer

        // 4. Set up listeners for creative size detection
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(myTAG, "GAM ad loaded container event fired. Scanning for Prebid components...")
                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        val prebidSize = AdSize(width, height)
                        Log.d(myTAG, "Prebid creative verified successfully: ${width}x${height}")

                        // Adapt the outer container to match the raw Prebid asset dimensions
                        applyContainerSizing(context, adContainer, adView, prebidSize)
                    }

                    override fun failure(error: PbFindSizeError) {
                        val renderedSize = adView.adSize ?: config.sizes.first()
                        Log.d(myTAG, "Standard fallback ad detected (Error Code: ${error.description}). Using fallback size: $renderedSize")
                        applyContainerSizing(context, adContainer, adView, renderedSize)
                    }
                })
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(myTAG, "Ad manager tracking failure: ${error.message}")
            }
        }

        // 5. Fetch Demand and Load Ad
        val requestBuilder = AdManagerAdRequest.Builder()
        bannerUnit.fetchDemand(requestBuilder) { resultCode ->
            Log.d(myTAG, "Prebid fetchDemand completed: $resultCode")
            val request = requestBuilder.build()
            adView.loadAd(request)
        }
    }

    /**
     * Updates layout configurations cleanly without stripping GAM's inner core view structures.
     */
    private fun applyContainerSizing(
        context: Context,
        adContainer: FrameLayout,
        adView: AdManagerAdView,
        adSize: AdSize
    ) {
        adContainer.post {
            // Determine structural heights and widths
            val widthParam = if (adSize.width > 0) adSize.getWidthInPixels(context) else ViewGroup.LayoutParams.MATCH_PARENT
            val heightParam = if (adSize.height > 0) adSize.getHeightInPixels(context) else ViewGroup.LayoutParams.WRAP_CONTENT

            // Re-apply explicit measurements onto the master wrapper frame layout only
            val containerParams = adContainer.layoutParams ?: ViewGroup.LayoutParams(widthParam, heightParam)
            containerParams.width = widthParam
            containerParams.height = heightParam
            adContainer.layoutParams = containerParams

            // Keep the underlying ad view filling the parent container cleanly
            val adViewParams = adView.layoutParams as? FrameLayout.LayoutParams ?: FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            adViewParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            adViewParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            adView.layoutParams = adViewParams

            adContainer.requestLayout()
        }
    }

    fun pauseAd(placement: AdPlacement) {
        val key = placement.config.prebidID
        activeAdUnits[key]?.stopAutoRefresh()
        activeAdViews[key]?.pause()
    }

    fun resumeAd(placement: AdPlacement) {
        val key = placement.config.prebidID
        activeAdUnits[key]?.resumeAutoRefresh()
        activeAdViews[key]?.resume()
    }

    fun destroyAd(placement: AdPlacement) {
        val key = placement.config.prebidID
        activeAdUnits[key]?.stopAutoRefresh()
        activeAdViews[key]?.destroy()
        activeAdContainers[key]?.removeAllViews()
        activeAdUnits.remove(key)
        activeAdViews.remove(key)
        activeAdContainers.remove(key)
    }
}