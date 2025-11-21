package com.destiny.when2go.network

import com.destiny.when2go.idl.NearestLrDeparturesResponse

interface ApiRepository {

    suspend fun getNearestDepartures(
        latitude: Double,
        longitude: Double,
        stopLatitude: Double = 0.0,
        stopLongitude: Double = 0.0
    ): NearestLrDeparturesResponse?
}