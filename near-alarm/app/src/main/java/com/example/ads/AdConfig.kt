package com.example.ads

/**
 * Centralized Ad Configuration for Near Alarm.
 *
 * For Production Release:
 * Replace ADMOB_APP_ID with your registered Google AdMob App ID (also in AndroidManifest.xml).
 * Replace BANNER_AD_UNIT_ID with your production Banner Ad Unit ID.
 * Set USE_TEST_ADS to false.
 */
object AdConfig {
    // Official Google Sample App ID for testing
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // Official Google Sample Banner Ad Unit ID
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // Set to your production unit ID when releasing
    const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    const val USE_TEST_ADS = true

    val activeBannerAdUnitId: String
        get() = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID
}
