package com.ascendeum.pbjsservertestapp

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.ascendeum.pbjsservertestapp.Ads.AdManager
import com.ascendeum.pbjsservertestapp.Ads.AdPlacement
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.InitializationStatus

class MainActivity : ComponentActivity() {
    private lateinit var adContainer: FrameLayout
    private var currentAdView: AdManagerAdView? = null

    /**** CHANGE THIS TO LOAD A DIFFERENT UNIT ****/
    private val currentPlacement = AdPlacement.SYMBOL_INSTREAM_2

    private val myTAG:String = "[Nexx360]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adContainer = findViewById(R.id.ad_view_container)

        /**** ALERT - START For Testing only we start ads from here  ****/
        // Actually we need to initialize SDK from MA, here in sample app ads gets called
        // before sdk Initialized , so using here
        PrebidMobile.setPrebidServerAccountId("1225")
        PrebidMobile.initializeSdk(this, "https://fast.nexx360.io/inapp") { status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(myTAG, "Prebid SDK Initialized")


                // Load the ad using the Singleton Manager
                AdManager.instance.getAd(
                    context = this,
                    placement = currentPlacement,
                    onAdViewCreated = ::attachAdView,
                    onAdLoaded = ::resizeLoadedAd
                )


            }
        }
        // BEFORE LIVE: PLEASE check and updates the store url and app bundle name # Set properties
        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=org.stocktwits.android.activity")
        TargetingParams.setBundleName("org.stocktwits.android.activity")
        /**** ALERT - END *****/
    }

    override fun onPause() {
        super.onPause()
        // Stop the 30s timer when the user leaves the app
        AdManager.instance.pauseAd(currentPlacement)
    }

    private fun attachAdView(loadedAdView: AdManagerAdView, initialSize: AdSize) {
        currentAdView = loadedAdView

        val widthPixels = initialSize.getWidthInPixels(this)
        val heightPixels = initialSize.getHeightInPixels(this)
        resizeAdContainer(initialSize, widthPixels, heightPixels)

        adContainer.removeAllViews()
        adContainer.addView(
            loadedAdView,
            createAdLayoutParams(initialSize, widthPixels, heightPixels)
        )
    }

    private fun resizeLoadedAd(renderedSize: AdSize) {
        val widthPixels = renderedSize.getWidthInPixels(this)
        val heightPixels = renderedSize.getHeightInPixels(this)

        resizeAdContainer(renderedSize, widthPixels, heightPixels)
        resizeAdView(renderedSize, widthPixels, heightPixels)
    }

    private fun resizeAdContainer(renderedSize: AdSize, widthPixels: Int, heightPixels: Int) {
        val layoutParams = adContainer.layoutParams

        if (isConcreteSize(renderedSize)) {
            layoutParams.width = widthPixels
            layoutParams.height = heightPixels
            Log.d(myTAG, "Ad container resized to: ${renderedSize.width}x${renderedSize.height}")
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            Log.d(myTAG, "Ad container using flexible size for: $renderedSize")
        }

        adContainer.layoutParams = layoutParams
        adContainer.requestLayout()
    }

    private fun resizeAdView(
        renderedSize: AdSize,
        widthPixels: Int,
        heightPixels: Int
    ) {
        val loadedAdView = currentAdView ?: return
        loadedAdView.layoutParams = createAdLayoutParams(renderedSize, widthPixels, heightPixels)
        loadedAdView.requestLayout()
    }

    private fun createAdLayoutParams(
        renderedSize: AdSize,
        widthPixels: Int,
        heightPixels: Int
    ): FrameLayout.LayoutParams {
        val width = if (isConcreteSize(renderedSize)) widthPixels else ViewGroup.LayoutParams.MATCH_PARENT
        val height = if (isConcreteSize(renderedSize)) heightPixels else ViewGroup.LayoutParams.WRAP_CONTENT

        return FrameLayout.LayoutParams(width, height, Gravity.CENTER)
    }

    private fun isConcreteSize(renderedSize: AdSize): Boolean {
        return renderedSize.width > 0 && renderedSize.height > 0
    }

    override fun onResume() {
        super.onResume()
        // Restart the timer when the user returns[cite: 12]
        AdManager.instance.resumeAd(currentPlacement)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up memory and stop refresh timers
        AdManager.instance.destroyAd(currentPlacement)
    }
}
