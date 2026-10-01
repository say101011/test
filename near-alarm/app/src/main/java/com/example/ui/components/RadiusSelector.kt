package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.DistanceUnit
import com.example.map.MapHelper
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RadiusSelector(
    selectedRadiusMeters: Int,
    distanceUnit: DistanceUnit,
    onRadiusSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetRadii = listOf(100, 300, 500, 1000)
    var isCustom by remember(selectedRadiusMeters) {
        mutableStateOf(!presetRadii.contains(selectedRadiusMeters))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            presetRadii.forEach { radius ->
                val isSelected = !isCustom && selectedRadiusMeters == radius
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        isCustom = false
                        onRadiusSelected(radius)
                    },
                    label = {
                        Text(MapHelper.formatRadius(radius, distanceUnit))
                    }
                )
            }
            FilterChip(
                selected = isCustom,
                onClick = { isCustom = true },
                label = { Text("Custom") }
            )
        }

        if (isCustom) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Radius: ${MapHelper.formatRadius(selectedRadiusMeters, distanceUnit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "50 m – 5 km",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Slider(
                value = selectedRadiusMeters.coerceIn(50, 5000).toFloat(),
                onValueChange = { onRadiusSelected((it / 50).roundToInt() * 50) },
                valueRange = 50f..5000f,
                steps = 98,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
