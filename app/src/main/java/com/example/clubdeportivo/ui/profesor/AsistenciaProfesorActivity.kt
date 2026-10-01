package com.example.clubdeportivo.ui.profesor

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.profesor.ProfesorStore

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import kotlin.math.roundToInt

class AsistenciaProfesorActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CLASE_ID = "clase_id"
    }

    private lateinit var titulo: TextView
    private lateinit var detalle: TextView
    private lateinit var contador: TextView
    private lateinit var lista: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencia_profesor)
        titulo = findViewById(R.id.tvTituloClaseProfesor)
        detalle = findViewById(R.id.tvDetalleClaseProfesor)
        contador = findViewById(R.id.tvContadorAsistenciaProfesor)
        lista = findViewById(R.id.listaSociosProfesor)

        findViewById<AppCompatButton>(R.id.btnVolverAsistenciaProfesor).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mostrarClase()
    }

    private fun mostrarClase() {
        lista.removeAllViews()

        val clase = ProfesorStore.buscarClase(intent.getStringExtra(EXTRA_CLASE_ID))
        if (clase == null) {
            titulo.text = "Clase no disponible"
            detalle.text = "No se encontró la clase seleccionada. Vuelve a Mis clases."
            contador.text = "Sin clase seleccionada"
            return
        }

        titulo.text = clase.deporte
        detalle.text = "${clase.fecha} · ${clase.horario}\n${clase.lugar}"
        actualizarContador(clase)

        if (clase.socios.isEmpty()) {
            lista.addView(TextView(this).apply {
                text = "No hay socios inscriptos en esta clase de ejemplo."
                textSize = 16f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setBackgroundResource(R.drawable.bg_card)
            })

            return
        }

        val tintes = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.parseColor("#60A5FA"), Color.parseColor("#94A3B8"))
        )

        for (socio in clase.socios) {
            val casilla = AppCompatCheckBox(this).apply {
                isChecked = ProfesorStore.estaPresente(clase.id, socio.id)
                text = etiqueta(socio, isChecked)
                textSize = 16f
                setTextColor(Color.WHITE)
                buttonTintList = tintes
                minHeight = dp(64)
                setPadding(dp(12), dp(8), dp(12), dp(8))
                setBackgroundResource(R.drawable.bg_card)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }

            casilla.setOnCheckedChangeListener { _, marcada ->
                if (ProfesorStore.marcarPresente(clase.id, socio.id, marcada)) {
                    casilla.text = etiqueta(socio, marcada)
                    actualizarContador(clase)
                }
            }

            lista.addView(casilla)
        }
    }

    private fun etiqueta(socio: ProfesorStore.Socio, presente: Boolean): String =
        "${socio.nombre}\n${if (presente) "Presente" else "Sin marcar"}"

    private fun actualizarContador(clase: ProfesorStore.Clase) {
        contador.text = "${ProfesorStore.contarPresentes(clase.id)} de ${clase.socios.size} presentes"
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).roundToInt()
}
