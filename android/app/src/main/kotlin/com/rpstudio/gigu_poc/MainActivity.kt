package com.rpstudio.gigu_poc

import android.content.Intent
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val messenger = flutterEngine.dartExecutor.binaryMessenger

        MethodChannel(messenger, "gigu_poc/control").setMethodCallHandler { call, result ->
            when (call.method) {
                // Ainda não existe serviço; no M2 isso passa a consultar o sistema.
                "isServiceEnabled" -> result.success(false)
                "openAccessibilitySettings" -> {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    result.success(null)
                }
                "getLog" -> result.success(Bus.snapshot())
                "emitTest" -> {
                    val timestamp = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val formattedDate = sdf.format(Date(timestamp))
                    Bus.emit("Teste $formattedDate")
                    result.success(null)
                }
                "emitDelayed" -> {
                    Thread {
                        Thread.sleep(8000)
                        val timestamp = System.currentTimeMillis()
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        val formattedDate = sdf.format(Date(timestamp))
                        Bus.emit("Atrasado $formattedDate")
                    }.start()
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }

        EventChannel(messenger, "gigu_poc/events").setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                    Bus.attach(events)
                }

                override fun onCancel(arguments: Any?) {
                    Bus.detach()
                }
            }
        )
    }
}
