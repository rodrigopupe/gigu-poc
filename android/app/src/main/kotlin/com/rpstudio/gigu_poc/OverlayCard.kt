package com.rpstudio.gigu_poc

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import androidx.core.graphics.toColorInt

/** "R$ 7,70" a partir de um BigDecimal (2 casas, vírgula decimal). */
fun BigDecimal.toBrl(): String = "R$ " + formatPtBr("#,##0.00")

/** "7,2" / "75,0": uma casa decimal, vírgula decimal e ponto de milhar. */
fun BigDecimal.toKmText(): String = formatPtBr("#,##0.0")

private fun BigDecimal.formatPtBr(pattern: String): String =
    DecimalFormat(pattern, DecimalFormatSymbols(Locale("pt", "BR")))
        .apply { roundingMode = RoundingMode.HALF_UP }
        .format(this)

/**
 * Card desenhado por cima do app alvo. O `context` DEVE ser o do AccessibilityService:
 * é ele que dá permissão para TYPE_ACCESSIBILITY_OVERLAY (um context de Application não serve).
 */
class OverlayCard(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private val autoHide = Runnable { hide() }
    private var cardView: View? = null

    fun show(offer: RideOffer, verdict: Verdict) {
        hide()
        val view = buildView(offer, verdict)
        windowManager.addView(view, buildLayoutParams())
        cardView = view
        handler.postDelayed(autoHide, AUTO_HIDE_MS)
    }

    fun hide() {
        handler.removeCallbacks(autoHide)
        // removeView lança se a janela já foi destruída (ex.: serviço desconectado).
        cardView?.let { runCatching { windowManager.removeView(it) } }
        cardView = null
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP
        return params
    }

    private fun buildView(offer: RideOffer, verdict: Verdict): View {
        val verdictColor = (if (verdict.worth) "#2E7D32" else "#C62828").toColorInt()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = GradientDrawable().apply {
                setColor(verdictColor)
                cornerRadius = dp(16).toFloat()
            }
            addView(
                text(if (verdict.worth) "VALE A PENA" else "NÃO COMPENSA", 22f, bold = true)
            )
            addView(
                text("Sobra ${verdict.netBrl.toBrl()}  ·  ${verdict.netPerKm.toBrl()}/km", 16f)
            )
            addView(
                text(
                    "Bruto ${offer.priceBrl.toBrl()}  ·  ${offer.totalKm.toKmText()} km  ·  ${offer.totalMinutes} min",
                    14f
                )
            )
        }

        // Margem em volta do card.
        return FrameLayout(context).apply {
            setPadding(dp(12), dp(12), dp(12), 0)
            addView(card)
        }
    }

    private fun text(value: String, sizeSp: Float, bold: Boolean = false) = TextView(context).apply {
        text = value
        textSize = sizeSp
        setTextColor(Color.WHITE)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun dp(value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    private companion object {
        const val AUTO_HIDE_MS = 5000L
    }
}
