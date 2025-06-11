package com.ascendeum.pbjsservertestapp

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.data.InitializationStatus
import org.prebid.mobile.api.exceptions.AdException

class MainActivity : ComponentActivity() {
    private lateinit var adContainer: FrameLayout
    private lateinit var adView: AdManagerAdView
    private var bannerUnit: BannerAdUnit? = null
    private var refCount: Int = 0
    private var isOnBannerAds:Boolean = true
    private val myTAG:String = "prebid-server"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ALERT - DON'T need to init from here as a test we are doing here
        // just to insure that ads are requesting after sdk initialized
        initPrebidSDK()
    }

    private fun initPrebidSDK() {
        Log.d(myTAG, "Init Prebid SDK")
        PrebidMobile.setPrebidServerAccountId("22178-chive-android")
        PrebidMobile.initializeSdk(this,"https://prebid-server.rubiconproject.com/openrtb2/auction",){ status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(myTAG, "Prebid SDK initialized successfully!")
                setupAd()
            } else {
                Log.e(myTAG, "Prebid SDK initialization error: $status\n${status.description}")
            }
        }

        PrebidMobile.setShareGeoLocation(true)
        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=com.thechive")
        TargetingParams.setBundleName("com.thechive")
        Log.d(myTAG,"SDK_ACC_ID ${PrebidMobile.getPrebidServerAccountId()}")
        Log.d(myTAG,"SDK_HOST ${PrebidMobile.getPrebidServerHost()}")
    }

    private fun destroyBanner() {
        Log.d(myTAG, "destroyBanner")
        bannerUnit?.stopAutoRefresh()
        bannerUnit = null
        adView.destroy()
    }

    private fun setupAd() {
        Log.d(myTAG, "Initializing ad components")
        if(!isOnBannerAds) return
        // 1. Initialize GAM AdView
        adView = AdManagerAdView(this).apply {
            adUnitId = "/1006418/theChive_Android_Latest_300x250_Instream"
            setAdSizes(AdSize(300, 250))
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(myTAG, "onAdLoaded")
                    Log.d(myTAG, "GAM ad loaded, trying to detect creative size")

                    AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                        override fun success(width: Int, height: Int) {
                            Log.d("Prebid", "Detected creative size: $adSize")
                            adView.setAdSize(AdSize(width,height))
                        }

                        override fun failure(error: PbFindSizeError) {
                            Log.e(myTAG, "Failed to detect creative size: ${error.description}")
                        }
                    })
                    showAd()
                    // clear the ads container and add a new ads
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(myTAG, "Ad failed to load: ${error.message}")
                }
            }
        }


        // 3. Create and configure Prebid BannerAdUnit
        bannerUnit = BannerAdUnit("22178-imp-Chive_Android_S2S-Latest_300x250_Instream", 300, 250)
        // Start auto-refresh every 30 seconds
        bannerUnit?.setAutoRefreshInterval(30)

        // 4. Fetch demand and load GAM ad
        loadPrebidAd()
    }

    private fun loadPrebidAd() {
        Log.d(myTAG, "loadPrebidAd")
        val adRequestBuilder = AdManagerAdRequest.Builder()

        bannerUnit?.fetchDemand(adRequestBuilder) { resultCode ->
            Log.d(myTAG, "Prebid fetchDemand result: $resultCode")
            adRequestBuilder.addCustomTargeting("refreshCount",refCount.toString())
            Log.d(myTAG, "Custom KV : ${adRequestBuilder.build().customTargeting}")
            adView.loadAd(adRequestBuilder.build())
            refCount++
        }
    }

    // 2. Add GAM AdView to layout
    private fun showAd() {
        adContainer = findViewById(R.id.ad_view_container)
        adContainer.removeAllViews()
        adContainer.addView(adView)
    }

    override fun onPause() {
        Log.d(myTAG, "onPause")
        super.onPause()
        // Stop refresh when app goes to background
        isOnBannerAds = false
        bannerUnit?.stopAutoRefresh()
        Log.d(myTAG, "Auto-refresh stopped")
    }

    override fun onResume() {
        Log.d(myTAG, "onResume")
        super.onResume()
        if (!isOnBannerAds){
            // Resume refresh when app comes to foreground
//            bannerUnit?.setAutoRefreshInterval(30)
//            Log.d(myTAG, "Auto-refresh restarted")
            isOnBannerAds = true
            Log.d(myTAG, "set up add again")
            setupAd()
        }
    }

    override fun onDestroy() {
        Log.d(myTAG, "onDestroy")
        super.onDestroy()
        isOnBannerAds = false
        destroyBanner()
        Log.d(myTAG, "Banner destroyed and auto-refresh stopped")
    }
}
