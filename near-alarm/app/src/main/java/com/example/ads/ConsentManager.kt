package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

object ConsentManager {
    private const val TAG = "ConsentManager"
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    val canRequestAdsFlow = kotlinx.coroutines.flow.MutableStateFlow(false)

    fun canRequestAds(context: Context): Boolean {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        val canRequest = consentInformation.canRequestAds()
        canRequestAdsFlow.value = canRequest
        return canRequest
    }

    fun isPrivacyOptionsRequired(context: Context): Boolean {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Official UMP consent gathering flow:
     * 1. Request consent information update
     * 2. Load and show consent form if required (EEA/UK/Switzerland/US State regulations)
     * 3. Initialize Mobile Ads ONLY after canRequestAds() returns true
     */
    fun gatherConsent(activity: Activity, onConsentCompleted: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.errorCode}: ${formError.message}")
                    }

                    val canRequest = consentInformation.canRequestAds()
                    canRequestAdsFlow.value = canRequest
                    if (canRequest) {
                        initializeMobileAds(activity)
                    }
                    onConsentCompleted()
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent info update error: ${requestConsentError.errorCode}: ${requestConsentError.message}")
                val canRequest = consentInformation.canRequestAds()
                canRequestAdsFlow.value = canRequest
                if (canRequest) {
                    initializeMobileAds(activity)
                }
                onConsentCompleted()
            }
        )

        // Check if cached consent already permits ad requests
        val cachedCanRequest = consentInformation.canRequestAds()
        canRequestAdsFlow.value = cachedCanRequest
        if (cachedCanRequest) {
            initializeMobileAds(activity)
        }
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Privacy options form error: ${formError.message}")
            }
            onDismissed()
        }
    }

    private fun initializeMobileAds(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        try {
            MobileAds.initialize(context) {
                Log.d(TAG, "MobileAds initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }
}
