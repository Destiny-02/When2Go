package com.destiny.when2go.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.destiny.when2go.location.getLocator
import com.destiny.when2go.model.DepartureInfo
import com.destiny.when2go.network.ApiRepository
import com.destiny.when2go.network.ApiRepositoryImpl
import com.destiny.when2go.network.ApiServiceImpl
import com.destiny.when2go.idl.Stop
import com.destiny.when2go.network.createHttpClient
import com.destiny.when2go.preferences.PreferencesStore
import com.destiny.when2go.util.calculateSecondsUntil
import com.destiny.when2go.util.toDepartureInfos
import com.destiny.when2go.view.showToast
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ApiRepository,
) : ViewModel() {
    private val preferencesStore = PreferencesStore()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _nextDepartures = MutableStateFlow<List<DepartureInfo>>(emptyList())
    val nextDepartures: StateFlow<List<DepartureInfo>> = _nextDepartures

    private val _stopName = MutableStateFlow("")
    val stopName: StateFlow<String> = _stopName

    private val _stopDistanceMins = MutableStateFlow(0)
    val stopDistanceMins: StateFlow<Int> = _stopDistanceMins

    private val _nextLeaveTimeMins = MutableStateFlow(0)
    val nextLeaveTimeMins: StateFlow<Int> = _nextLeaveTimeMins

    private val _targetDepartureRow = MutableStateFlow(0)
    val targetDepartureRow: StateFlow<Int> = _targetDepartureRow

    private var allStops: List<Stop> = emptyList()
    private val _stopNames: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val stopNames: StateFlow<List<String>> = _stopNames

    private var stop: Stop? = null

    private var spotPointIndex = 0

    private var latitude = 0.0
    private var longitude = 0.0

    init {
        getNextDepartures()

        viewModelScope.launch {
            while (true) {
                delay(30000L)
                setNextDepartures()
            }
        }
    }

    fun getNextDepartures(stopIndex: Int = -1) {
        _isLoading.value = true
        viewModelScope.launch { // Get current location
            val location = Geolocator(getLocator()).current(Priority.HighAccuracy)
            location.onSuccess {
                latitude = it.coordinates.latitude
                longitude = it.coordinates.longitude

                val (stopLatitude, stopLongitude) = getStopCoordForRequest(stopIndex)
                viewModelScope.launch {
                    performGetNextDeparturesRequest(stopLatitude, stopLongitude)
                }
            }
            location.onFailed {
                showToast("Something went wrong. Please try again. ")
                _isLoading.value = false
            }
        }
    }

    private fun getStopCoordForRequest(stopIndex: Int): Pair<Double, Double> {
        var stopLatitude = latitude
        var stopLongitude = longitude
        if (stopIndex >= 0) { // Selected different stop via dropdown
            stopLatitude = allStops.getOrNull(stopIndex)?.stop_points?.getOrNull(0)?.coord?.latitude ?: latitude
            stopLongitude = allStops.getOrNull(stopIndex)?.stop_points?.getOrNull(0)?.coord?.longitude ?: longitude
            preferencesStore.saveDropdownPreference(latitude, longitude, stopLatitude, stopLongitude)
        } else { // Initial request or refresh, check if preference exists
            preferencesStore.getDropdownPreference(latitude, longitude)?.let {
                stopLatitude = it.first
                stopLongitude = it.second
            }
        }
        return Pair(stopLatitude, stopLongitude)
    }

    private suspend fun performGetNextDeparturesRequest(
        stopLatitude: Double,
        stopLongitude: Double
    ) {
        val result = repository.getNearestDepartures(latitude = latitude, longitude = longitude, stopLatitude = stopLatitude, stopLongitude = stopLongitude)
        if (result != null && result.all_stops.isNotEmpty()) {
            stop = result.stop
            _stopName.value = result.stop.name
            _stopDistanceMins.value = (result.stop.distance_time / 60).toInt()
            allStops = result.all_stops
            _stopNames.value = allStops.map { stop -> stop.name }
            setStopPointIndex()
            setNextDepartures()
            _isLoading.value = false
        } else {
            showToast("Something went wrong. Please try again. ")
            _isLoading.value = false
        }
    }

    fun switchSpotPoint() {
        spotPointIndex = (spotPointIndex + 1) % (stop?.stop_points?.size ?: 1).coerceAtLeast(1)
        setNextDepartures()

        // Save preferences
        val stopId = stop?.id
        val stopPointId = stop?.stop_points?.getOrNull(spotPointIndex)?.id
        if (stopId != null && stopPointId != null) {
            preferencesStore.saveStopPointPreference(stopId, stopPointId)
        }
    }

    private fun setStopPointIndex() {
        val stopId = stop?.id ?: return
        val stopPointId = preferencesStore.getStopPointPreference(stopId) ?: return

        val indexFromPref = stop?.stop_points?.indexOfFirst { it.id == stopPointId } ?: return

        spotPointIndex = indexFromPref
    }

    fun clearPreferences() {
        preferencesStore.clearAllPreferences()
    }

    private fun setNextDepartures() {
        val stop = stop ?: return
        _nextDepartures.value = stop.toDepartureInfos(spotPointIndex).take(10)

        val departures = stop.stop_points.getOrNull(spotPointIndex)?.departures
        _targetDepartureRow.value = departures?.indexOfFirst {
            calculateSecondsUntil(it.time) - stop.distance_time >= 0
        } ?: -1
        var latestLeaveTimeSeconds = 0L
        if (_targetDepartureRow.value >= 0) {
            latestLeaveTimeSeconds =
                calculateSecondsUntil(
                    departures?.getOrNull(_targetDepartureRow.value)?.time ?: 0
                ) - (stop.distance_time)
        }
        _nextLeaveTimeMins.value = (latestLeaveTimeSeconds / 60).toInt()

    }
}

val mainViewModelFactory = viewModelFactory {
    initializer {
        MainViewModel(repository = getRepository())
    }
}

fun getRepository(): ApiRepository = ApiRepositoryImpl(ApiServiceImpl(createHttpClient()))
