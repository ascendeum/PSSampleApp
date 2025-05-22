package com.ascendeum.pbjsservertestapp

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams

class MainApplication : Application() {
    private val myTAG:String = "prebid-server"
    override fun onCreate() {
        super.onCreate()
        initDFP()
        initPrebidSDK()
    }

    private fun initPrebidSDK() {
        Log.d(myTAG, "Init Prebid SDK")
        PrebidMobile.setPrebidServerAccountId("22178-chive-android")
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setApplicationContext(this)
        PrebidMobile.setShareGeoLocation(true)
        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=com.thechive")
        TargetingParams.setBundleName("com.thechive")
        Log.d(myTAG,"SDK_ACC_ID ${PrebidMobile.getPrebidServerAccountId()}")
        Log.d(myTAG,"SDK_HOST ${PrebidMobile.getPrebidServerHost()}")
    }

    private fun initDFP() {
        MobileAds.initialize(this) {
            Log.d(myTAG, "GAM SDK Initialization complete")
        }
    }
}