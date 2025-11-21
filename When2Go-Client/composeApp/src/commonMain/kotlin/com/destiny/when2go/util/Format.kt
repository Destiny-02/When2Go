package com.destiny.when2go.util

import com.destiny.when2go.model.DepartureInfo
import com.destiny.when2go.idl.Stop
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun calculateMinUntil(timeEpochSeconds: Long): Long {
    val currentEpochSeconds = Clock.System.now().epochSeconds
    return (timeEpochSeconds - currentEpochSeconds) / 60
}

@OptIn(ExperimentalTime::class)
fun calculateSecondsUntil(timeEpochSeconds: Long): Long {
    return timeEpochSeconds - (Clock.System.now().epochSeconds)
}

fun Stop.toDepartureInfos(index: Int = 0): List<DepartureInfo> {
    return this.stop_points.getOrNull(index)?.departures?.map { departure ->
        DepartureInfo(
            travellingTo = departure.destination,
            departingIn = calculateMinUntil(departure.time),
            isRealtime = departure.is_real_time,
            leaveIn = calculateMinUntil(departure.time - this.distance_time)
        )
    } ?: emptyList()
}

fun distBetweenCoords(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371e3 // in meters
    val lat1Rad = toRadians(lat1)
    val lat2Rad = toRadians(lat2)
    val deltaLat = toRadians(lat2 - lat1)
    val deltaLon = toRadians(lon2 - lon1)

    val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLon / 2) * sin(deltaLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}

private fun toRadians(deg: Double): Double = deg / 180.0 * PI