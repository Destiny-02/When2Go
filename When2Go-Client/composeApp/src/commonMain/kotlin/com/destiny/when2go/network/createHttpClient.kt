package com.destiny.when2go.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient() = HttpClient {
    install(Logging) {
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                isLenient = true
            }
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15000 // 15 seconds
        connectTimeoutMillis = 15000 // 15 seconds
        socketTimeoutMillis = 15000 // 15 seconds
    }
}