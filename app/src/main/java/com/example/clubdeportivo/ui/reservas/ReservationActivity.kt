package com.example.clubdeportivo.ui.reservas

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.reservas.ReservationCatalog
import com.example.clubdeportivo.data.reservas.ReservationResult
import com.example.clubdeportivo.data.reservas.ReservationStore

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ReservationActivity : AppCompatActivity() {
    private var editing = false
    private var reservationId: String? = null
    private var selectedDay: String? = null
    private var selectedClass: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

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
            if (editing) "Modificar reserva" else "Reservar clase"
        continueButton.text = if (editing) "Revisar cambios" else "Continuar"
        if (editing && original == null) {
            error.visibility = View.VISIBLE
            error.text = ReservationResult.UNKNOWN_ID.message
            continueButton.isEnabled = false
        }
        refreshChoices(dayChoices, classChoices)

        backButton.setOnClickListener { finish() }
        continueButton.setOnClickListener {
            if (editing && reservationId?.let { ReservationStore.getById(it) } == null) {
                error.visibility = View.VISIBLE
                error.text = ReservationResult.UNKNOWN_ID.message
            } else if (!ReservationCatalog.isValid(selectedClass, selectedDay, selectedTime)) {
                error.visibility = View.VISIBLE
                error.text = ReservationResult.INVALID.message
            } else {
                error.visibility = View.GONE
                startActivity(Intent(this, ReservationConfirmationActivity::class.java).apply {
                    putExtra(EXTRA_CLASS, selectedClass)
                    putExtra(EXTRA_DAY, selectedDay)
                    putExtra(EXTRA_TIME, selectedTime)
                    if (editing) putExtra(EXTRA_ID, reservationId)
                })
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (editing) outState.putString(EXTRA_ID, reservationId)
        outState.putString(EXTRA_CLASS, selectedClass)
        outState.putString(EXTRA_DAY, selectedDay)
        outState.putString(EXTRA_TIME, selectedTime)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun refreshChoices(dayChoices: LinearLayout, classChoices: LinearLayout) {
        dayChoices.removeAllViews()
        ReservationCatalog.days.forEach { day -> dayChoices.addView(choiceButton(day, selectedDay == day) {
            selectedDay = day
            refreshChoices(dayChoices, classChoices)
        }) }
        classChoices.removeAllViews()
        ReservationCatalog.classes.forEach { (className, times) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(dp(14), dp(12), dp(14), dp(12))
            }
            card.addView(TextView(this).apply {
                text = className
                textSize = 17f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            val timesRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            times.forEach { time ->
                timesRow.addView(choiceButton(time, selectedClass == className && selectedTime == time) {
                    selectedClass = className
                    selectedTime = time
                    refreshChoices(dayChoices, classChoices)
                })
            }
            card.addView(timesRow)
            classChoices.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        }
    }

    private fun choiceButton(label: String, selected: Boolean = false, onClick: () -> Unit): AppCompatButton {
        return AppCompatButton(this).apply {
            text = label
            setAllCaps(false)
            textSize = 12f
            setTextColor(if (selected) 0xFF071326.toInt() else 0xFFFFFFFF.toInt())
            setBackgroundColor(if (selected) 0xFFFFC928.toInt() else 0xFF102A49.toInt())
            gravity = Gravity.CENTER
            minWidth = 0
            minHeight = dp(48)
            setPadding(dp(8), 0, dp(8), 0)
            setOnClickListener { onClick() }
            val margin = dp(2)
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
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
