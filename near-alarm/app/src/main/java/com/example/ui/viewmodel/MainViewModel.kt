package com.example.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AlarmHistory
import com.example.data.model.DistanceUnit
import com.example.data.model.LocationAlarm
import com.example.data.model.SearchResultPlace
import com.example.data.repository.AlarmRepository
import com.example.data.search.NominatimClient
import com.example.geofence.GeofenceManager
import com.example.map.MapHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MainViewModel(
    private val repository: AlarmRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    val allAlarms: StateFlow<List<LocationAlarm>> = repository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAlarms: StateFlow<List<LocationAlarm>> = allAlarms
        .map { list -> list.filter { it.isEnabled } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inactiveAlarms: StateFlow<List<LocationAlarm>> = allAlarms
        .map { list -> list.filter { !it.isEnabled } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<AlarmHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distanceUnit: StateFlow<DistanceUnit> = repository.settings.distanceUnit
    val defaultRadius: StateFlow<Int> = repository.settings.defaultRadius
    val soundEnabled: StateFlow<Boolean> = repository.settings.soundEnabled
    val vibrationEnabled: StateFlow<Boolean> = repository.settings.vibrationEnabled

    // User's live location on map (updated when map is active)
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    // Currently selected point on map (from pin drop or search result)
    private val _selectedMapPoint = MutableStateFlow<GeoPoint?>(null)
    val selectedMapPoint: StateFlow<GeoPoint?> = _selectedMapPoint.asStateFlow()

    private val _selectedPointTitle = MutableStateFlow("")
    val selectedPointTitle: StateFlow<String> = _selectedPointTitle.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultPlace>>(emptyList())
    val searchResults: StateFlow<List<SearchResultPlace>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Alarm creation / edit dialog state
    private val _alarmBeingEdited = MutableStateFlow<LocationAlarm?>(null)
    val alarmBeingEdited: StateFlow<LocationAlarm?> = _alarmBeingEdited.asStateFlow()

    private val _isAlarmDialogOpen = MutableStateFlow(false)
    val isAlarmDialogOpen: StateFlow<Boolean> = _isAlarmDialogOpen.asStateFlow()

    // Prominent disclosures state
    private val _showBgDisclosure = MutableStateFlow(false)
    val showBgDisclosure: StateFlow<Boolean> = _showBgDisclosure.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Automatically sync geofences whenever activeAlarms changes
        viewModelScope.launch {
            activeAlarms.collect { activeList ->
                geofenceManager.syncGeofences(activeList)
            }
        }
    }

    fun updateCurrentLocation(loc: Location?) {
        _currentLocation.value = loc
    }

    fun onMapPointTapped(geoPoint: GeoPoint, defaultTitle: String = "Selected Location") {
        _selectedMapPoint.value = geoPoint
        _selectedPointTitle.value = defaultTitle
    }

    fun clearSelectedMapPoint() {
        _selectedMapPoint.value = null
        _selectedPointTitle.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        // Polite rate-limiting debounce of 800ms
        searchJob = viewModelScope.launch {
            delay(800)
            _isSearching.value = true
            val result = NominatimClient.searchPlaces(query)
            _searchResults.value = result.getOrDefault(emptyList())
            _isSearching.value = false
        }
    }

    fun onSearchResultSelected(place: SearchResultPlace) {
        _selectedMapPoint.value = GeoPoint(place.latitude, place.longitude)
        // Clean short title
        val shortName = place.displayName.split(",").firstOrNull()?.trim() ?: place.displayName
        _selectedPointTitle.value = shortName
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun openCreateAlarm(point: GeoPoint? = _selectedMapPoint.value, title: String = _selectedPointTitle.value) {
        val lat = point?.latitude ?: 37.5665
        val lon = point?.longitude ?: 126.9780
        val alarmTitle = if (title.isNotBlank()) title else "My Location Alarm"

        _alarmBeingEdited.value = LocationAlarm(
            title = alarmTitle,
            latitude = lat,
            longitude = lon,
            radiusMeters = defaultRadius.value
        )
        _isAlarmDialogOpen.value = true
    }

    fun openEditAlarm(alarm: LocationAlarm) {
        _alarmBeingEdited.value = alarm
        _isAlarmDialogOpen.value = true
    }

    fun closeAlarmDialog() {
        _alarmBeingEdited.value = null
        _isAlarmDialogOpen.value = false
    }

    fun saveAlarm(alarm: LocationAlarm) {
        viewModelScope.launch {
            repository.saveAlarm(alarm)
            closeAlarmDialog()
            clearSelectedMapPoint()
        }
    }

    fun toggleAlarm(alarm: LocationAlarm, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm.id, isEnabled)
        }
    }

    fun deleteAlarm(alarm: LocationAlarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm.id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteAllSavedPlaces() {
        viewModelScope.launch {
            repository.deleteAllAlarms()
            clearSelectedMapPoint()
        }
    }

    fun deleteAllAppData() {
        viewModelScope.launch {
            repository.deleteAllAppData()
            clearSelectedMapPoint()
        }
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        repository.settings.setDistanceUnit(unit)
    }

    fun setDefaultRadius(radius: Int) {
        repository.settings.setDefaultRadius(radius)
    }

    fun setSoundEnabled(enabled: Boolean) {
        repository.settings.setSoundEnabled(enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.settings.setVibrationEnabled(enabled)
    }

    fun showBackgroundDisclosure(show: Boolean) {
        _showBgDisclosure.value = show
    }

    fun markBgDisclosureSeen() {
        repository.settings.setBgDisclosureSeen(true)
        _showBgDisclosure.value = false
    }

    fun calculateDistanceToAlarm(alarm: LocationAlarm): Double? {
        val curLoc = _currentLocation.value ?: return null
        return MapHelper.calculateDistanceMeters(
            curLoc.latitude,
            curLoc.longitude,
            alarm.latitude,
            alarm.longitude
        )
    }

    class Factory(
        private val repository: AlarmRepository,
        private val geofenceManager: GeofenceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, geofenceManager) as T
        }
    }
}
