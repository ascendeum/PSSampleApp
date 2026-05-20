package com.ascendeum.pbjsservertestapp

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.ascendeum.pbjsservertestapp.Ads.AdManager
import com.ascendeum.pbjsservertestapp.Ads.AdPlacement
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.InitializationStatus

class MainActivity : ComponentActivity() {
    private lateinit var feedContainer: LinearLayout
    private val feedPlacements = listOf(
        AdPlacement.BANNER_SYMBOL,
        AdPlacement.SYMBOL,
        AdPlacement.SYMBOL_INSTREAM_1,
        AdPlacement.SYMBOL_INSTREAM_2,
        AdPlacement.SYMBOL_INSTREAM_3,
        AdPlacement.SYMBOL_INSTREAM_4,
        AdPlacement.SYMBOL_INSTREAM_5,
        AdPlacement.SYMBOL_INSTREAM_6
    )
    private val myTAG = "[Nexx360]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        feedContainer = findViewById(R.id.feed_container)

        // Initialize Prebid Global Server Environments
        PrebidMobile.setPrebidServerAccountId("1225")
        PrebidMobile.initializeSdk(this, "https://fast.nexx360.io/inapp") { status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(myTAG, "Prebid Core Framework initialized successfully.")
                runOnUiThread { buildFeed() }
            } else {
                Log.e(myTAG, "Prebid initialization error occurred: $status")
            }
        }

        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=org.stocktwits.android.activity")
        TargetingParams.setBundleName("org.stocktwits.android.activity")
    }

    private fun buildFeed() {
        feedContainer.removeAllViews()

        addHeader()
        feedPlacements.forEachIndexed { index, placement ->
            if (index == 0) {
                addAdSlot(placement)
            } else {
                repeat(3) { postOffset ->
                    val postNumber = ((index - 1) * 3) + postOffset + 1
                    addPostRow(postNumber)
                }
                addAdSlot(placement)
            }
        }

        repeat(3) { offset -> addPostRow(((feedPlacements.size - 1) * 3) + offset + 1) }
    }

    private fun addHeader() {
        val title = TextView(this).apply {
            text = "Nexx360 Multi Ad Feed Test"
            textSize = 20f
            setTextColor(0xFF111111.toInt())
            setPadding(dp(4), dp(8), dp(4), dp(12))
        }
        feedContainer.addView(title, fullWidthParams())
    }

    private fun addPostRow(postNumber: Int) {
        val post = TextView(this).apply {
            text = "Post #$postNumber\nAAPL market update, watchlist activity, and sample feed content."
            textSize = 15f
            setTextColor(0xFF222222.toInt())
            setBackgroundColor(0xFFF5F5F5.toInt())
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        feedContainer.addView(post, fullWidthParams(topMargin = dp(8), bottomMargin = dp(8)))
    }

    private fun addAdSlot(placement: AdPlacement) {
        val slot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFFE0E0E0.toInt())
            setPadding(dp(4), dp(8), dp(4), dp(8))
        }

        val label = TextView(this).apply {
            text = "Ad Slot: ${placement.name}"
            textSize = 12f
            setTextColor(0xFF666666.toInt())
            gravity = Gravity.CENTER
        }
        slot.addView(label, fullWidthParams(bottomMargin = dp(6)))

        feedContainer.addView(slot, fullWidthParams(topMargin = dp(10), bottomMargin = dp(10)))

        AdManager.instance.getAd(
            context = this,
            placement = placement,
            onAdViewCreated = { managedAdView ->
                showAdView(slot, placement, managedAdView)
            }
        )
    }

    private fun showAdView(slot: LinearLayout, placement: AdPlacement, bannerView: View) {
        Log.d(myTAG, "Injecting managed ad view for ${placement.name}: $bannerView")
        if (bannerView.parent != null) {
            (bannerView.parent as? ViewGroup)?.removeView(bannerView)
        }
        slot.addView(bannerView)
    }

    override fun onPause() {
        super.onPause()
        AdManager.instance.pauseAds(feedPlacements)
    }

    override fun onResume() {
        super.onResume()
        AdManager.instance.resumeAds(feedPlacements)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.instance.destroyAds(feedPlacements)
    }

    private fun fullWidthParams(topMargin: Int = 0, bottomMargin: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, topMargin, 0, bottomMargin)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
