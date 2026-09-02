package com.pizzza.pizzzaDrive.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pizzza.pizzzaDrive.R
import com.pizzza.pizzzaDrive.usecases.DataUseCase
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.milliseconds

class TrackingService : Service() {

    private val dataUseCase: DataUseCase by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null
    
    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    
    private val CHANNEL_ID = "tracking_service_channel"
    private val NOTIFICATION_ID = 1001

    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        println("UI_TAG_DRIVER: TrackingService: Servicio creado.")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("UI_TAG_DRIVER: TrackingService: onStartCommand recibido.")
        
        if (!isForeground) {
            val notification = createNotification("Iniciando rastreo de entrega...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForeground = true
        }
        
        if (trackingJob?.isActive != true) {
            startTrackingLoop()
        }
        
        return START_STICKY
    }

    private fun startTrackingLoop() {
        println("UI_TAG_DRIVER: TrackingService: Iniciando bucle de seguimiento cada 2 minutos.")
        trackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val activeOrders = dataUseCase.loadParentOrder(forceRefresh = false)
                        .filter { it.state.trim().uppercase() == "INICIADO" }

                    println("UI_TAG_DRIVER: TrackingService: ${activeOrders.size} órdenes en estado 'INICIADO' encontradas en DB local.")

                    if (activeOrders.isEmpty()) {
                        println("UI_TAG_DRIVER: TrackingService: No se detectaron órdenes activas. Deteniendo servicio automáticamente.")
                        stopForeground(true)
                        isForeground = false
                        stopSelf()
                        break
                    }

                    // 2. Obtener ubicación actual nativa
                    val currentLocation = getCurrentLocationNative()
                    
                    if (currentLocation != null) {
                        val distance = if (lastLocation != null) calculateDistance(lastLocation!!, currentLocation) else Float.MAX_VALUE
                        println("UI_TAG_DRIVER: TrackingService: Ubicación obtenida (${currentLocation.latitude}, ${currentLocation.longitude}). Distancia desde último punto: ${distance}m")

                        if (distance >= 10) {
                            println("UI_TAG_DRIVER: TrackingService: Umbral de 20m superado. Sincronizando con el servidor...")
                            
                            activeOrders.forEach { order ->
                                try {
                                    val updatedOrder = order.copy(
                                        currentLatitude = currentLocation.latitude.toString(),
                                        currentLongitude = currentLocation.longitude.toString()
                                    )
                                    dataUseCase.updateOrder(updatedOrder)
                                    println("UI_TAG_DRIVER: TrackingService: Orden ${order.uid} actualizada exitosamente.")
                                } catch (e: Exception) {
                                    println("⚠️ TrackingService: Error al actualizar orden ${order.uid} en servidor: ${e.message}")
                                }
                            }
                            lastLocation = currentLocation
                            updateNotification("Rastreo activo: Repartiendo ${activeOrders.size} pedidos")
                        } else {
                            println("UI_TAG_DRIVER: TrackingService: Movimiento insuficiente (< 50m). No se requiere actualización.")
                        }
                    } else {
                        println("⚠️ TrackingService: No se pudo obtener la ubicación actual.")
                    }
                } catch (e: Exception) {
                    println("⚠️ TrackingService: Error inesperado en el bucle: ${e.message}")
                }

                // 3. Esperar 2 minutos
                delay(60_000.milliseconds)
            }
        }
    }

    private fun getCurrentLocationNative(): Location? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            println("⚠️ TrackingService: Permisos de ubicación no concedidos.")
            return null
        }

        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation
        } catch (e: Exception) {
            println("⚠️ TrackingService: Excepción al obtener ubicación: ${e.message}")
            null
        }
    }

    private fun calculateDistance(loc1: Location, loc2: Location): Float {
        val results = FloatArray(1)
        Location.distanceBetween(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude, results)
        return results[0]
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Rastreo de Reparto",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pizza Driver")
            .setContentText(content)
            .setSmallIcon(R.drawable.ui_ani_drive)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        println("UI_TAG_DRIVER: TrackingService: Servicio destruido.")
        isForeground = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
