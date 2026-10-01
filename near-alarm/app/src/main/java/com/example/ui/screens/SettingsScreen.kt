package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ads.ConsentManager
import com.example.data.model.DistanceUnit
import com.example.map.MapConfig
import com.example.ui.components.BackgroundLocationDisclosureDialog
import com.example.ui.components.ForegroundLocationDisclosureDialog
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val distanceUnit by viewModel.distanceUnit.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

    var showBgDisclosure by remember { mutableStateOf(false) }
    var showFgDisclosure by remember { mutableStateOf(false) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var showDeletePlacesConfirm by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    var hasFgLocation by remember { mutableStateOf(false) }
    var hasBgLocation by remember { mutableStateOf(false) }
    var hasNotificationPerm by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        hasFgLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasBgLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasFgLocation
        }

        hasNotificationPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    val fgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshPermissions()
    }

    val bgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. Location Section
        SettingsCard(title = stringResource(R.string.settings_section_location), icon = Icons.Default.LocationOn) {
            SettingsItemRow(
                title = stringResource(R.string.settings_foreground_perm),
                subtitle = if (hasFgLocation) stringResource(R.string.settings_perm_granted) else stringResource(R.string.settings_perm_denied),
                actionText = if (!hasFgLocation) stringResource(R.string.settings_perm_action) else null,
                onActionClick = { showFgDisclosure = true }
            )

            HorizontalDivider()

            SettingsItemRow(
                title = stringResource(R.string.settings_background_perm),
                subtitle = if (hasBgLocation) {
                    stringResource(R.string.settings_perm_granted)
                } else if (!hasFgLocation) {
                    "Enable Foreground Location first"
                } else {
                    "Tap to enable proximity arrival alarms when closed"
                },
                actionText = if (!hasBgLocation && hasFgLocation) stringResource(R.string.settings_perm_action) else null,
                onActionClick = { showBgDisclosure = true }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                HorizontalDivider()
                SettingsItemRow(
                    title = "Notifications",
                    subtitle = if (hasNotificationPerm) stringResource(R.string.settings_perm_granted) else stringResource(R.string.settings_perm_denied),
                    actionText = if (!hasNotificationPerm) stringResource(R.string.settings_perm_action) else null,
                    onActionClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                )
            }
        }

        // 2. Alarm Defaults Section
        SettingsCard(title = stringResource(R.string.settings_section_alarm), icon = Icons.Default.VolumeUp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_sound),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Play alarm sound on arrival",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_vibration),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Vibrate device when triggered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }
        }

        // 3. Distance Units Section
        SettingsCard(title = stringResource(R.string.settings_section_units), icon = Icons.Default.Straighten) {
            UnitOptionRow(
                title = stringResource(R.string.unit_automatic),
                selected = distanceUnit == DistanceUnit.AUTOMATIC,
                onClick = { viewModel.setDistanceUnit(DistanceUnit.AUTOMATIC) }
            )
            UnitOptionRow(
                title = stringResource(R.string.unit_metric),
                selected = distanceUnit == DistanceUnit.METRIC,
                onClick = { viewModel.setDistanceUnit(DistanceUnit.METRIC) }
            )
            UnitOptionRow(
                title = stringResource(R.string.unit_imperial),
                selected = distanceUnit == DistanceUnit.IMPERIAL,
                onClick = { viewModel.setDistanceUnit(DistanceUnit.IMPERIAL) }
            )
        }

        // 4. Privacy & Data Section
        SettingsCard(title = stringResource(R.string.settings_section_privacy), icon = Icons.Default.PrivacyTip) {
            SettingsClickableRow(
                title = stringResource(R.string.settings_privacy_policy),
                onClick = { showPrivacyPolicyDialog = true }
            )

            // Privacy Options via Google UMP (For EEA/UK/US state regulations)
            if (activity != null && ConsentManager.isPrivacyOptionsRequired(activity)) {
                HorizontalDivider()
                SettingsClickableRow(
                    title = stringResource(R.string.settings_privacy_options),
                    onClick = { ConsentManager.showPrivacyOptionsForm(activity) {} }
                )
            }

            HorizontalDivider()

            SettingsClickableRow(
                title = stringResource(R.string.settings_clear_history),
                onClick = { viewModel.clearHistory() }
            )

            HorizontalDivider()

            SettingsClickableRow(
                title = stringResource(R.string.settings_delete_places),
                onClick = { showDeletePlacesConfirm = true }
            )

            HorizontalDivider()

            SettingsClickableRow(
                title = stringResource(R.string.settings_delete_all),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteAllConfirm = true }
            )
        }

        // 5. About Section
        SettingsCard(title = stringResource(R.string.settings_section_about), icon = Icons.Default.Info) {
            SettingsItemRow(
                title = stringResource(R.string.about_version),
                subtitle = "1.0 (Production Release)",
                actionText = null,
                onActionClick = {}
            )

            HorizontalDivider()

            SettingsClickableRow(
                title = stringResource(R.string.about_osm_attribution),
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MapConfig.ATTRIBUTION_URL))
                    context.startActivity(browserIntent)
                }
            )

            HorizontalDivider()

            SettingsClickableRow(
                title = stringResource(R.string.about_licenses),
                onClick = { showLicensesDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Dialogs
    if (showFgDisclosure) {
        ForegroundLocationDisclosureDialog(
            onConfirm = {
                showFgDisclosure = false
                fgLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismiss = { showFgDisclosure = false }
        )
    }

    if (showBgDisclosure) {
        BackgroundLocationDisclosureDialog(
            onConfirm = {
                showBgDisclosure = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            },
            onDismiss = { showBgDisclosure = false }
        )
    }

    if (showDeletePlacesConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePlacesConfirm = false },
            title = { Text(stringResource(R.string.settings_delete_places)) },
            text = { Text("Are you sure you want to delete all saved alarms?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllSavedPlaces()
                        showDeletePlacesConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlacesConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text(stringResource(R.string.delete_all_confirm_title)) },
            text = { Text(stringResource(R.string.delete_all_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllAppData()
                        showDeleteAllConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text(stringResource(R.string.settings_privacy_policy)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Near Alarm Privacy Policy:\n\n" +
                                "• Zero External Server: Near Alarm operates entirely on your device. We do not have servers, user accounts, or cloud databases.\n\n" +
                                "• Location Data: Used solely to calculate distance to your target destination and trigger arrival/departure alarms via on-device geofencing. Your live location and travel history are never stored, logged, or uploaded.\n\n" +
                                "• Saved Alarms: Stored locally in a secure on-device SQLite/Room database.\n\n" +
                                "• Third-Party Advertising: We use Google Mobile Ads SDK (AdMob) to display advertisements. AdMob may process device identifiers, general location, and advertising metrics according to Google's Privacy Policy. You can adjust your consent choices under Privacy Options at any time.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open-Source Licenses") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "• OpenStreetMap: © OpenStreetMap contributors (ODbL License)\n\n" +
                                "• osmdroid: Licensed under the Apache License 2.0\n\n" +
                                "• Android Jetpack & Compose: Licensed under the Apache License 2.0\n\n" +
                                "• Google Play Services: Google APIs Terms of Service\n\n" +
                                "• OkHttp & Retrofit: Square, Inc. Apache License 2.0",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun SettingsItemRow(
    title: String,
    subtitle: String,
    actionText: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (actionText != null) {
            OutlinedButton(onClick = onActionClick) {
                Text(text = actionText, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = titleColor)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun UnitOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
    }
}
