package com.example.clubdeportivo.ui.reservas

import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.socio.HomeSocioActivity

import com.example.clubdeportivo.data.reservas.BookedClass
import com.example.clubdeportivo.data.reservas.ReservationCatalog
import com.example.clubdeportivo.data.reservas.ReservationResult
import com.example.clubdeportivo.data.reservas.ReservationStore

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ReservationConfirmationActivity : AppCompatActivity() {
    private var editing = false
    private var reservationId: String? = null
    private var className: String? = null
    private var day: String? = null
    private var time: String? = null
    private var saved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_confirmation)

        val state = savedInstanceState ?: intent.extras ?: Bundle()
        editing = state.containsKey(ReservationActivity.EXTRA_ID)
        reservationId = state.get(ReservationActivity.EXTRA_ID) as? String
        className = state.get(ReservationActivity.EXTRA_CLASS) as? String
        day = state.get(ReservationActivity.EXTRA_DAY) as? String
        time = state.get(ReservationActivity.EXTRA_TIME) as? String
        saved = savedInstanceState?.getBoolean(STATE_SAVED) ?: false

        findViewById<TextView>(R.id.confirmationTitle).text =
            if (editing) "Confirmar cambios" else "Confirmar reserva"
        val summary = findViewById<TextView>(R.id.reservationSummary)
        val confirmButton = findViewById<AppCompatButton>(R.id.confirmButton)
        confirmButton.text = if (editing) "Guardar cambios" else "Confirmar reserva"
        val initialError = validationError()
        summary.text = initialError?.message ?: "$className\n$day · $time"
        confirmButton.isEnabled = initialError == null && !saved

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<AppCompatButton>(R.id.modifyButton).setOnClickListener { finish() }
        confirmButton.setOnClickListener {
            if (saved) return@setOnClickListener
            val error = validationError()
            if (error != null) {
                summary.text = error.message
                Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                confirmButton.isEnabled = false
                return@setOnClickListener
            }
            val booking = BookedClass(className = className!!, day = day!!, time = time!!)
            // Solo la confirmación explícita modifica las reservas en memoria.
            val result = if (editing) {
                ReservationStore.update(reservationId!!, booking)
            } else {
                ReservationStore.add(booking)
            }
            if (result != ReservationResult.SUCCESS) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            saved = true
            confirmButton.isEnabled = false
            Toast.makeText(this, if (editing) "Reserva actualizada." else "¡Reserva confirmada!", Toast.LENGTH_LONG).show()
            val home = Intent(this, HomeSocioActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(home)
            finish()
        }
    }

    private fun validationError(): ReservationResult? {
        if (editing && reservationId?.let { ReservationStore.getById(it) } == null) {
            return ReservationResult.UNKNOWN_ID
        }
        if (!ReservationCatalog.isValid(className, day, time)) return ReservationResult.INVALID
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (editing) outState.putString(ReservationActivity.EXTRA_ID, reservationId)
        outState.putString(ReservationActivity.EXTRA_CLASS, className)
        outState.putString(ReservationActivity.EXTRA_DAY, day)
        outState.putString(ReservationActivity.EXTRA_TIME, time)
        outState.putBoolean(STATE_SAVED, saved)
    }

    companion object {
        private const val STATE_SAVED = "reservation_saved"
    }
}
