package com.pizzza.pizzzaDrive.repository.network

import com.pizzza.pizzzaDrive.shared.BuildConfig
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.header
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class SocketIdentify(
    val type: String = "IDENTIFY",
    val role: String = "DRIVER",
    val driverId: String = "1"
)

@Serializable
private data class SocketMessage(
    val type: String? = null,
    val status: String? = null,
    val message: String? = null
)

class WebSocketManager(private val client: HttpClient) {
    private val _refreshOrders = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshOrders = _refreshOrders.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isRunning = false

    fun connect() {
        if (isRunning) return
        isRunning = true
        
        scope.launch {
            while (isActive) {
                try {
                    val hostUrl = if (BuildConfig.IS_DEBUG) BuildConfig.BASE_URL_SERVICE_DEV else BuildConfig.BASE_URL_SERVICE
                    
                    // Extraer host y protocolo
                    val isSecure = hostUrl.startsWith("https")
                    val cleanHost = hostUrl
                        .replace("https://", "")
                        .replace("http://", "")
                        .removeSuffix("/")

                    println("UI_TAG_DRIVER: WebSocket: Intentando conectar a $cleanHost (Secure: $isSecure)")

                    client.webSocket(
                        method = io.ktor.http.HttpMethod.Get,
                        host = cleanHost,
                        path = "/", // Por defecto a la raíz según el código del servidor
                        request = {
                            if (isSecure) {
                                url.protocol = io.ktor.http.URLProtocol.WSS
                            } else {
                                url.protocol = io.ktor.http.URLProtocol.WS
                            }
                            header("ngrok-skip-browser-warning", "true")
                        }
                    ) {
                        println("UI_TAG_DRIVER: WebSocket: Conectado exitosamente.")
                        
                        // 1. Enviar IDENTIFY
                        val identify = SocketIdentify()
                        val identifyJson = Json.encodeToString(identify)
                        send(Frame.Text(identifyJson))
                        println("UI_TAG_DRIVER: WebSocket: Identificación enviada: $identifyJson")

                        // 2. Escuchar mensajes
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                println("UI_TAG_DRIVER: WebSocket: Mensaje recibido: $text")
                                
                                try {
                                    val msg = Json { ignoreUnknownKeys = true }.decodeFromString<SocketMessage>(text)
                                    
                                    if (msg.type == "REFRESH_DRIVER_ORDERS") {
                                        println("UI_TAG_DRIVER: WebSocket: ¡Señal de refresco detectada!")
                                        _refreshOrders.tryEmit(Unit)
                                    } else if (msg.status == "OK") {
                                        println("UI_TAG_DRIVER: WebSocket: Servidor confirmó identificación: ${msg.message}")
                                    }
                                } catch (e: Exception) {
                                    println("UI_TAG_DRIVER: WebSocket: Error al procesar mensaje: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    println("UI_TAG_DRIVER: WebSocket: Error en la conexión: ${e.message}. Reintentando en 5s...")
                }
                delay(5000)
            }
        }
    }

    fun close() {
        isRunning = false
        scope.cancel()
    }
}
