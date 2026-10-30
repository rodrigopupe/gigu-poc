package com.rpstudio.gigu_poc

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.math.BigDecimal

class RideAccessibilityService : AccessibilityService() {

    // M5: valores fixos. No M6 isso vira config persistida.
    private val monitoredPackages = setOf("com.rpstudio.gigu_poc")
    private val costPerKm = BigDecimal("1.50")
    private val minNetPerKm = BigDecimal("1.00")

    private val handler = Handler(Looper.getMainLooper())
    private val scanRunnable = Runnable { scan() }
    private var scanPending = false

    private var overlay: OverlayCard? = null
    private var lastOffer: RideOffer? = null
    private var nodesVisited = 0

    override fun onServiceConnected() {
        overlay = OverlayCard(this)
        Bus.emit("serviço conectado")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in monitoredPackages) return

        // N1: throttle com trailing edge. O 1º evento agenda um scan; os que chegam
        // dentro da janela são ignorados, e o scan lê o estado mais recente da tela.
        if (scanPending) return
        scanPending = true
        handler.postDelayed(scanRunnable, SCAN_THROTTLE_MS)
    }

    private fun scan() {
        scanPending = false

        val root = rootInActiveWindow ?: return
        val started = System.nanoTime()
        nodesVisited = 0
        val texts = mutableListOf<String>()
        try {
            // N6: confere o pacote da janela lida, não só o do evento.
            if (root.packageName?.toString() in monitoredPackages) {
                collectTexts(root, texts, depth = 0)
            }
        } finally {
            root.recycle()
        }

        val offer = RideParser.parse(texts)
        val ms = "%.1f".format((System.nanoTime() - started) / 1_000_000.0)

        // N1: mesma oferta (ou continua sem oferta) -> nada a fazer.
        if (offer == lastOffer) return
        lastOffer = offer

        if (offer == null) {
            overlay?.hide()
            Bus.emit("scan: $nodesVisited nós, $ms ms, sem oferta")
            return
        }

        val verdict = RideParser.evaluate(offer, costPerKm, minNetPerKm)
        overlay?.show(offer, verdict)
        val label = if (verdict.worth) "VALE A PENA" else "NÃO COMPENSA"
        Bus.emit(
            "scan: $nodesVisited nós, $ms ms | $label | sobra ${verdict.netBrl.toBrl()} " +
                "(${verdict.netPerKm.toBrl()}/km)"
        )
    }

    private fun collectTexts(node: AccessibilityNodeInfo, out: MutableList<String>, depth: Int) {
        if (depth > MAX_DEPTH || nodesVisited >= MAX_NODES) return
        nodesVisited++

        node.text?.takeIf { it.isNotBlank() }?.let { out.add(it.toString()) }
        node.contentDescription?.takeIf { it.isNotBlank() }?.let { out.add(it.toString()) }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                collectTexts(child, out, depth + 1)
            } finally {
                child.recycle()
            }
        }
    }

    // N2: nada pode sobrar agendado nem desenhado quando o serviço sai de cena.
    private fun cancelWork() {
        handler.removeCallbacks(scanRunnable)
        scanPending = false
        overlay?.hide()
        lastOffer = null
    }

    override fun onInterrupt() {
        cancelWork()
        Bus.emit("serviço interrompido")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        cancelWork()
        overlay = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cancelWork()
        overlay = null
        super.onDestroy()
    }

    private companion object {
        const val MAX_DEPTH = 15
        const val MAX_NODES = 500
        const val SCAN_THROTTLE_MS = 300L
    }
}
