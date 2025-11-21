package com.destiny.when2go.idl

import kotlinx.serialization.Serializable

@Serializable
data class NearestLrDeparturesResponse(
    val stop: Stop,
    val all_stops: List<Stop>
)

@Serializable
data class Stop(
    val id: String,
    val name: String,
    val stop_points: List<StopPoint>,
    val distance_meters: Long, // Only populated for the current stop
    val distance_time: Long // Only populated for the current stop
)

@Serializable
data class StopPoint(
    val id: String,
    val coord: Coord,
    val departures: List<Departure>? // Only populated for the current stop
)

@Serializable
data class Coord(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class Departure(
    val time: Long,
    val is_real_time: Boolean,
    val description: String,
    val destination: String
)
