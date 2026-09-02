package com.pizzza.pizzzaDrive.repository.di

import com.pizzza.pizzzaDrive.repository.network.exception.CompleteErrorModel
import com.pizzza.pizzzaDrive.repository.network.exception.UiTayApiException
import com.pizzza.pizzzaDrive.repository.network.exception.UnAuthorizedException
import com.pizzza.pizzzaDrive.repository.network.manager.InstantSerializer
import com.pizzza.pizzzaDrive.repository.network.KmmService
import com.pizzza.pizzzaDrive.repository.network.WebSocketManager
import com.pizzza.pizzzaDrive.requestLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jsonLenient = Json { ignoreUnknownKeys = true }

val networkModule = module {
    // Cliente para peticiones REST (con ContentNegotiation y validación)
    single(named("httpClient")) {
        HttpClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                        serializersModule = SerializersModule {
                            contextual(Instant::class, InstantSerializer)
                        }
                    },
                )
            }

            defaultRequest {
                headers.append("ngrok-skip-browser-warning", "true")
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        val statusCode = response.status.value
                        val errorText = try { response.bodyAsText() } catch (e: Exception) { "" }
                        
                        when (statusCode) {
                            //401 -> throw UnAuthorizedException()
                            in 400..599 -> {
                                val errorModel = try {
                                    jsonLenient.decodeFromString<CompleteErrorModel>(errorText)
                                } catch (e: Exception) {
                                    null
                                }
                                throw UiTayApiException(
                                    code = statusCode,
                                    title = errorModel?.title ?: "Error $statusCode",
                                    messageApi = errorModel?.errorMessage ?: errorText.takeIf { it.isNotBlank() } ?: "Ocurrió un error inesperado"
                                )
                            }
                        }
                    }
                }
            }

            install(Logging) {
                logger = requestLogger
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
        }
    }

    single { KmmService(get(named("httpClient"))) }
    
    single(named("webSocketClient")) {
        HttpClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            // No instalamos Logging aquí para evitar conflictos con WebSockets
        }
    }

    single { WebSocketManager(get(named("webSocketClient"))) }
}

