package com.example.clubdeportivo.ui.socio

import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.reservas.ReservationActivity

import com.example.clubdeportivo.data.reservas.BookedClass
import com.example.clubdeportivo.data.reservas.ReservationResult
import com.example.clubdeportivo.data.reservas.ReservationStore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MisClasesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_clases)

        val backButton = findViewById<TextView>(R.id.backButton)
        findViewById<AppCompatButton>(R.id.reserveClassButton).setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    // Se llama cada vez que la pantalla aparece. Lo recargamos en onResume y no solo en
    // onCreate para que, al volver desde "Reservar clase", se vea la reserva nueva.
    override fun onResume() {
        super.onResume()
        mostrarReservas()
    }
    private fun mostrarReservas() {
        val classList = findViewById<LinearLayout>(R.id.classList)
        val emptyState = findViewById<TextView>(R.id.emptyState)
        val reserveClassButton = findViewById<AppCompatButton>(R.id.reserveClassButton)
        val resumen = findViewById<TextView>(R.id.tvResumen)

        val reservas = ReservationStore.getAll()

        classList.removeAllViews()

        if (reservas.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            reserveClassButton.visibility = View.VISIBLE
            classList.visibility = View.GONE
            resumen.text = "Sin reservas por ahora."
            return
        }

        emptyState.visibility = View.GONE
        reserveClassButton.visibility = View.GONE
        classList.visibility = View.VISIBLE
        resumen.text = pluralReservas(reservas.size)

        // Una tarjeta vertical por cada clase reservada.
        for (reserva in reservas) {
            classList.addView(crearTarjeta(reserva))
        }
    }

    /** Construye la tarjeta de una clase con acciones sobre su identidad estable. */
    private fun crearTarjeta(reserva: BookedClass): View {
        val tarjeta = LinearLayout(this)
        tarjeta.orientation = LinearLayout.VERTICAL
        tarjeta.setBackgroundResource(R.drawable.bg_card)
        tarjeta.setPadding(dp(14), dp(12), dp(14), dp(12))

        val nombre = TextView(this)
        nombre.text = reserva.className
        nombre.textSize = 18f
        nombre.setTextColor(0xFFFFFFFF.toInt())
        nombre.setTypeface(null, android.graphics.Typeface.BOLD)
        tarjeta.addView(nombre)

        val diaYHora = TextView(this)
        diaYHora.text = "${reserva.day} · ${reserva.time}"
        diaYHora.textSize = 14f
        diaYHora.setTextColor(0xFF94A3B8.toInt())
        tarjeta.addView(diaYHora)

        val acciones = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        acciones.addView(botonAccion("Modificar") {
            startActivity(Intent(this, ReservationActivity::class.java).apply {
                putExtra(ReservationActivity.EXTRA_ID, reserva.id)
            })
        })
        acciones.addView(botonAccion("Cancelar") { confirmarCancelacion(reserva) })
        tarjeta.addView(acciones)

        // LayoutParams норма: ocupa todo el ancho de la pantalla.
        return tarjeta.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(14) }
        }
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun botonAccion(label: String, accion: () -> Unit): AppCompatButton =
        AppCompatButton(this).apply {
            text = label
            setAllCaps(false)
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundResource(R.drawable.bg_input_field)
            minWidth = 0
            minHeight = dp(48)
            setPadding(dp(8), 0, dp(8), 0)
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                setMargins(dp(4), dp(12), dp(4), 0)
            }
            setOnClickListener { accion() }
        }

    private fun confirmarCancelacion(reserva: BookedClass) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar reserva")
            .setMessage("¿Quieres cancelar ${reserva.className}, ${reserva.day} a las ${reserva.time}?")
            .setNegativeButton("Conservar", null)
            .setPositiveButton("Cancelar reserva") { _, _ ->
                val result = ReservationStore.delete(reserva.id)
                val mensaje = if (result == ReservationResult.SUCCESS) "Reserva cancelada." else result.message
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                mostrarReservas()
            }
            .show()
    }

    private fun pluralReservas(cantidad: Int): String {
        if (cantidad == 1) {
            return "1 clase reservada."
        }
        return "$cantidad clases reservadas."
    }
}
