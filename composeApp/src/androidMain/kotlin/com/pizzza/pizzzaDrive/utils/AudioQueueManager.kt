package com.pizzza.pizzzaDrive.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.pizzza.pizzzaDrive.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AudioQueueManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var soundPool: SoundPool? = null
    private var soundId: Int = -1
    private var isLoaded = false

    init {
        println("UI_TAG_DRIVER: AudioQueueManager - Inicializando SoundPool...")
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
                println("UI_TAG_DRIVER: AudioQueueManager - Archivo de audio cargado y listo.")
            } else {
                println("⚠️ AudioQueueManager - Error al cargar audio (status $status)")
            }
        }

        // Cargar el audio
        try {
            soundId = soundPool?.load(context, R.raw.new_order, 1) ?: -1
        } catch (e: Exception) {
            println("⚠️ AudioQueueManager - Excepción al cargar: ${e.message}")
        }
    }

    fun enqueueAudio() {
        scope.launch {
            println("UI_TAG_DRIVER: AudioQueueManager - Intentando reproducir...")
            
            // Esperar un poco si no ha terminado de cargar (solo la primera vez)
            var retry = 0
            while (!isLoaded && retry < 5) {
                delay(200)
                retry++
            }

            if (isLoaded && soundPool != null) {
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
                println("UI_TAG_DRIVER: AudioQueueManager - ¡Reproduciendo ahora!")
            } else {
                println("⚠️ AudioQueueManager - No se pudo reproducir (isLoaded=$isLoaded)")
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
