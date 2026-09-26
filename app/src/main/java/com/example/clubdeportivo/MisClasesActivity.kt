package com.example.clubdeportivo

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MisClasesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_clases)

        val backButton = findViewById<TextView>(R.id.backButton)

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
        val resumen = findViewById<TextView>(R.id.tvResumen)

        val reservas = ReservationStore.getAll()

        classList.removeAllViews()

        if (reservas.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            classList.visibility = View.GONE
            resumen.text = "Sin reservas por ahora."
            return
        }

        emptyState.visibility = View.GONE
        classList.visibility = View.VISIBLE
        resumen.text = pluralReservas(reservas.size)

        // Una tarjeta vertical por cada clase reservada.
        for (reserva in reservas) {
            classList.addView(crearTarjeta(reserva))
        }
    }

    /** Construye la tarjeta de una clase: nombre arriba, día y hora abajo. */
    private fun crearTarjeta(reserva: BookedClass): View {
        val tarjeta = LinearLayout(this)
        tarjeta.orientation = LinearLayout.VERTICAL
        tarjeta.setBackgroundResource(R.drawable.bg_card)
        tarjeta.setPadding(28, 24, 28, 24)

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

        // LayoutParams норма: ocupa todo el ancho de la pantalla.
        return tarjeta.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 28 }
        }
    }
    private fun pluralReservas(cantidad: Int): String {
        if (cantidad == 1) {
            return "1 clase reservada."
        }
        return "$cantidad clases reservadas."
    }
}
