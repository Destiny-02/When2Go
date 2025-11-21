package com.destiny.when2go.network

import com.destiny.when2go.idl.NearestLrDeparturesRequest
import com.destiny.when2go.idl.NearestLrDeparturesResponse
import com.destiny.when2go.util.serverUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface ApiService {
    suspend fun getNearestDepartures(body: NearestLrDeparturesRequest): NetworkResult<NearestLrDeparturesResponse>
}

class ApiServiceImpl(
    private val httpClient: HttpClient,
) : ApiService {
    override suspend fun getNearestDepartures(body: NearestLrDeparturesRequest): NetworkResult<NearestLrDeparturesResponse> =
        httpClient.safeRequest {
            post(serverUrl + "/nearest-lr-departures") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
}