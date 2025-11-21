package com.destiny.when2go.preferences

import kotlinx.serialization.Serializable

@Serializable
data class DropdownPreference(
    val locations: MutableMap<Coord, Coord> // Current location to stop location
)

@Serializable
data class StopPointPreference(
    val stops: MutableMap<String, String> // Stop to preffered stop point
)

@Serializable
data class Coord(
    val latitude: Double,
    val longitude: Double
)