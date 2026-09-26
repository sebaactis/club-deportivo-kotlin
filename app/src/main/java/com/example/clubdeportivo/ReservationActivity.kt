package com.example.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ReservationActivity : AppCompatActivity() {
    private val weekdays = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    private val options = listOf(
        "Funcional" to listOf("09:00", "18:00", "19:30"),
        "Musculación" to listOf("08:00", "12:00", "20:00"),
        "Spinning" to listOf("07:00", "17:00", "21:00"),
    )
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

        refreshChoices(dayChoices, classChoices)

        backButton.setOnClickListener { finish() }
        continueButton.setOnClickListener {
            val selection = ReservationSelection(selectedClass, selectedDay, selectedTime)
            if (selection.summary() == null) {
                error.visibility = View.VISIBLE
                error.text = "Seleccioná un día, una clase y un horario para continuar."
            } else {
                error.visibility = View.GONE
                startActivity(Intent(this, ReservationConfirmationActivity::class.java).apply {
                    putExtra(EXTRA_CLASS, selection.className)
                    putExtra(EXTRA_DAY, selection.date)
                    putExtra(EXTRA_TIME, selection.time)
                })
            }
        }
    }

    private fun refreshChoices(dayChoices: LinearLayout, classChoices: LinearLayout) {
        dayChoices.removeAllViews()
        weekdays.forEach { day -> dayChoices.addView(choiceButton(day, selectedDay == day) {
            selectedDay = day
            refreshChoices(dayChoices, classChoices)
        }) }
        classChoices.removeAllViews()
        options.forEach { (className, times) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(14, 12, 14, 12)
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
            classChoices.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 })
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
            minHeight = 40
            setPadding(10, 0, 10, 0)
            setOnClickListener { onClick() }
            val margin = (resources.displayMetrics.density * 6).toInt()
            layoutParams = LinearLayout.LayoutParams(-2, 40).apply { setMargins(margin, margin, margin, margin) }
        }
    }

    companion object {
        const val EXTRA_CLASS = "reservation_class"
        const val EXTRA_DAY = "reservation_day"
        const val EXTRA_TIME = "reservation_time"
    }
}
