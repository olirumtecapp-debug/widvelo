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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.creativeam.widvelo.overspeed.OverspeedLevel
import kotlin.math.abs

/**
 * Janela flutuante minimalista (Widget Flutuante) do WidVelo.
 * Permanece visível sobre o Google Maps, Waze ou outros aplicativos com velocímetro,
 * limite de via e proximidade de radar em tempo real.
 */
class FloatingWidVeloWidget(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs: SharedPreferences = context.getSharedPreferences("widvelo_widget_prefs", Context.MODE_PRIVATE)

    private var rootView: LinearLayout? = null
    private var windowParams: WindowManager.LayoutParams? = null

    // Componentes de interface do widget
    private lateinit var tvSpeed: TextView
    private lateinit var tvUnit: TextView
    private lateinit var tvLimit: TextView
    private lateinit var limitBadgeLayout: LinearLayout
    private lateinit var tvRadar: TextView
    private lateinit var radarBadgeLayout: LinearLayout
    private lateinit var tvTrafficLight: TextView
    private lateinit var trafficLightBadgeLayout: LinearLayout
    private lateinit var cardContainer: LinearLayout

    private var isVisible = false

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

        // Listener de arrasto e toque
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
                        // Salva a posição preferida do motorista
                        prefs.edit().putInt("widget_pos_x", params.x).putInt("widget_pos_y", params.y).apply()
                    } else {
                        // Toque simples: abre o aplicativo principal
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
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Card Container com Borda e Cantos Arredondados Modernos
        cardContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = createCardBackground("#0A0B0E", "#0284C7", 16f, 3)
        }

        // 1. Coluna de Velocidade (Número Gigante + km/h)
        val speedCol = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.BOTTOM
        }
        tvSpeed = TextView(context).apply {
            text = "0"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        tvUnit = TextView(context).apply {
            text = " km/h"
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setPadding(0, 0, 0, dp(4))
        }
        speedCol.addView(tvSpeed)
        speedCol.addView(tvUnit)
        cardContainer.addView(speedCol)

        // Divisória vertical 1
        val sep1 = View(context).apply {
            background = createSolidBackground("#334155", 1f)
            layoutParams = LinearLayout.LayoutParams(dp(2), dp(24)).apply {
                marginStart = dp(10)
                marginEnd = dp(10)
            }
        }
        cardContainer.addView(sep1)

        // 2. Badge de Limite da Via (Círculo padrão regulamentar ou Pill)
        limitBadgeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(3), dp(8), dp(3))
            background = createCardBackground("#FFFFFF", "#EF4444", 8f, 2)
        }
        tvLimit = TextView(context).apply {
            text = "---"
            setTextColor(Color.parseColor("#0F172A"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        limitBadgeLayout.addView(tvLimit)
        cardContainer.addView(limitBadgeLayout)

        // 3. Badge de Radar Próximo
        radarBadgeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = createCardBackground("#1E293B", "#38BDF8", 8f, 1)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(8)
            }
        }
        tvRadar = TextView(context).apply {
            text = "RADAR 500m"
            setTextColor(Color.parseColor("#38BDF8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        radarBadgeLayout.addView(tvRadar)
        cardContainer.addView(radarBadgeLayout)

        // 4. Badge de Semáforo / Temporizador de Cruzamento
        trafficLightBadgeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = createCardBackground("#1E293B", "#10B981", 8f, 1)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(8)
            }
        }
        tvTrafficLight = TextView(context).apply {
            text = "FAROL"
            setTextColor(Color.parseColor("#10B981"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        trafficLightBadgeLayout.addView(tvTrafficLight)
        cardContainer.addView(trafficLightBadgeLayout)

        root.addView(cardContainer)
        return root
    }

    fun updateState(state: FloatingWidgetState) {
        if (!isVisible || rootView == null) return

        // 1. Velocidade
        tvSpeed.text = state.speedValue
        tvUnit.text = " ${state.speedUnit}"

        // 2. Cor da Borda conforme Excesso de Velocidade
        val borderColor = when (state.overspeedLevel) {
            OverspeedLevel.EXCEEDED -> "#EF4444" // Vermelho Alerta
            OverspeedLevel.CAUTION -> "#F59E0B"  // Âmbar
            OverspeedLevel.NORMAL -> "#10B981"   // Verde
            OverspeedLevel.NO_LIMIT -> "#0284C7" // Azul Elétrico
        }
        cardContainer.background = createCardBackground("#0A0B0E", borderColor, 16f, 3)

        // 3. Limite da Via
        if (!state.speedLimitText.isNullOrBlank()) {
            limitBadgeLayout.visibility = View.VISIBLE
            tvLimit.text = state.speedLimitText
        } else {
            limitBadgeLayout.visibility = View.GONE
        }

        // 4. Radar Próximo
        if (!state.radarDistanceText.isNullOrBlank()) {
            radarBadgeLayout.visibility = View.VISIBLE
            val radarType = state.radarTypeLabel ?: "RADAR"
            tvRadar.text = "⚠️ $radarType ${state.radarDistanceText}"

            if (state.isRadarImminent) {
                // Alerta Iminente (<= 100m): Realce em amarelo/vermelho
                radarBadgeLayout.background = createCardBackground("#451A03", "#F59E0B", 8f, 2)
                tvRadar.setTextColor(Color.parseColor("#FDE047"))
            } else {
                radarBadgeLayout.background = createCardBackground("#0F172A", "#38BDF8", 8f, 1)
                tvRadar.setTextColor(Color.parseColor("#38BDF8"))
            }
        } else {
            radarBadgeLayout.visibility = View.GONE
        }

        // 5. Semáforo Próximo & Temporizador
        if (!state.trafficLightNotice.isNullOrBlank()) {
            trafficLightBadgeLayout.visibility = View.VISIBLE
            tvTrafficLight.text = state.trafficLightNotice

            if (state.isAtRedLightStandstill) {
                // Parado no semáforo: destaque em vermelho/laranja com contagem regressiva
                trafficLightBadgeLayout.background = createCardBackground("#450A0A", "#EF4444", 8f, 2)
                tvTrafficLight.setTextColor(Color.parseColor("#FCA5A5"))
            } else {
                // Aproximação de semáforo: verde/ciano discreto
                trafficLightBadgeLayout.background = createCardBackground("#0F172A", "#10B981", 8f, 1)
                tvTrafficLight.setTextColor(Color.parseColor("#34D399"))
            }
        } else {
            trafficLightBadgeLayout.visibility = View.GONE
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

    private fun createCardBackground(bgColorHex: String, strokeColorHex: String, radiusDp: Float, strokeWidthDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(bgColorHex))
            cornerRadius = dp(radiusDp.toInt()).toFloat()
            setStroke(dp(strokeWidthDp), Color.parseColor(strokeColorHex))
        }
    }

    private fun createSolidBackground(bgColorHex: String, radiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(bgColorHex))
            cornerRadius = dp(radiusDp.toInt()).toFloat()
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
