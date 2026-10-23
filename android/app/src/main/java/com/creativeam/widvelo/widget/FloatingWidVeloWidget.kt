package com.creativeam.widvelo.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.creativeam.widvelo.overspeed.OverspeedLevel
import kotlin.math.abs

/**
 * Widget Flutuante Estilo Waze do WidVelo.
 * Design circular, clean e dinâmico:
 * 1. Velocímetro redondo branco com tipografia preta sólida.
 * 2. Em excesso de velocidade: velocímetro fica VERMELHO com dígitos brancos.
 * 3. Placa R-19 circular de limite da via: em excesso de velocidade, fica MAIORZINHA e vermelha como no Waze.
 * 4. Botão discreto de fechar (✕) para encerrar o widget com 1 toque.
 * 5. Mini-cápsula inferior para alerta de radar e semáforos.
 */
class FloatingWidVeloWidget(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs: SharedPreferences = context.getSharedPreferences("widvelo_widget_prefs", Context.MODE_PRIVATE)

    private var rootView: LinearLayout? = null
    private var windowParams: WindowManager.LayoutParams? = null

    // Componentes de interface do widget
    private lateinit var speedCircle: LinearLayout
    private lateinit var tvSpeed: TextView
    private lateinit var tvUnit: TextView

    private lateinit var limitCircle: LinearLayout
    private lateinit var tvLimit: TextView

    private lateinit var radarPillLayout: LinearLayout
    private lateinit var tvRadarPill: TextView

    private lateinit var btnClose: TextView

    private var isVisible = false
    private var currentOverspeedLevel = OverspeedLevel.NORMAL

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (isVisible) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("widget_pos_x", 40)
            y = prefs.getInt("widget_pos_y", 220)
        }
        windowParams = params

        val view = buildWidgetView()
        rootView = view

        // Listener de arrasto e reposicionamento
        var initialX = 0
        var initialY = 0
        var touchStartX = 0f
        var touchStartY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - touchStartX).toInt()
                    val deltaY = (event.rawY - touchStartY).toInt()
                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isDragging = true
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY
                        try {
                            windowManager.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        prefs.edit().putInt("widget_pos_x", params.x).putInt("widget_pos_y", params.y).apply()
                    } else {
                        openMainActivity()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(view, params)
            isVisible = true
        } catch (_: Exception) {}
    }

    private fun buildWidgetView(): LinearLayout {
        // Container vertical raiz (comporta a linha dos círculos e a pílula de radar abaixo)
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        // Linha Principal Horizontal (Placa Limite + Velocímetro Redondo + Botão Fechar)
        val mainRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // 1. Placa R-19 Circular de Limite da Via (Waze Style)
        limitCircle = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = createCircleDrawable("#FFFFFF", "#EF4444", 3)
            layoutParams = LinearLayout.LayoutParams(dp(38), dp(38)).apply {
                marginEnd = dp(6)
            }
        }
        tvLimit = TextView(context).apply {
            text = "50"
            setTextColor(Color.parseColor("#0F172A"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        limitCircle.addView(tvLimit)
        mainRow.addView(limitCircle)

        // 2. Velocímetro Redondo Central (Waze Style)
        speedCircle = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = createCircleDrawable("#FFFFFF", "#CBD5E1", 2)
            layoutParams = LinearLayout.LayoutParams(dp(76), dp(76))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = dp(6).toFloat()
            }
        }

        tvSpeed = TextView(context).apply {
            text = "0"
            setTextColor(Color.parseColor("#0F172A"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        tvUnit = TextView(context).apply {
            text = "km/h"
            setTextColor(Color.parseColor("#64748B"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        speedCircle.addView(tvSpeed)
        speedCircle.addView(tvUnit)
        mainRow.addView(speedCircle)

        // 3. Botão Circular de Fechar Widget (✕)
        btnClose = TextView(context).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = createCircleDrawable("#334155", "#475569", 1)
            layoutParams = LinearLayout.LayoutParams(dp(22), dp(22)).apply {
                marginStart = dp(6)
            }
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.alpha = 0.5f
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        v.alpha = 1.0f
                        FloatingWidVeloService.stop(context)
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        v.alpha = 1.0f
                        true
                    }
                    else -> false
                }
            }
        }
        mainRow.addView(btnClose)
        root.addView(mainRow)

        // 4. Mini-Cápsula Inferior de Radar / Semáforo
        radarPillLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(3), dp(10), dp(3))
            background = createPillBackground("#0F172A", "#38BDF8", 12f, 1)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
            visibility = View.GONE
        }
        tvRadarPill = TextView(context).apply {
            text = "⚠️ RADAR 400m"
            setTextColor(Color.parseColor("#38BDF8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        radarPillLayout.addView(tvRadarPill)
        root.addView(radarPillLayout)

        return root
    }

    fun updateState(state: FloatingWidgetState) {
        if (!isVisible || rootView == null) return

        // 1. Atualiza valor de velocidade
        tvSpeed.text = state.speedValue
        tvUnit.text = state.speedUnit

        val isOverspeed = state.overspeedLevel == OverspeedLevel.EXCEEDED ||
                state.overspeedLevel == OverspeedLevel.CAUTION

        // 2. Estilo do Velocímetro Central:
        // Normal: Círculo Branco com dígitos escuros.
        // Excesso (Waze): Círculo VERMELHO vivo com dígitos brancos.
        if (isOverspeed) {
            speedCircle.background = createCircleDrawable("#EF4444", "#DC2626", 3)
            tvSpeed.setTextColor(Color.WHITE)
            tvUnit.setTextColor(Color.parseColor("#FEE2E2"))
        } else {
            speedCircle.background = createCircleDrawable("#FFFFFF", "#CBD5E1", 2)
            tvSpeed.setTextColor(Color.parseColor("#0F172A"))
            tvUnit.setTextColor(Color.parseColor("#64748B"))
        }

        // 3. Estilo da Placa R-19 de Limite da Via (Waze Style):
        // Quando está acima da velocidade, a placa FICA MAIORZINHA e com borda vermelha destacada!
        val limitText = state.speedLimitText
        if (!limitText.isNullOrBlank()) {
            limitCircle.visibility = View.VISIBLE
            tvLimit.text = limitText

            val layoutParams = limitCircle.layoutParams as LinearLayout.LayoutParams
            if (isOverspeed) {
                // Placa expandida (maiorzinha)
                layoutParams.width = dp(48)
                layoutParams.height = dp(48)
                limitCircle.layoutParams = layoutParams
                limitCircle.background = createCircleDrawable("#FFFFFF", "#DC2626", 4)
                tvLimit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvLimit.setTextColor(Color.parseColor("#DC2626"))
            } else {
                // Placa tamanho normal compacto
                layoutParams.width = dp(38)
                layoutParams.height = dp(38)
                limitCircle.layoutParams = layoutParams
                limitCircle.background = createCircleDrawable("#FFFFFF", "#EF4444", 3)
                tvLimit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                tvLimit.setTextColor(Color.parseColor("#0F172A"))
            }
        } else {
            limitCircle.visibility = View.GONE
        }

        // 4. Alerta de Radar ou Semáforo na Mini-Cápsula
        if (!state.radarDistanceText.isNullOrBlank()) {
            radarPillLayout.visibility = View.VISIBLE
            val radarType = state.radarTypeLabel ?: "RADAR"
            tvRadarPill.text = "⚠️ $radarType ${state.radarDistanceText}"

            if (state.isRadarImminent) {
                radarPillLayout.background = createPillBackground("#450A0A", "#EF4444", 12f, 2)
                tvRadarPill.setTextColor(Color.parseColor("#FCA5A5"))
            } else {
                radarPillLayout.background = createPillBackground("#0F172A", "#38BDF8", 12f, 1)
                tvRadarPill.setTextColor(Color.parseColor("#38BDF8"))
            }
        } else if (!state.trafficLightNotice.isNullOrBlank()) {
            radarPillLayout.visibility = View.VISIBLE
            tvRadarPill.text = state.trafficLightNotice

            if (state.isAtRedLightStandstill) {
                radarPillLayout.background = createPillBackground("#450A0A", "#EF4444", 12f, 2)
                tvRadarPill.setTextColor(Color.parseColor("#FCA5A5"))
            } else {
                radarPillLayout.background = createPillBackground("#0F172A", "#10B981", 12f, 1)
                tvRadarPill.setTextColor(Color.parseColor("#34D399"))
            }
        } else {
            radarPillLayout.visibility = View.GONE
        }
    }

    fun hide() {
        if (!isVisible || rootView == null) return
        try {
            windowManager.removeView(rootView)
        } catch (_: Exception) {}
        isVisible = false
        rootView = null
    }

    private fun openMainActivity() {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            if (intent != null) {
                context.startActivity(intent)
            }
        } catch (_: Exception) {}
    }

    private fun createCircleDrawable(bgColorHex: String, strokeColorHex: String, strokeWidthDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(bgColorHex))
            if (strokeWidthDp > 0) {
                setStroke(dp(strokeWidthDp), Color.parseColor(strokeColorHex))
            }
        }
    }

    private fun createPillBackground(bgColorHex: String, strokeColorHex: String, radiusDp: Float, strokeWidthDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(bgColorHex))
            cornerRadius = dp(radiusDp.toInt()).toFloat()
            setStroke(dp(strokeWidthDp), Color.parseColor(strokeColorHex))
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
