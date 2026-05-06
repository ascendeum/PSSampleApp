package com.ascendeum.pbjsservertestapp

import android.app.Application
import android.util.Log
import com.ascendeum.pbjsservertestapp.Ads.AdManager
import com.google.android.gms.ads.MobileAds
import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.InitializationStatus

class MainApplication : Application() {
    private val myTAG:String = "[MainApp]"
    override fun onCreate() {
        super.onCreate()
        initGAM()
        initPrebidSDK()
    }

    // Move the global SDK initializations here to ensure they happen only once when the app starts
    private fun initPrebidSDK() {

//        PrebidMobile.setPrebidServerAccountId("1225")
//        PrebidMobile.initializeSdk(this, "https://fast.nexx360.io/inapp") { status ->
//            if (status == InitializationStatus.SUCCEEDED) {
//                Log.d(myTAG, "Prebid SDK Initialized")
//            }
//        }
//        // BEFORE LIVE: PLEASE check and updates the store url and app bundle name # Set properties
//        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=org.stocktwits.android.activity")
//        TargetingParams.setBundleName("org.stocktwits.android.activity")

//         Optional
//         Prebid SDK allows the customization of the OpenRTB request on the global level using function setGlobalOrtbConfig()
//         The parameter passed to TargetingParams.setGlobalOrtbConfig() will be merged into all SDK’s bid requests on the global level.
//         the below example will add the $.ext.myext.test parameter and change the displaymanager and displaymanagerver parameters in each request.
//         attention that there are certain protected fields such as regs, device, geo, ext.gdpr,
//         ext.us_privacy, and ext.consent which cannot be changed using the setGlobalOrtbConfig() method.

//        TargetingParams.setGlobalOrtbConfig(
//            "{" +
//                    " \"displaymanager\": \"Google\"," +
//                    " \"displaymanagerver\": \"" + MobileAds.getVersion() + "\"," +
//                    " \"ext\": {" +
//                    "   \"myext\": {" +
//                    "    \"test\": 1" +
//                    "   }" +
//                    " }" +
//                    "}"
//        )
//         To invalidate the global config, just set the empty string:
//         TargetingParams.setGlobalOrtbConfig("")
    }

    private fun initGAM() {
        MobileAds.initialize(this) {
            Log.d(myTAG, "GAM SDK Initialized")
        }
    }
}