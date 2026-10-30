package com.rpstudio.gigu_poc

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RideAccessibilityService : AccessibilityService() {

    // M2: só o próprio app. No M6 isso vira config persistida.
    private val monitoredPackages = setOf("com.rpstudio.gigu_poc")

    private var lastTexts: List<String> = emptyList()

    override fun onServiceConnected() {
        Bus.emit("serviço conectado")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in monitoredPackages) return

        val root = rootInActiveWindow ?: return
        val texts = mutableListOf<String>()
        try {
            collectTexts(root, texts, depth = 0)
        } finally {
            root.recycle()
        }
        // N1: conteúdo igual ao último scan, não há nada novo a reportar.
        if (texts == lastTexts) return
        lastTexts = texts

        Bus.emit("scan $pkg: ${texts.size} textos -> ${texts.joinToString(" | ")}")
    }

    private fun collectTexts(node: AccessibilityNodeInfo, out: MutableList<String>, depth: Int) {
        if (depth > MAX_DEPTH) return

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

    private companion object {
        const val MAX_DEPTH = 15
    }

    override fun onInterrupt() {
        Bus.emit("serviço interrompido")
    }
}
