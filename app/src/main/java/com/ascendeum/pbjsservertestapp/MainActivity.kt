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
    private val myTAG:String = "[nexx360]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ALERT - DON'T need to init from here as a test we are doing here
        // just to insure that ads are requesting after sdk initialized
        initPrebidSDK()
    }

    private fun initPrebidSDK() {
        Log.d(myTAG, "Init Prebid SDK")
        PrebidMobile.setPrebidServerAccountId("1225") // "22178-chive-android"
        PrebidMobile.initializeSdk(this,"https://fast.nexx360.io/inapp",){ status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(myTAG, "Prebid SDK initialized successfully!")
                setupAd()
            } else {
                Log.e(myTAG, "Prebid SDK initialization error: $status\n${status.description}")
            }
        }

//        PrebidMobile.setShareGeoLocation(true)
//        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=sun.way2sms.hyd.com")
//        TargetingParams.setBundleName("Way2News")

        // Prebid SDK allows the customization of the OpenRTB request on the global level using function setGlobalOrtbConfig()
        // The parameter passed to TargetingParams.setGlobalOrtbConfig() will be merged into all SDK’s bid requests on the global level.
        // the below example will add the $.ext.myext.test parameter and change the displaymanager and displaymanagerver parameters in each request.
        // attention that there are certain protected fields such as regs, device, geo, ext.gdpr,
        // ext.us_privacy, and ext.consent which cannot be changed using the setGlobalOrtbConfig() method.

        TargetingParams.setGlobalOrtbConfig(
            "{" +
                    " \"displaymanager\": \"Google\"," +
                    " \"displaymanagerver\": \"" + MobileAds.getVersion() + "\"," +
                    " \"ext\": {" +
                    "   \"myext\": {" +
                    "    \"test\": 1" +
                    "   }" +
                    " }" +
                    "}"
        )
        // To invalidate the global config, just set the empty string:
        // TargetingParams.setGlobalOrtbConfig("")


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
        if (bannerUnit != null) return
        // 1. Initialize GAM AdView
        adView = AdManagerAdView(this).apply {
            adUnitId = "/22404395434/stocktwitsandroidapp/symbol"
            setAdSizes(
                AdSize(300, 250),
                AdSize(320, 50)
            )
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(myTAG, "onAdLoaded")
                    Log.d(myTAG, "GAM ad loaded, trying to detect creative size")
                    AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                        override fun success(width: Int, height: Int) {
                            Log.d(myTAG, "Detected creative size: $adSize")
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
        // BannerAdUnit in Prebid does NOT support multiple sizes in a single ad unit like GAM does.
        bannerUnit = BannerAdUnit("c74jxrrz", 320, 50)
        // Start auto-refresh every 30 seconds
        bannerUnit?.setAutoRefreshInterval(30)
        // Prebid SDK allows the customization of the OpenRTB request on the impression level using the setImpORTBConfig()
        // The parameter passed to setImpOrtbConfig() will be merged into the respective imp object for this Ad Unit.
        // the below example will add the $.imp[0].bidfloor and $.imp[0].banner.battr parameters to the bid request.
        bannerUnit?.impOrtbConfig = "{" +
                "  \"bidfloor\": 0.01," +
                "  \"banner\": {" +
                "    \"battr\": [1,2,3,4]" +
                "  }" +
                "}";

        // To empty out a previously provided impression config, just set it to the empty string:
        // bannerUnit?.setImpOrtbConfig("")
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
        isOnBannerAds = true
        if (bannerUnit == null) {
            //setupAd()
        } else {
            // If it already exists, just make sure it's refreshing
            bannerUnit?.resumeAutoRefresh()
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
