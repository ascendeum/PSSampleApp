//package com.ascendeum.pbjsservertestapp
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.FrameLayout
//import androidx.activity.ComponentActivity
//import com.google.android.gms.ads.AdListener
//import com.google.android.gms.ads.AdSize
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.admanager.AdManagerAdRequest
//import com.google.android.gms.ads.admanager.AdManagerAdView
//import org.prebid.mobile.BannerAdUnit
//import org.prebid.mobile.BannerParameters
//import org.prebid.mobile.Signals
//
//class MainActivity : ComponentActivity() {
//    private lateinit var myFrameLayout: FrameLayout
//    private var bannerUnit: BannerAdUnit? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        createAd()
//    }
//
//    private fun createAd() {
//        Log.d("PS-Sample", "createAd")
//        // 1. Create Prebid Banner AdUnit
//        bannerUnit = BannerAdUnit("22178-imp-Chive_Android_S2S-Latest_300x250_Instream", 300, 250) // prebid baner
//        // 2. Configure banner parameters
//        val parameters = BannerParameters()
//        parameters.api = listOf(Signals.Api.MRAID_3, Signals.Api.OMID_1)
//        bannerUnit?.bannerParameters = parameters
//        Log.d("PS-Sample", "step-2")
//        // 3. Create AdManagerAdView
//        val adView = AdManagerAdView(this)
//        adView.adUnitId =  "/1006418/theChive_Android_Latest_300x250_Instream" // EXAMPLE AD UNIT
//        // Request an anchored adaptive banner with a width of 360.
//        adView.setAdSizes(AdSize(300,250))
//        // Add GMA SDK banner view to the app UI
//        myFrameLayout = findViewById(R.id.ad_view_container)
//        myFrameLayout.addView(adView)
//        Log.d("PS-Sample", "set gam view")
//        // 4. Make a bid request to Prebid Server
//        // Fetch Prebid bids and attach to GAM request
//        val adRequestBuilder = AdManagerAdRequest.Builder()
//        Log.d("PS-Sample", "adRequestBuilder")
//        bannerUnit?.fetchDemand(adRequestBuilder) { resultCode ->
//            Log.d("PS-Sample", "Fetch demand result: $resultCode")
//            // 5. Load GAM Ad
//            adView.loadAd(adRequestBuilder.build())
//        }
//        Log.d("PS-Sample", "fetchDemand failed")
//
//
//        // Ad events
//        adView.adListener = object : AdListener() {
//            override fun onAdFailedToLoad(adError : LoadAdError) {
//                Log.d("PS-Sample", "onAdFailedToLoad ${adError.message}")
//            }
//            override fun onAdLoaded() {
//                Log.d("PS-Sample", "onAdLoaded")
//            }
//        }
//    }
//}
