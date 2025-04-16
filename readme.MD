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

       