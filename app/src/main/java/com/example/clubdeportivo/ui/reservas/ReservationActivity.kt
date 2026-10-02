package com.example.clubdeportivo.ui.reservas

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.data.reservas.ReservationCatalog
import com.example.clubdeportivo.data.reservas.ReservationResult
import com.example.clubdeportivo.data.reservas.ReservationStore
import com.example.clubdeportivo.ui.acceso.DemoAccess
import com.example.clubdeportivo.ui.inicio.MainActivity

class ReservationActivity : AppCompatActivity() {
    private var editing = false
    private var reservationId: String? = null
    private var selectedDay: String? = null
    private var selectedClass: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!requireMember()) {
            return
        }
        setContentView(R.layout.activity_reservation)
        installInsets()

        val dayChoices = findViewById<LinearLayout>(R.id.dayChoices)
        val classChoices = findViewById<LinearLayout>(R.id.classChoices)
        val error = findViewById<TextView>(R.id.selectionError)
        val continueButton = findViewById<AppCompatButton>(R.id.continueButton)
        val backButton = findViewById<TextView>(R.id.backButton)

        val state = savedInstanceState ?: intent.extras ?: Bundle()
        editing = state.containsKey(EXTRA_ID)
        reservationId = state.get(EXTRA_ID) as? String
        val original = reservationId?.let { ReservationStore.getById(it) }
        if (savedInstanceState == null && editing && original != null) {
            selectedDay = original.day
            selectedClass = original.className
            selectedTime = original.time
        } else {
            selectedDay = (state.get(EXTRA_DAY) as? String)?.takeIf { it in ReservationCatalog.days }
            selectedClass = (state.get(EXTRA_CLASS) as? String)?.takeIf { it in ReservationCatalog.classes }
            selectedTime = (state.get(EXTRA_TIME) as? String)?.takeIf {
                ReservationCatalog.classes[selectedClass]?.contains(it) == true
            }
        }
        findViewById<TextView>(R.id.reservationTitle).text =
            getString(if (editing) R.string.reservas_edit_title else R.string.reservas_title)
        continueButton.setText(if (editing) R.string.reservas_review else R.string.reservas_continue)
        if (editing && original == null) {
            error.visibility = View.VISIBLE
            error.text = ReservationResult.UNKNOWN_ID.message
            continueButton.isEnabled = false
        }
        refreshChoices(dayChoices, classChoices)

        backButton.setOnClickListener {
            finish()
        }
        continueButton.setOnClickListener {
            if (!requireMember()) {
                return@setOnClickListener
            }
            if (editing && reservationId?.let { ReservationStore.getById(it) } == null) {
                error.visibility = View.VISIBLE
                error.text = ReservationResult.UNKNOWN_ID.message
            } else if (!ReservationCatalog.isValid(selectedClass, selectedDay, selectedTime)) {
                error.visibility = View.VISIBLE
                error.text = ReservationResult.INVALID.message
            } else {
                error.visibility = View.GONE
                startActivity(
                    Intent(this, ReservationConfirmationActivity::class.java).apply {
                        putExtra(EXTRA_CLASS, selectedClass)
                        putExtra(EXTRA_DAY, selectedDay)
                        putExtra(EXTRA_TIME, selectedTime)
                        if (editing) {
                            putExtra(EXTRA_ID, reservationId)
                        }
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (editing) {
            outState.putString(EXTRA_ID, reservationId)
        }
        outState.putString(EXTRA_CLASS, selectedClass)
        outState.putString(EXTRA_DAY, selectedDay)
        outState.putString(EXTRA_TIME, selectedTime)
    }

    override fun onResume() {
        super.onResume()
        if (requireMember()) {
            refreshChoices(findViewById(R.id.dayChoices), findViewById(R.id.classChoices))
        }
    }

    private fun requireMember(): Boolean {
        if (DemoAccess.currentProfile?.role == DemoAccess.Role.SOCIO) {
            return true
        }
        if (!isFinishing) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        return false
    }

    private fun installInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val root = findViewById<View>(R.id.reservasRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun refreshChoices(dayChoices: LinearLayout, classChoices: LinearLayout) {
        dayChoices.removeAllViews()
        for (day in ReservationCatalog.days) {
            val button = choiceButton(day, selectedDay == day) {
                selectedDay = day
                refreshChoices(dayChoices, classChoices)
            }
            dayChoices.addView(
                button,
                LinearLayout.LayoutParams(dp(48), -2).apply {
                    marginEnd = dp(6)
                }
            )
        }

        classChoices.removeAllViews()
        ReservationCatalog.classes.forEach { (className, times) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.access_card)
                setPadding(dp(14), dp(12), dp(14), dp(12))
            }
            val details = ReservationCatalog.demoDetails.getValue(className)
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            header.addView(
                TextView(this).apply {
                    text = details.initials
                    typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
                    setTextColor(0xFF071A33.toInt())
                    gravity = Gravity.CENTER
                    setBackgroundResource(
                        if (className == "Musculación") {
                            R.drawable.access_circle_member
                        } else {
                            R.drawable.reservas_avatar
                        }
                    )
                },
                LinearLayout.LayoutParams(dp(44), dp(44))
            )
            val labels = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            labels.addView(
                TextView(this).apply {
                    text = details.teacher
                    textSize = 16f
                    setTextColor(0xFFF2F6FC.toInt())
                    typeface = ResourcesCompat.getFont(context, R.font.inter_semibold)
                }
            )
            labels.addView(
                TextView(this).apply {
                    text = getString(R.string.reservas_class_room, className, details.room)
                    textSize = 12f
                    setTextColor(0xFF93A9C8.toInt())
                    typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
                }
            )
            header.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(header)
            val timesRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            times.forEach { time ->
                timesRow.addView(
                    choiceButton(time, selectedClass == className && selectedTime == time) {
                        selectedClass = className
                        selectedTime = time
                        refreshChoices(dayChoices, classChoices)
                    }
                )
            }
            card.addView(
                timesRow,
                LinearLayout.LayoutParams(-1, -2).apply {
                    topMargin = dp(12)
                }
            )
            classChoices.addView(
                card,
                LinearLayout.LayoutParams(-1, -2).apply {
                    bottomMargin = dp(12)
                }
            )
        }

        val valid = ReservationCatalog.isValid(selectedClass, selectedDay, selectedTime)
        val knownId = !editing || reservationId?.let { ReservationStore.getById(it) } != null
        findViewById<AppCompatButton>(R.id.continueButton).apply {
            isEnabled = valid && knownId
            alpha = if (isEnabled) 1f else 0.5f
        }
    }

    private fun choiceButton(label: String, selected: Boolean = false, onClick: () -> Unit): AppCompatButton {
        return AppCompatButton(this).apply {
            text = label
            setAllCaps(false)
            textSize = 12f
            typeface = ResourcesCompat.getFont(context, R.font.inter_medium)
            isSelected = selected
            setTextColor(if (selected) 0xFFFFC72C.toInt() else 0xFFF2F6FC.toInt())
            backgroundTintList = null
            supportBackgroundTintList = null
            setBackgroundResource(R.drawable.reservas_choice)
            gravity = Gravity.CENTER
            minWidth = dp(48)
            minimumWidth = dp(48)
            minHeight = dp(48)
            setPadding(dp(8), 0, dp(8), 0)
            setOnClickListener {
                onClick()
            }
            val margin = dp(2)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply {
                setMargins(margin, margin, margin, margin)
            }
        }
    }

    companion object {
        const val EXTRA_ID = "reservation_id"
        const val EXTRA_CLASS = "reservation_class"
        const val EXTRA_DAY = "reservation_day"
        const val EXTRA_TIME = "reservation_time"
    }
}
