package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.map.MapConfig
import com.example.map.MapHelper
import com.example.ui.components.ForegroundLocationDisclosureDialog
import com.example.ui.viewmodel.MainViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allAlarms by viewModel.allAlarms.collectAsStateWithLifecycle()
    val selectedPoint by viewModel.selectedMapPoint.collectAsStateWithLifecycle()
    val selectedTitle by viewModel.selectedPointTitle.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val distanceUnit by viewModel.distanceUnit.collectAsStateWithLifecycle()
    val defaultRadius by viewModel.defaultRadius.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var showAttributionDialog by remember { mutableStateOf(false) }
    var showFgDisclosure by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun checkHasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            locationOverlayRef?.enableMyLocation()
            locationOverlayRef?.enableFollowLocation()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.updateCurrentLocation(loc)
                        mapViewRef?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                    }
                }
        }
    }

    fun requestLocationWithDisclosure() {
        if (checkHasLocationPermission()) {
            locationOverlayRef?.enableMyLocation()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.updateCurrentLocation(loc)
                        mapViewRef?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                    }
                }
        } else {
            showFgDisclosure = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. OSM Map Canvas
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(MapConfig.DEFAULT_ZOOM)
                    controller.setCenter(GeoPoint(MapConfig.DEFAULT_LATITUDE, MapConfig.DEFAULT_LONGITUDE))
                    minZoomLevel = MapConfig.MIN_ZOOM
                    maxZoomLevel = MapConfig.MAX_ZOOM

                    // Touch events for single tap & long press to select location
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                viewModel.onMapPointTapped(p, "Selected Pin")
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                viewModel.onMapPointTapped(p, "Pinned Place")
                            }
                            return true
                        }
                    })
                    overlays.add(eventsOverlay)

                    // My Location Overlay
                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    if (checkHasLocationPermission()) {
                        locationOverlay.enableMyLocation()
                    }
                    overlays.add(locationOverlay)
                    locationOverlayRef = locationOverlay
                    mapViewRef = this
                }
            },
            update = { mapView ->
                // Keep base overlays (events & my location)
                val baseOverlays = mapView.overlays.take(2)
                mapView.overlays.clear()
                mapView.overlays.addAll(baseOverlays)

                // Draw circles and markers for saved alarms
                allAlarms.forEach { alarm ->
                    // Alarm Radius circle
                    val circle = Polygon.pointsAsCircle(
                        GeoPoint(alarm.latitude, alarm.longitude),
                        alarm.radiusMeters.toDouble()
                    )
                    val color = if (alarm.isEnabled) {
                        AndroidColor.argb(38, 29, 78, 216) // Light translucent blue
                    } else {
                        AndroidColor.argb(20, 100, 116, 139) // Translucent grey
                    }
                    val strokeColor = if (alarm.isEnabled) {
                        AndroidColor.argb(180, 29, 78, 216)
                    } else {
                        AndroidColor.argb(100, 100, 116, 139)
                    }
                    val polygon = Polygon(mapView).apply {
                        points = circle
                        fillPaint.color = color
                        fillPaint.style = Paint.Style.FILL
                        outlinePaint.color = strokeColor
                        outlinePaint.strokeWidth = 3f
                    }
                    mapView.overlays.add(polygon)

                    // Alarm Marker
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(alarm.latitude, alarm.longitude)
                        title = alarm.title
                        snippet = "${alarm.radiusMeters} m · ${alarm.triggerType}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            viewModel.openEditAlarm(alarm)
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }

                // Draw circle and marker for currently selected point
                selectedPoint?.let { pt ->
                    val circle = Polygon.pointsAsCircle(pt, defaultRadius.toDouble())
                    val polygon = Polygon(mapView).apply {
                        points = circle
                        fillPaint.color = AndroidColor.argb(45, 217, 119, 6) // Warm orange radius
                        fillPaint.style = Paint.Style.FILL
                        outlinePaint.color = AndroidColor.argb(220, 217, 119, 6)
                        outlinePaint.strokeWidth = 4f
                    }
                    mapView.overlays.add(polygon)

                    val selectedMarker = Marker(mapView).apply {
                        position = pt
                        title = selectedTitle.ifBlank { "Selected Point" }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(selectedMarker)
                }

                mapView.invalidate()
            }
        )

        // When selectedPoint changes, animate map center smoothly
        LaunchedEffect(selectedPoint) {
            selectedPoint?.let { pt ->
                mapViewRef?.controller?.animateTo(pt)
            }
        }

        // 2. Search Bar at Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text(stringResource(R.string.map_search_hint)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            }

            // Search Results List
            AnimatedVisibility(visible = searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(searchResults) { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSearchResultSelected(place) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = place.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Floating Action Buttons (Right side: My Location)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selectedPoint != null) 140.dp else 40.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = { requestLocationWithDisclosure() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.map_my_location)
                )
            }
        }

        // 4. Attribution button at bottom left
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = if (selectedPoint != null) 140.dp else 40.dp)
                .clickable { showAttributionDialog = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.map_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 5. Bottom Card when a point is selected
        selectedPoint?.let { pt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedTitle.ifBlank { stringResource(R.string.map_selected_point) },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val distanceAway = currentLocation?.let { loc ->
                                val dist = MapHelper.calculateDistanceMeters(
                                    loc.latitude,
                                    loc.longitude,
                                    pt.latitude,
                                    pt.longitude
                                )
                                MapHelper.formatDistance(dist, distanceUnit)
                            }
                            if (distanceAway != null) {
                                Text(
                                    text = distanceAway,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.clearSelectedMapPoint() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.openCreateAlarm(pt, selectedTitle) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.map_set_alarm_button))
                    }
                }
            }
        }
    }

    // Attribution Dialog
    if (showAttributionDialog) {
        AlertDialog(
            onDismissRequest = { showAttributionDialog = false },
            title = { Text(text = "Map Data & Attribution") },
            text = {
                Column {
                    Text(
                        text = "Map data is provided by OpenStreetMap contributors under the Open Database License (ODbL).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "© OpenStreetMap contributors\nhttps://www.openstreetmap.org/copyright",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(MapConfig.ATTRIBUTION_URL))
                        context.startActivity(browserIntent)
                        showAttributionDialog = false
                    }
                ) {
                    Text("Visit OpenStreetMap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttributionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Foreground Location Disclosure Dialog
    if (showFgDisclosure) {
        ForegroundLocationDisclosureDialog(
            onConfirm = {
                showFgDisclosure = false
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismiss = { showFgDisclosure = false }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDetach()
        }
    }
}
