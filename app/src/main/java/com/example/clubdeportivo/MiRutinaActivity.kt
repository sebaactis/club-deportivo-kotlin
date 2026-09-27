package com.example.clubdeportivo

import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MiRutinaActivity : AppCompatActivity() {

    private val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    private val ejerciciosCompletados = mutableSetOf<String>()
    private val ejerciciosPorDia = mapOf(
        "Lunes" to listOf(
            Ejercicio("Sentadillas", "3 series", "12 repeticiones", "Descansa 60 segundos entre series."),
            Ejercicio("Flexiones", "3 series", "10 repeticiones", "Apoya las rodillas si lo necesitas."),
            Ejercicio("Plancha", "3 series", "30 segundos", "Mantén la espalda recta."),
        ),
        "Martes" to listOf(
            Ejercicio("Remo con mancuerna", "3 series", "12 por lado", "Usa un peso cómodo."),
            Ejercicio("Press de hombros", "3 series", "10 repeticiones", "Evita arquear la espalda."),
            Ejercicio("Curl de bíceps", "3 series", "12 repeticiones", "Haz el movimiento despacio."),
        ),
        "Miércoles" to listOf(
            Ejercicio("Puente de glúteos", "3 series", "15 repeticiones", "Aprieta los glúteos arriba."),
            Ejercicio("Peso muerto ligero", "3 series", "10 repeticiones", "Mantén la espalda recta."),
            Ejercicio("Elevación de talones", "3 series", "15 repeticiones", "Sujétate para mantener el equilibrio."),
        ),
        "Jueves" to listOf(
            Ejercicio("Jalón al pecho", "3 series", "12 repeticiones", "Lleva la barra hacia el pecho."),
            Ejercicio("Press de pecho", "3 series", "10 repeticiones", "Controla el peso al bajarlo."),
            Ejercicio("Abdominales", "3 series", "15 repeticiones", "No tires del cuello."),
        ),
        "Viernes" to listOf(
            Ejercicio("Zancadas", "3 series", "10 por pierna", "Da un paso cómodo y estable."),
            Ejercicio("Extensión de tríceps", "3 series", "12 repeticiones", "Mantén los codos cerca del cuerpo."),
            Ejercicio("Plancha lateral", "3 series", "20 segundos por lado", "Mantén el cuerpo alineado."),
        ),
        "Sábado" to listOf(
            Ejercicio("Bicicleta estática", "1 serie", "15 minutos", "Mantén un ritmo cómodo."),
            Ejercicio("Bird-dog", "3 series", "10 por lado", "Extiende brazo y pierna contrarios."),
            Ejercicio("Estiramiento general", "1 serie", "5 minutos", "Estira sin rebotes."),
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_rutina)

        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }

        val selectorDia = findViewById<Spinner>(R.id.selectorDia)
        val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, dias)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selectorDia.adapter = adaptador
        selectorDia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mostrarEjercicios(dias[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun mostrarEjercicios(dia: String) {
        val listaEjercicios = findViewById<LinearLayout>(R.id.exerciseList)
        listaEjercicios.removeAllViews()

        for (ejercicio in ejerciciosPorDia[dia].orEmpty()) {
            listaEjercicios.addView(crearTarjeta(ejercicio, dia))
        }
    }

    private fun crearTarjeta(ejercicio: Ejercicio, dia: String): View {
        val tarjeta = LinearLayout(this)
        tarjeta.orientation = LinearLayout.VERTICAL
        tarjeta.setBackgroundResource(R.drawable.bg_card)
        tarjeta.setPadding(24, 20, 24, 16)

        val nombre = TextView(this)
        nombre.text = ejercicio.nombre
        nombre.textSize = 18f
        nombre.setTextColor(0xFFFFFFFF.toInt())
        nombre.setTypeface(null, android.graphics.Typeface.BOLD)
        tarjeta.addView(nombre)

        val detalle = TextView(this)
        detalle.text = "${ejercicio.series} · ${ejercicio.repeticiones}"
        detalle.textSize = 14f
        detalle.setTextColor(0xFFFFD54F.toInt())
        val detalleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        detalleParams.topMargin = 8
        tarjeta.addView(detalle, detalleParams)

        val notas = TextView(this)
        notas.text = ejercicio.notas
        notas.textSize = 14f
        notas.setTextColor(0xFF94A3B8.toInt())
        val notasParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        notasParams.topMargin = 6
        tarjeta.addView(notas, notasParams)

        val clave = "$dia-${ejercicio.nombre}"
        val realizado = CheckBox(this)
        realizado.text = "Realizado"
        realizado.textSize = 14f
        realizado.setTextColor(0xFFFFFFFF.toInt())
        realizado.buttonTintList = ColorStateList.valueOf(0xFFFFD54F.toInt())
        realizado.isChecked = ejerciciosCompletados.contains(clave)
        actualizarAspectoTarjeta(tarjeta, nombre, realizado.isChecked)
        realizado.setOnCheckedChangeListener { _, marcado ->
            if (marcado) {
                ejerciciosCompletados.add(clave)
            } else {
                ejerciciosCompletados.remove(clave)
            }
            actualizarAspectoTarjeta(tarjeta, nombre, marcado)
        }
        val checkParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        checkParams.topMargin = 8
        tarjeta.addView(realizado, checkParams)

        return tarjeta.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 16 }
        }
    }

    private fun actualizarAspectoTarjeta(tarjeta: View, nombre: TextView, completado: Boolean) {
        tarjeta.alpha = if (completado) 0.6f else 1f
        if (completado) {
            nombre.paintFlags = nombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            nombre.paintFlags = nombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private data class Ejercicio(
        val nombre: String,
        val series: String,
        val repeticiones: String,
        val notas: String,
    )
}
