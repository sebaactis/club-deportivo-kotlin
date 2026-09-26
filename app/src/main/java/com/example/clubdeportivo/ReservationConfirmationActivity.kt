package com.example.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ReservationConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation_confirmation)

        val className = intent.getStringExtra(ReservationActivity.EXTRA_CLASS).orEmpty()
        val day = intent.getStringExtra(ReservationActivity.EXTRA_DAY).orEmpty()
        val time = intent.getStringExtra(ReservationActivity.EXTRA_TIME).orEmpty()
        findViewById<TextView>(R.id.reservationSummary).text = "$className\n$day · $time"

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        findViewById<AppCompatButton>(R.id.modifyButton).setOnClickListener { finish() }
        findViewById<AppCompatButton>(R.id.confirmButton).setOnClickListener {
            // Save the booking first, so it is not lost if the app closes.
            ReservationStore.add(BookedClass(className = className, day = day, time = time))
            Toast.makeText(this, "¡Reserva confirmada!", Toast.LENGTH_LONG).show()
            val home = Intent(this, HomeSocioActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(home)
            finish()
        }
    }
}
