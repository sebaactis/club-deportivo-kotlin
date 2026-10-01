package com.example.clubdeportivo.ui.profesor

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.profesor.ProfesorStore

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlin.math.roundToInt

class ClasesProfesorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clases_profesor)
        findViewById<AppCompatButton>(R.id.btnVolverClasesProfesor).setOnClickListener {
            finish()
        }

        val lista = findViewById<LinearLayout>(R.id.listaClasesProfesor)

        for (clase in ProfesorStore.clases) {

            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setBackgroundResource(R.drawable.bg_card)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            }

            tarjeta.addView(TextView(this).apply {
                text = "${clase.horario} · ${clase.deporte}"
                textSize = 18f
                setTextColor(Color.WHITE)
            })

            tarjeta.addView(TextView(this).apply {
                text = "${clase.fecha}\n${clase.lugar} · ${clase.socios.size} socios inscriptos"
                textSize = 14f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(0, dp(8), 0, dp(12))
            })

            tarjeta.addView(AppCompatButton(this).apply {
                text = "Tomar asistencia · ${clase.horario}"
                isAllCaps = false
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.bg_badge_blue)
                minHeight = dp(48)
                setOnClickListener {
                    startActivity(Intent(this@ClasesProfesorActivity,
                        AsistenciaProfesorActivity::class.java)
                        .putExtra(AsistenciaProfesorActivity.EXTRA_CLASE_ID, clase.id))
                }
            })

            lista.addView(tarjeta)
        }
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).roundToInt()
}
