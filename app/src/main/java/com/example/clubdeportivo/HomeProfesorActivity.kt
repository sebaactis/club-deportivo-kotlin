package com.example.clubdeportivo

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class HomeProfesorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_profesor)

        // Referencias del layout
        val btnEscanearQr = findViewById<AppCompatButton>(R.id.btnEscanearQr)
        val btnTomarAsistencia = findViewById<LinearLayout>(R.id.btnTomarAsistencia)
        val btnMisClasesProfesor = findViewById<LinearLayout>(R.id.btnMisClasesProfesor)
        val btnProximaClaseProfesor = findViewById<LinearLayout>(R.id.btnProximaClaseProfesor)

        // Eventos Click
        btnEscanearQr.setOnClickListener {
            Toast.makeText(this, "Abriendo escáner QR de socios...", Toast.LENGTH_SHORT).show()
        }

        btnTomarAsistencia.setOnClickListener {
            Toast.makeText(this, "Abrir lista de asistencia de inscriptos", Toast.LENGTH_SHORT).show()
        }

        btnMisClasesProfesor.setOnClickListener {
            Toast.makeText(this, "Abrir agenda de clases del profesor", Toast.LENGTH_SHORT).show()
        }

        btnProximaClaseProfesor.setOnClickListener {
            Toast.makeText(this, "Ver detalles de la clase de Funcional", Toast.LENGTH_SHORT).show()
        }
    }
}