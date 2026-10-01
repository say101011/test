package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AlertType
import com.example.data.model.DistanceUnit
import com.example.data.model.LocationAlarm
import com.example.data.model.RepeatType
import com.example.data.model.TriggerType
import com.example.ui.components.RadiusSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    initialAlarm: LocationAlarm,
    distanceUnit: DistanceUnit,
    onSave: (LocationAlarm) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialAlarm.title) }
    var radiusMeters by remember { mutableIntStateOf(initialAlarm.radiusMeters) }
    var triggerType by remember { mutableStateOf(initialAlarm.triggerType) }
    var alertType by remember { mutableStateOf(initialAlarm.alertType) }
    var repeatType by remember { mutableStateOf(initialAlarm.repeatType) }
    var memo by remember { mutableStateOf(initialAlarm.memo) }

    val isEditing = initialAlarm.id != 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) stringResource(R.string.edit_alarm) else stringResource(R.string.map_set_alarm_button),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.alarm_name_label)) },
                    placeholder = { Text(stringResource(R.string.alarm_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Location Coordinate badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = String.format(
                                "Lat: %.5f, Lon: %.5f",
                                initialAlarm.latitude,
                                initialAlarm.longitude
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Radius Selector
                Column {
                    Text(
                        text = stringResource(R.string.alarm_radius_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    RadiusSelector(
                        selectedRadiusMeters = radiusMeters,
                        distanceUnit = distanceUnit,
                        onRadiusSelected = { radiusMeters = it }
                    )
                }

                // Trigger Type (Enter vs Leave)
                Column {
                    Text(
                        text = stringResource(R.string.alarm_trigger_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = triggerType == TriggerType.ENTER,
                            onClick = { triggerType = TriggerType.ENTER },
                            label = { Text(stringResource(R.string.trigger_enter)) }
                        )
                        FilterChip(
                            selected = triggerType == TriggerType.EXIT,
                            onClick = { triggerType = TriggerType.EXIT },
                            label = { Text(stringResource(R.string.trigger_exit)) }
                        )
                    }
                }

                // Alert Type
                Column {
                    Text(
                        text = stringResource(R.string.alarm_alert_type),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = alertType == AlertType.NOTIFICATION,
                            onClick = { alertType = AlertType.NOTIFICATION },
                            label = { Text("Notif") }
                        )
                        FilterChip(
                            selected = alertType == AlertType.NOTIFICATION_VIBRATION,
                            onClick = { alertType = AlertType.NOTIFICATION_VIBRATION },
                            label = { Text("+ Vibrate") }
                        )
                        FilterChip(
                            selected = alertType == AlertType.ALARM_SOUND,
                            onClick = { alertType = AlertType.ALARM_SOUND },
                            label = { Text("Sound") }
                        )
                    }
                }

                // Repeat Type
                Column {
                    Text(
                        text = stringResource(R.string.alarm_repeat_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = repeatType == RepeatType.EVERY_TIME,
                            onClick = { repeatType = RepeatType.EVERY_TIME },
                            label = { Text(stringResource(R.string.repeat_always)) }
                        )
                        FilterChip(
                            selected = repeatType == RepeatType.ONCE,
                            onClick = { repeatType = RepeatType.ONCE },
                            label = { Text(stringResource(R.string.repeat_once)) }
                        )
                    }
                }

                // Memo
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text(stringResource(R.string.alarm_memo_label)) },
                    placeholder = { Text(stringResource(R.string.alarm_memo_hint)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Location Alarm" }
                    val updated = initialAlarm.copy(
                        title = finalTitle,
                        radiusMeters = radiusMeters,
                        triggerType = triggerType,
                        alertType = alertType,
                        repeatType = repeatType,
                        memo = memo,
                        isEnabled = true
                    )
                    onSave(updated)
                }
            ) {
                Text(text = stringResource(R.string.save_alarm))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
