1. Import the Prebid Mobile SDK - 
implementation 'org.prebid:prebid-mobile-sdk:1.0'

2. Configure Global SDK attributes
a. Developers should explicitly set the Rubicon hosted Prebid Server URL
PrebidMobile.setPrebidServerHost(Host.RUBICON);

b. The global Account ID is a required field attached to all requests 
PrebidMobile.setPrebidServerAccountId("INSERT-ACCOUNT-ID");

3. Create Ad Units - Banner Ad Units
Below is a sample definition of a Banner Ad Unit creation with one additional ad size.
   BannerAdUnit("22178-imp-Way2News-1", 300, 250)

4. Create AdManagerAdView

5. 4. Make a bid request to Prebid Server
   val adRequest = AdManagerAdRequest.Builder().build()
        bannerUnit?.fetchDemand(adRequest) {
            // 6. Load GAM Ad
            adView.loadAd(adRequest)
        }



******************* CHANGES ************************
Changes made:

* AdConfig.kt

1. Added primarySize.
2. Added prebidAdditionalSizes.prebidAdditionalSizes excludes FLUID and any non-concrete size by filtering width > 0 && height > 0. Additional sizes skip the primary size and remove duplicates.

* AdManager.kt

1. Changed getAd() callback shape:
2. onAdViewCreated(adView, initialSize) for attaching the ad view before load.
3. onAdLoaded(renderedSize) for resizing layout after load.
4. Added Prebid additional sizes via bannerUnit.addAdditionalSize(...).Removed all post-load adView.setAdSize(...) resizing.
5. Moved GAM setAdSizes(...) to happen after fetchDemand() but before loadAd().
6. If Prebid returns hb_size, GAM is configured with that concrete size before load.If no Prebid size is returned, GAM uses the full configured size list.Added hb_size parsing helper: String.toAdSize().
7. Kept findPrebidCreativeSize(...) only to detect/report rendered Prebid size for layout handling.

* MainActivity.kt

1. Updated getAd() usage for the new two-callback flow.
 * Added currentAdView.
 * Added attachAdView(...) so the GAM view is attached before loadAd().
 * Added resizeLoadedAd(...).
 * Added container resizing based on rendered size.
 * Added ad view layout resizing without calling setAdSize().
Concrete sizes like 300x250 get fixed pixel dimensions.Fluid/non-concrete sizes use MATCH_PARENT x WRAP_CONTENT.

* activity_main.xml

1. Changed ad container from fixed 336dp x 280dp to: layout_width="match_parent"
layout_height="wrap_content", This supports both fluid ads and runtime resizing for concrete ads.        

       