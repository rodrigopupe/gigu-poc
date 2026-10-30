package com.rpstudio.gigu_poc

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.EventChannel

/**
 * Ponte em memória entre o serviço (produtor) e o Flutter (consumidor).
 * Vive no mesmo processo; morre junto com ele.
 */
object Bus {
    private const val MAX_LOG = 200

    private val mainHandler = Handler(Looper.getMainLooper())
    private val log = ArrayDeque<String>()

    // Só é não-nulo enquanto o Dart está escutando (onListen..onCancel).
    @Volatile
    private var sink: EventChannel.EventSink? = null

    fun attach(sink: EventChannel.EventSink) {
        this.sink = sink
    }

    fun detach() {
        this.sink = null
    }

    fun snapshot(): List<String> = synchronized(log) { log.toList() }

    fun emit(message: String) {
        synchronized(log) {
            log.apply {
                add(message)
                if (size > MAX_LOG) removeFirst()
            }

        }

        mainHandler.post {
            sink?.success(message)
        }
    }
}
