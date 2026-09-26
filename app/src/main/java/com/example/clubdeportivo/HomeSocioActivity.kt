package com.example.clubdeportivo

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class HomeSocioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_socio)

        // Referencias a las vistas del layout
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)
        val btnVerCredencial = findViewById<AppCompatButton>(R.id.btnVerCredencial)

        // Botones de Accesos Rápidos
        val btnReservarClase = findViewById<LinearLayout>(R.id.btnReservarClase)
        val btnMisClases = findViewById<LinearLayout>(R.id.btnMisClases)
        val btnMiRutina = findViewById<LinearLayout>(R.id.btnMiRutina)
        val btnNutricion = findViewById<LinearLayout>(R.id.btnNutricion)

        // Tarjeta de Próxima Clase
        val btnProximaClase = findViewById<LinearLayout>(R.id.btnProximaClase)

        // Subtítulo de la tarjeta "Mis clases"
        val tvMisClasesCount = findViewById<TextView>(R.id.tvMisClasesCount)

        // Configuración de los eventos Click (puedes reemplazar los Toast por Intent hacia nuevas pantallas)
        btnVerCredencial.setOnClickListener {
            Toast.makeText(this, "Mostrando Credencial Digital...", Toast.LENGTH_SHORT).show()
        }

        btnReservarClase.setOnClickListener {
            startActivity(android.content.Intent(this, ReservationActivity::class.java))
        }

        btnMisClases.setOnClickListener {
            startActivity(android.content.Intent(this, MisClasesActivity::class.java))
        }

        btnMiRutina.setOnClickListener {
            Toast.makeText(this, "Abrir pantalla: Mi rutina", Toast.LENGTH_SHORT).show()
        }

        btnNutricion.setOnClickListener {
            Toast.makeText(this, "Abrir pantalla: Nutrición", Toast.LENGTH_SHORT).show()
        }

        btnProximaClase.setOnClickListener {
            Toast.makeText(this, "Ver detalles de la próxima clase", Toast.LENGTH_SHORT).show()
        }

        // El texto con la cantidad de reservas se refresca cada vez que volvemos al inicio.
        actualizarContadorReservas(tvMisClasesCount)
    }

    override fun onResume() {
        super.onResume()
        actualizarContadorReservas(findViewById<TextView>(R.id.tvMisClasesCount))
    }

    /** Escribe "0 reservadas", "1 reservada" o "N reservadas" según el store. */
    private fun actualizarContadorReservas(contador: TextView) {
        val cantidad = ReservationStore.getAll().size
        if (cantidad == 1) {
            contador.text = "1 reservada"
        } else {
            contador.text = "$cantidad reservadas"
        }
    }
}