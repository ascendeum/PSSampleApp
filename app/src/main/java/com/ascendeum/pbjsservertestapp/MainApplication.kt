package com.ascendeum.pbjsservertestapp

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("PS-Sample", "App Started")
        initDFP()
        initPrebidSDK()
    }

    private fun initPrebidSDK() {
        Log.d("PS-Sample", "initPrebidSDK.")
        PrebidMobile.setPrebidServerAccountId("22178-Way2news-ANDROID")
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setApplicationContext(this)

        //PrebidMobile.setLogLevel(LogLevel.DEBUG)
//        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
//        PrebidMobile.setCustomStatusEndpoint("https://prebid-server-test-j.prebid.org/status")
//        PrebidMobile.initializeSdk(applicationContext, "https://prebid-server-test-j.prebid.org/openrtb2/auction") { status ->
//            if (status == InitializationStatus.SUCCEEDED) {
//                Log.d("S2S", "SDK initialized successfully!")
//            } else {
//                Log.e("S2S", "SDK initialization error: $status\n${status.description}")
//            }
//        }
        PrebidMobile.setShareGeoLocation(true)

        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=com.thechive")
        TargetingParams.setBundleName("com.thechive")

//        TargetingParams.setGlobalOrtbConfig(
//            """
//                {
//                  "displaymanager": "Google",
//                  "displaymanagerver": "${MobileAds.getVersion()}",
//                  "ext": {
//                    "myext": {
//                      "test": 1
//                    }
//                  }
//                }
//            """.trimIndent()
//        )
        Log.d("S2S","SDK_ACC_ID ${PrebidMobile.getPrebidServerAccountId()}")
        Log.d("S2S","SDK_HOST ${PrebidMobile.getPrebidServerHost()}")
    }

    private fun initDFP() {
        MobileAds.initialize(this) {
            Log.d("PS-Sample", "DFP-Initialization complete.")
        }
    }
}