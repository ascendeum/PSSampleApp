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
        initPrebidSDK()
        initDFP()
    }

    private fun initPrebidSDK() {
        Log.d("PS-Sample", "initPrebidSDK.")
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId("22178-Way2news-ANDROID")
        //PrebidMobile.setLogLevel(LogLevel.DEBUG)
    }

    private fun initDFP() {
        MobileAds.initialize(this) {
            Log.d("PS-Sample", "DFP-Initialization complete.")
        }
    }
}