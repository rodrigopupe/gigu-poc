package com.rpstudio.gigu_poc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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

    @SuppressLint("InflateParams")
    private fun buildView(offer: RideOffer, verdict: Verdict): View {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_overlay_card, null)
        val cardContainer = view.findViewById<View>(R.id.cardContainer)
        val tvVerdict = view.findViewById<TextView>(R.id.tvVerdict)
        val tvNetAmountInfo = view.findViewById<TextView>(R.id.tvNetAmountInfo)
        val tvGrossAmountInfo = view.findViewById<TextView>(R.id.tvGrossAmountInfo)

        val colorRes = if (verdict.worth)
            R.color.verdict_worth
        else
            R.color.verdict_not_worth

        val labelRes = if (verdict.worth)
            R.string.verdict_worth_label
        else
            R.string.verdict_not_worth_label

        cardContainer.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        tvVerdict.text = context.getString(labelRes)
        tvNetAmountInfo.text = context.getString(
            R.string.net_amount_info,
            verdict.netBrl.toBrl(),
            verdict.netPerKm.toBrl()
        )
        tvGrossAmountInfo.text = context.getString(
            R.string.gross_amount_info,
            offer.priceBrl.toBrl(),
            offer.totalKm.toKmText(),
            offer.totalMinutes
        )

        return view
    }

    private companion object {
        const val AUTO_HIDE_MS = 5000L
    }
}
