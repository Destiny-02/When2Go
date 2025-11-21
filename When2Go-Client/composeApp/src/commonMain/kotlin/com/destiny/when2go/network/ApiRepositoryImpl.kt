package com.destiny.when2go.network

import com.destiny.when2go.idl.Coord
import com.destiny.when2go.idl.NearestLrDeparturesRequest
import com.destiny.when2go.idl.NearestLrDeparturesResponse


class ApiRepositoryImpl(
    private val apiService: ApiService,
) : ApiRepository {

    override suspend fun getNearestDepartures(
        latitude: Double,
        longitude: Double,
        stopLatitude: Double,
        stopLongitude: Double
    ): NearestLrDeparturesResponse? {
        val body = NearestLrDeparturesRequest(
            coord = Coord(latitude, longitude),
            stop_coord = Coord(stopLatitude, stopLongitude),
        )


        val response = apiService.getNearestDepartures(body)
        return when (response) {
            is NetworkResult.Success -> {
                response.data
            }
            else -> {
                null
            }
        }
    }
}