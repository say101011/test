package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AlertType
import com.example.data.model.DistanceUnit
import com.example.data.model.LocationAlarm
import com.example.data.model.RepeatType
import com.example.data.model.TriggerType
import com.example.map.MapHelper
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val activeAlarms by viewModel.activeAlarms.collectAsStateWithLifecycle()
    val inactiveAlarms by viewModel.inactiveAlarms.collectAsStateWithLifecycle()
    val distanceUnit by viewModel.distanceUnit.collectAsStateWithLifecycle()

    val currentList = if (selectedTabIndex == 0) activeAlarms else inactiveAlarms

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateAlarm() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs: Active vs Inactive
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text("${stringResource(R.string.alarms_active_tab)} (${activeAlarms.size})")
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text("${stringResource(R.string.alarms_inactive_tab)} (${inactiveAlarms.size})")
                    }
                )
            }

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTabIndex == 0) {
                                stringResource(R.string.no_active_alarms)
                            } else {
                                stringResource(R.string.no_inactive_alarms)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { alarm ->
                        AlarmItemCard(
                            alarm = alarm,
                            distanceUnit = distanceUnit,
                            distanceToAlarm = viewModel.calculateDistanceToAlarm(alarm),
                            onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                            onEdit = { viewModel.openEditAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    alarm: LocationAlarm,
    distanceUnit: DistanceUnit,
    distanceToAlarm: Double?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alarm.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Real-time distance indicator
                    if (distanceToAlarm != null) {
                        val isInside = distanceToAlarm <= alarm.radiusMeters
                        val distanceString = if (isInside) {
                            stringResource(R.string.destination_nearby)
                        } else {
                            MapHelper.formatDistance(distanceToAlarm, distanceUnit)
                        }
                        Text(
                            text = distanceString,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isInside) FontWeight.Bold else FontWeight.Normal,
                            color = if (isInside) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row: Radius, Enter/Exit, Alert Type, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radius badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = MapHelper.formatRadius(alarm.radiusMeters, distanceUnit),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Trigger badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (alarm.triggerType == TriggerType.ENTER) "Enter" else "Leave",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Alert type icon
                val alertIcon = when (alarm.alertType) {
                    AlertType.NOTIFICATION -> Icons.Default.Notifications
                    AlertType.NOTIFICATION_VIBRATION -> Icons.Default.Notifications
                    AlertType.ALARM_SOUND -> Icons.Default.VolumeUp
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = alertIcon,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (alarm.alertType == AlertType.ALARM_SOUND) "Sound" else "Notif",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Repeat badge
                if (alarm.repeatType == RepeatType.ONCE) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Once",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Memo preview if available
            if (alarm.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = alarm.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Actions row: Edit and Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_alarm),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_alarm),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
