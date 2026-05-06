package com.ascendeum.pbjsservertestapp

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.ascendeum.pbjsservertestapp.Ads.AdManager
import com.ascendeum.pbjsservertestapp.Ads.AdPlacement
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.InitializationStatus

class MainActivity : ComponentActivity() {
    private lateinit var adContainer: FrameLayout

    /**** CHANGE THIS TO LOAD A DIFFERENT UNIT ****/
    private val currentPlacement = AdPlacement.SYMBOL_INSTREAM_1

    private val myTAG:String = "[ASC-Ads-UI]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adContainer = findViewById(R.id.ad_view_container)

//        Load the ad using the Singleton Manager
//        AdManager.instance.getAd(this, currentPlacement) { loadedAdView ->
//            adContainer.removeAllViews()
//            adContainer.addView(loadedAdView)
//        }

        /**** ALERT - START For Testing only we start ads from here  ****/
        // Actually we need to initialize SDK from MA, here in sample app ads gets called
        // before sdk Initialized , so using here
        PrebidMobile.setPrebidServerAccountId("1225")
        PrebidMobile.initializeSdk(this, "https://fast.nexx360.io/inapp") { status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(myTAG, "Prebid SDK Initialized")


                // Load the ad using the Singleton Manager
                AdManager.instance.getAd(this, currentPlacement) { loadedAdView ->
                    adContainer.removeAllViews()
                    adContainer.addView(loadedAdView)
                }


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
