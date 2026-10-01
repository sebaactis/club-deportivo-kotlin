package com.example.clubdeportivo.ui.profesor

import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.recepcion.ReceptionActivity

import com.example.clubdeportivo.data.profesor.ProfesorStore

import android.content.Intent
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


        val btnEscanearQr = findViewById<AppCompatButton>(R.id.btnEscanearQr)
        val btnTomarAsistencia = findViewById<LinearLayout>(R.id.btnTomarAsistencia)
        val btnMisClasesProfesor = findViewById<LinearLayout>(R.id.btnMisClasesProfesor)
        val btnProximaClaseProfesor = findViewById<LinearLayout>(R.id.btnProximaClaseProfesor)

        findViewById<AppCompatButton>(R.id.btnReceptionProfesor).setOnClickListener {
            startActivity(Intent(this, ReceptionActivity::class.java))
        }
        findViewById<AppCompatButton>(R.id.btnSelectorClasesProfesor).setOnClickListener {
            startActivity(Intent(this, ClasesProfesorActivity::class.java))
        }

        btnEscanearQr.setOnClickListener {
            Toast.makeText(this, "Abriendo escáner QR de socios...", Toast.LENGTH_SHORT).show()
        }

        btnTomarAsistencia.setOnClickListener {
            startActivity(Intent(this, ClasesProfesorActivity::class.java))
        }

        btnMisClasesProfesor.setOnClickListener {
            startActivity(Intent(this, ClasesProfesorActivity::class.java))
        }

        btnProximaClaseProfesor.setOnClickListener {
            startActivity(Intent(this, AsistenciaProfesorActivity::class.java)
                .putExtra(AsistenciaProfesorActivity.EXTRA_CLASE_ID, ProfesorStore.CLASE_PROXIMA_ID))
        }
    }
}