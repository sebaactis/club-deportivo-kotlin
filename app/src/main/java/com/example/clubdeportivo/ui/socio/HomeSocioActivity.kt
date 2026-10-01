package com.example.clubdeportivo.ui.socio

import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.nutricion.NutricionActivity
import com.example.clubdeportivo.ui.reservas.ReservationActivity

import com.example.clubdeportivo.data.reservas.ReservationStore

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class HomeSocioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_socio)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)
        val btnVerCredencial = findViewById<AppCompatButton>(R.id.btnVerCredencial)

        val btnReservarClase = findViewById<LinearLayout>(R.id.btnReservarClase)
        val btnMisClases = findViewById<LinearLayout>(R.id.btnMisClases)
        val btnMiRutina = findViewById<LinearLayout>(R.id.btnMiRutina)
        val btnNutricion = findViewById<LinearLayout>(R.id.btnNutricion)

        val btnProximaClase = findViewById<LinearLayout>(R.id.btnProximaClase)

        val tvMisClasesCount = findViewById<TextView>(R.id.tvMisClasesCount)

        btnVerCredencial.setOnClickListener {
            startActivity(android.content.Intent(this, CredencialActivity::class.java))
        }

        btnReservarClase.setOnClickListener {
            startActivity(android.content.Intent(this, ReservationActivity::class.java))
        }

        btnMisClases.setOnClickListener {
            startActivity(android.content.Intent(this, MisClasesActivity::class.java))
        }

        btnMiRutina.setOnClickListener {
            startActivity(android.content.Intent(this, MiRutinaActivity::class.java))
        }

        btnNutricion.setOnClickListener {
            startActivity(android.content.Intent(this, NutricionActivity::class.java))
        }

        btnProximaClase.setOnClickListener {
            startActivity(android.content.Intent(this, MisClasesActivity::class.java))
        }

        // El texto con la cantidad de reservas se refresca cada vez que volvemos al inicio.
        actualizarContadorReservas(tvMisClasesCount)
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorReservas(findViewById<TextView>(R.id.tvMisClasesCount))
        actualizarTarjetaReserva()
    }

    private fun actualizarTarjetaReserva() {
        val reserva = ReservationStore.getAll().firstOrNull()
        findViewById<TextView>(R.id.tvReservaNombre).text = reserva?.className ?: "Sin reservas"
        findViewById<TextView>(R.id.tvReservaHorario).text =
            reserva?.let { "${it.day} · ${it.time}" } ?: "Reserva una clase desde el acceso rápido."
        findViewById<TextView>(R.id.tvReservaDetalle).text = if (reserva == null) {
            "Toca para ver Mis clases."
        } else {
            "Primera reserva guardada de ejemplo, no por fecha. Toca para ver Mis clases."
        }
    }

    private fun actualizarContadorReservas(contador: TextView) {
        val cantidad = ReservationStore.getAll().size
        if (cantidad == 1) {
            contador.text = "1 reservada"
        } else {
            contador.text = "$cantidad reservadas"
        }
    }
}