package com.ascendeum.pbjsservertestapp.Ads

import com.google.android.gms.ads.AdSize

data class AdUnitConfig(
    val gamID: String,
    val prebidID: String,
    val sizes: List<AdSize>
) {
    // Returns the primary width/height for Prebid initialization
    val primaryWidth: Int get() = sizes[0].width
    val primaryHeight: Int get() = sizes[0].height
}

enum class AdPlacement {
    SYMBOL_INSTREAM_1, SYMBOL_INSTREAM_2, SYMBOL_INSTREAM_3,
    SYMBOL_INSTREAM_4, SYMBOL_INSTREAM_5, SYMBOL_INSTREAM_6,
    SYMBOL, BANNER_HOME, BANNER_SYMBOL;

    val config: AdUnitConfig
        get() = when (this) {
            SYMBOL_INSTREAM_1 -> AdUnitConfig("/22404395434/stocktwitsandroidapp/Symbol_InStream_Native", "2d60yfch", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            SYMBOL_INSTREAM_2 -> AdUnitConfig("/22404395434/stocktwitsandroidapp/Symbol_InStream_Native_2", "sckflmua", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            SYMBOL_INSTREAM_3 -> AdUnitConfig("/22404395434/stocktwitsandroidapp/Symbol_InStream_Native_3", "sbpwrpl5", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            SYMBOL_INSTREAM_4 -> AdUnitConfig("/22404395434/stocktwitsandroidapp/Symbol_InStream_Native_4", "ziuvyedi", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            SYMBOL_INSTREAM_5 -> AdUnitConfig("/22404395434/stocktwitsandroidapp/Symbol_InStream_Native_5", "0ot0h9cn", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            SYMBOL_INSTREAM_6 -> AdUnitConfig("/22404395434/stocktwitsandroidapp/Symbol_InStream_Native_6", "wtw10il9", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            SYMBOL -> AdUnitConfig("/22404395434/stocktwitsandroidapp/symbol", "c74jxrrz", listOf(AdSize.MEDIUM_RECTANGLE, AdSize.BANNER))
            BANNER_HOME -> AdUnitConfig("/22404395434/stocktwitsandroidapp/HomePage_SmallBanner", "q0yz226t", listOf(AdSize.BANNER))
            BANNER_SYMBOL -> AdUnitConfig("/22404395434/stocktwitsandroidapp/SymbolPage_SmallBanner", "wwpjhyld", listOf(AdSize.BANNER))
        }
}