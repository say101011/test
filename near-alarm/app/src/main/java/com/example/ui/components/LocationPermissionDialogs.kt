package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun BackgroundLocationDisclosureDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.disclosure_bg_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.disclosure_bg_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• Location data is never transmitted to any external server.\n• Geofencing is handled locally by Android to conserve battery.\n• No background monitoring occurs when all alarms are turned off.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.continue_action))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.not_now))
            }
        }
    )
}

@Composable
fun ForegroundLocationDisclosureDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.disclosure_fg_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.disclosure_fg_desc),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.continue_action))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.not_now))
            }
        }
    )
}

@Composable
fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.disclosure_notification_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.disclosure_notification_desc),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.continue_action))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.not_now))
            }
        }
    )
}
