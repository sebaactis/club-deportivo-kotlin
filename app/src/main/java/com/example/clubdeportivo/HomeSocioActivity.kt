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
            Toast.makeText(this, "Mostrando Credencial Digital...", Toast.LENGTH_SHORT).show()
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

    private fun actualizarContadorReservas(contador: TextView) {
        val cantidad = ReservationStore.getAll().size
        if (cantidad == 1) {
            contador.text = "1 reservada"
        } else {
            contador.text = "$cantidad reservadas"
        }
    }
}