package com.ascendeum.pbjsservertestapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.ascendeum.pbjsservertestapp.Ads.AdManager
import com.ascendeum.pbjsservertestapp.Ads.AdPlacement
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.InitializationStatus

class MainActivity : ComponentActivity() {
    private lateinit var adContainer: FrameLayout
    private val currentPlacement = AdPlacement.SYMBOL_INSTREAM_6
    private val myTAG = "[Nexx360]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adContainer = findViewById(R.id.ad_view_container)

        // Initialize Prebid Global Server Environments
        PrebidMobile.setPrebidServerAccountId("1225")
        PrebidMobile.initializeSdk(this, "https://fast.nexx360.io/inapp") { status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(myTAG, "Prebid Core Framework initialized successfully.")

                // Initiate asynchronous loading via our singleton framework
                AdManager.instance.getAd(
                    context = this,
                    placement = currentPlacement,
                    onAdViewCreated = ::showAdView
                )
            } else {
                Log.e(myTAG, "Prebid initialization error occurred: $status")
            }
        }

        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=org.stocktwits.android.activity")
        TargetingParams.setBundleName("org.stocktwits.android.activity")
    }

    private fun showAdView(bannerView: View) {
        Log.d(myTAG, "Injecting layout container into active activity layout.")
        adContainer.removeAllViews()
        Log.d(myTAG, "final banner view $bannerView")
        adContainer.addView(bannerView)
    }

    override fun onPause() {
        super.onPause()
        AdManager.instance.pauseAd(currentPlacement)
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.resumeAd(currentPlacement)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.instance.destroyAd(currentPlacement)
    }
}
