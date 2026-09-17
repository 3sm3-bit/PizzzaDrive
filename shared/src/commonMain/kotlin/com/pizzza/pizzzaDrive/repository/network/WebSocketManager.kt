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
data class SocketIdentify(
    val type: String = "IDENTIFY",
    val role: String = "DRIVER",
    val driverId: String
)

@Serializable
data class SocketMessage(
    val type: String? = null,
    val status: String? = null,
    val message: String? = null
)

private val jsonWorker = Json { 
    ignoreUnknownKeys = true 
    encodeDefaults = true 
}

class WebSocketManager(private val client: HttpClient) {
    private val _refreshOrders = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshOrders = _refreshOrders.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var connectionJob: Job? = null
    private var isRunning = false

    fun connect(driverId: String) {
        if (isRunning) return
        isRunning = true
        
        connectionJob?.cancel()
        connectionJob = scope.launch {
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
                    println("UI_TAG_DRIVER: WebSocket: URL completa base: $hostUrl")

                    client.webSocket(
                        method = io.ktor.http.HttpMethod.Get,
                        host = cleanHost,
                        path = "/", // Por defecto a la raíz según el código del servidor
                        request = {
                            if (isSecure) {
                                url.protocol = io.ktor.http.URLProtocol.WSS
                                println("UI_TAG_DRIVER: WebSocket: Usando protocolo WSS")
                            } else {
                                url.protocol = io.ktor.http.URLProtocol.WS
                                println("UI_TAG_DRIVER: WebSocket: Usando protocolo WS")
                            }
                            header("ngrok-skip-browser-warning", "true")
                        }
                    ) {
                        println("UI_TAG_DRIVER: WebSocket: Conexión establecida con el servidor.")
                        
                        // 1. Enviar IDENTIFY
                        val identify = SocketIdentify(driverId = driverId)
                        val identifyJson = jsonWorker.encodeToString(identify)
                        try {
                            send(Frame.Text(identifyJson))
                            println("UI_TAG_DRIVER: WebSocket: Identificación enviada: $identifyJson")
                        } catch (e: Exception) {
                            println("UI_TAG_DRIVER: WebSocket: Error al enviar identificación: ${e.message}")
                        }

                        // 2. Escuchar mensajes
                        try {
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    val text = frame.readText()
                                    println("UI_TAG_DRIVER: WebSocket: Mensaje recibido: $text")
                                    
                                    try {
                                        val msg = jsonWorker.decodeFromString<SocketMessage>(text)
                                        
                                        if (msg.type == "REFRESH_DRIVER_ORDERS") {
                                            println("UI_TAG_DRIVER: WebSocket: ¡Señal de refresco detectada!")
                                            _refreshOrders.tryEmit(Unit)
                                        } else if (msg.status == "OK") {
                                            println("UI_TAG_DRIVER: WebSocket: Servidor confirmó identificación: ${msg.message}")
                                        } else {
                                            println("UI_TAG_DRIVER: WebSocket: Mensaje no reconocido o sin acción: ${msg.type}")
                                        }
                                    } catch (e: Exception) {
                                        println("UI_TAG_DRIVER: WebSocket: Error al parsear JSON del mensaje: ${e.message}")
                                    }
                                } else {
                                    println("UI_TAG_DRIVER: WebSocket: Recibido frame de tipo no texto: ${frame::class.simpleName}")
                                }
                            }
                        } catch (e: Exception) {
                            println("UI_TAG_DRIVER: WebSocket: Error durante la escucha de mensajes: ${e.message}")
                        }
                        println("UI_TAG_DRIVER: WebSocket: La sesión de WebSocket se ha cerrado.")
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    println("UI_TAG_DRIVER: WebSocket: Fallo crítico en el bucle de conexión: ${e.message}")
                    println("UI_TAG_DRIVER: WebSocket: Causa: ${e.cause?.message}")
                    println("UI_TAG_DRIVER: WebSocket: Reintentando en 5s...")
                }
                delay(5000)
            }
        }
    }

    fun close() {
        isRunning = false
        connectionJob?.cancel()
    }
}
