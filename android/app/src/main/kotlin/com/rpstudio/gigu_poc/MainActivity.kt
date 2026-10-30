package com.rpstudio.gigu_poc

import android.content.ComponentName
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

    private fun isServiceEnabled(): Boolean {
        val componentName = ComponentName(this, RideAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        return enabledServices
            .orEmpty()
            .split(":").any { it == componentName }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val messenger = flutterEngine.dartExecutor.binaryMessenger

        MethodChannel(messenger, "gigu_poc/control").setMethodCallHandler { call, result ->
            when (call.method) {
                "isServiceEnabled" -> result.success(isServiceEnabled())
                "openAccessibilitySettings" -> {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    result.success(null)
                }
                "getLog" -> result.success(Bus.snapshot())
                "emitTest" -> {
                    val formattedDate = formatTimeStamp(System.currentTimeMillis())
                    Bus.emit("Teste $formattedDate")
                    result.success(null)
                }
                "emitDelayed" -> {
                    Thread {
                        Thread.sleep(8000)
                        val formattedDate = formatTimeStamp(System.currentTimeMillis())
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

    private fun formatTimeStamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
