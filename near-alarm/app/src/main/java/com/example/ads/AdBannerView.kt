package com.example.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.activeBannerAdUnitId
) {
    val context = LocalContext.current
    val canRequestAdsFromFlow by ConsentManager.canRequestAdsFlow.collectAsStateWithLifecycle()
    val canRequestAds = canRequestAdsFromFlow || ConsentManager.canRequestAds(context)

    var adLoaded by remember { mutableStateOf(false) }
    var adFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        if (canRequestAds) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                adLoaded = true
                                adFailed = false
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                adFailed = true
                            }
                        }
                        val adRequest = AdRequest.Builder().build()
                        loadAd(adRequest)
                    }
                }
            )
        }

        // In test mode or before live ad fill in container/emulator, render test indicator placeholder
        if ((!canRequestAds || !adLoaded) && AdConfig.USE_TEST_ADS) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AdMob Banner (Test Mode: 320x50)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
