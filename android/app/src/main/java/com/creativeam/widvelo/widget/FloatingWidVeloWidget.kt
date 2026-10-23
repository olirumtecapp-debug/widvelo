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
 * 1. Em repouso/velocidade normal: Velocímetro redondo escuro elegante (Waze Dark), sem distrações.
 * 2. Em excesso de velocidade: O velocímetro fica VERMELHO e a placa R-19 (limite) surge ao lado.
 * 3. Botão de fechar (✕): mini-badge circular discreta no canto superior direito do velocímetro (sem quebras de caracteres).
 * 4. Pílula inferior inteligente: Alertas de radares, semáforos/cruzamentos e obras viárias.
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
        // Container vertical raiz
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        // Linha Principal Horizontal (Placa Limite + Velocímetro Redondo com Badge de Fechar)
        val mainRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // 1. Placa R-19 Circular de Limite da Via (Waze Style)
        // Oculta por padrão em velocidade normal; só surge quando ultrapassa a velocidade
        limitCircle = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = createCircleDrawable("#FFFFFF", "#DC2626", 3)
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                marginEnd = dp(6)
            }
            visibility = View.GONE
        }
        tvLimit = TextView(context).apply {
            text = "50"
            setTextColor(Color.parseColor("#DC2626"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        limitCircle.addView(tvLimit)
        mainRow.addView(limitCircle)

        // 2. Container FrameLayout do Velocímetro + Botão Fechar no Canto Superior Direito
        val speedBadgeContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(82), dp(82))
        }

        // Velocímetro Redondo Central (Waze Dark Style por padrão)
        speedCircle = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = createCircleDrawable("#1F242D", "#374151", 2)
            layoutParams = FrameLayout.LayoutParams(dp(74), dp(74), Gravity.CENTER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = dp(6).toFloat()
            }
        }

        tvSpeed = TextView(context).apply {
            text = "0"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        tvUnit = TextView(context).apply {
            text = "km/h"
            setTextColor(Color.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        speedCircle.addView(tvSpeed)
        speedCircle.addView(tvUnit)
        speedBadgeContainer.addView(speedCircle)

        // 3. Botão Circular Discreto de Fechar Widget (posicionado no topo-direito do velocímetro)
        btnClose = TextView(context).apply {
            text = "\u2715" // Unicode explícito para o X de fechar (evita qualquer corrupção de charset)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = createCircleDrawable("#1E293B", "#475569", 1)
            layoutParams = FrameLayout.LayoutParams(dp(22), dp(22), Gravity.TOP or Gravity.END)
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.alpha = 0.4f
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
        speedBadgeContainer.addView(btnClose)

        mainRow.addView(speedBadgeContainer)
        root.addView(mainRow)

        // 4. Mini-Cápsula Inferior Inteligente para Alertas (Radar, Semáforo ou Obras)
        radarPillLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(4), dp(10), dp(4))
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
            text = "\u26A0\uFE0F RADAR 400m"
            setTextColor(Color.parseColor("#38BDF8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
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

        // 2. Comportamento Waze do Velocímetro Central:
        // Velocidade normal: Fundo escuro discreto (#1F242D), borda suave, dígitos brancos e unidade cinza.
        // Excesso de velocidade: Fundo VERMELHO vivo (#EF4444) com borda #DC2626, alertando visualmente!
        if (isOverspeed) {
            speedCircle.background = createCircleDrawable("#EF4444", "#DC2626", 3)
            tvSpeed.setTextColor(Color.WHITE)
            tvUnit.setTextColor(Color.parseColor("#FEE2E2"))
        } else {
            speedCircle.background = createCircleDrawable("#1F242D", "#374151", 2)
            tvSpeed.setTextColor(Color.WHITE)
            tvUnit.setTextColor(Color.parseColor("#94A3B8"))
        }

        // 3. Comportamento da Placa R-19 de Limite da Via (Waze Style):
        // Na velocidade normal: OCULTA (nada acontece).
        // Em excesso de velocidade: SURGE ao lado em destaque com borda vermelha e limite regulamentar!
        val limitText = state.speedLimitText
        if (isOverspeed && !limitText.isNullOrBlank()) {
            limitCircle.visibility = View.VISIBLE
            tvLimit.text = limitText
            limitCircle.background = createCircleDrawable("#FFFFFF", "#DC2626", 3)
            tvLimit.setTextColor(Color.parseColor("#DC2626"))
        } else {
            limitCircle.visibility = View.GONE
        }

        // 4. Alertas Inteligentes na Mini-Cápsula Inferior
        if (state.isIncidentAhead && !state.incidentNotice.isNullOrBlank()) {
            // Alerta de Via Interditada ou Obras
            radarPillLayout.visibility = View.VISIBLE
            tvRadarPill.text = state.incidentNotice
            radarPillLayout.background = createPillBackground("#451A03", "#F59E0B", 12f, 1)
            tvRadarPill.setTextColor(Color.parseColor("#FCD34D"))
        } else if (!state.radarDistanceText.isNullOrBlank()) {
            // Alerta de Radar Homologado à Frente
            radarPillLayout.visibility = View.VISIBLE
            val radarType = state.radarTypeLabel ?: "RADAR"
            tvRadarPill.text = "\uD83D\uDCF7 $radarType ${state.radarDistanceText}"

            if (state.isRadarImminent) {
                radarPillLayout.background = createPillBackground("#450A0A", "#EF4444", 12f, 2)
                tvRadarPill.setTextColor(Color.parseColor("#FCA5A5"))
            } else {
                radarPillLayout.background = createPillBackground("#0F172A", "#38BDF8", 12f, 1)
                tvRadarPill.setTextColor(Color.parseColor("#38BDF8"))
            }
        } else if (!state.trafficLightNotice.isNullOrBlank()) {
            // Alerta de Semáforo / Cruzamento com Temporizador
            radarPillLayout.visibility = View.VISIBLE
            tvRadarPill.text = state.trafficLightNotice

            if (state.isAtRedLightStandstill) {
                radarPillLayout.background = createPillBackground("#450A0A", "#EF4444", 12f, 2)
                tvRadarPill.setTextColor(Color.parseColor("#FCA5A5"))
            } else {
                radarPillLayout.background = createPillBackground("#064E3B", "#10B981", 12f, 1)
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
