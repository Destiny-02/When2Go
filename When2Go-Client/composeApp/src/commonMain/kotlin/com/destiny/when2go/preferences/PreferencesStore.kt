package com.destiny.when2go.preferences

import com.destiny.when2go.util.distBetweenCoords
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.containsValue
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.serialization.ExperimentalSerializationApi


@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class PreferencesStore {
    companion object {
        private const val DROPDOWN_PREF_KEY = "dropdown-preference"
        private const val STOP_POINT_PREF_KEY = "stop-point-preference"
    }
    private val settings = Settings()

    init {
        val isPresent = settings.containsValue(DropdownPreference.serializer(), DROPDOWN_PREF_KEY)
                && settings.containsValue(StopPointPreference.serializer(), STOP_POINT_PREF_KEY)
        if (!isPresent) clearAllPreferences()
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun saveDropdownPreference(currLat: Double, currLong: Double, stopLat: Double, stopLong: Double) {
        val currPref = settings.decodeValueOrNull(DropdownPreference.serializer(), DROPDOWN_PREF_KEY) ?: return
        currPref.locations.keys.forEach {
            if (distBetweenCoords(
                    it.latitude,
                    it.longitude,
                    currLat,
                    currLong
                ) < 100) { // 100m
                currPref.locations.put(it, Coord(stopLat, stopLong))
                settings.encodeValue(DropdownPreference.serializer(), DROPDOWN_PREF_KEY, currPref)
                return
            }
        }
        currPref.locations.put(Coord(currLat, currLong), Coord(stopLat, stopLong))
        settings.encodeValue(DropdownPreference.serializer(), DROPDOWN_PREF_KEY, currPref)
    }

    fun getDropdownPreference(currLat: Double, currLong: Double): Pair<Double, Double>? {
        val currPref = settings.decodeValueOrNull(DropdownPreference.serializer(), DROPDOWN_PREF_KEY) ?: return null
        currPref.locations.keys.forEach {
            val dist = distBetweenCoords(
                it.latitude,
                it.longitude,
                currLat,
                currLong
            )
            if (dist < 100) { // 100m
                return currPref.locations[it]?.let { loc ->
                    Pair(loc.latitude, loc.longitude)
                }
            }
        }
        return null
    }

    fun saveStopPointPreference(stopId: String, stopPointId: String) {
        val currPref = settings.decodeValueOrNull(StopPointPreference.serializer(), STOP_POINT_PREF_KEY) ?: return
        currPref.stops.put(stopId, stopPointId)
        settings.encodeValue(StopPointPreference.serializer(), STOP_POINT_PREF_KEY, currPref)
    }

    fun getStopPointPreference(stopId: String): String? {
        val currPref = settings.decodeValueOrNull(StopPointPreference.serializer(), STOP_POINT_PREF_KEY) ?: return null
        return currPref.stops[stopId]
    }

    fun clearAllPreferences() {
        settings.encodeValue(DropdownPreference.serializer(), DROPDOWN_PREF_KEY,
            DropdownPreference(mutableMapOf()))
        settings.encodeValue(StopPointPreference.serializer(), STOP_POINT_PREF_KEY,
            StopPointPreference(mutableMapOf()))
    }
}