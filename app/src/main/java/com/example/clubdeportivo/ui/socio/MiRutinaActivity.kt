package com.example.clubdeportivo.ui.socio

import android.content.Intent
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.acceso.DemoAccess
import com.example.clubdeportivo.ui.inicio.MainActivity

class MiRutinaActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_CONSULTA_PROFESOR = "consulta_profesor"
        const val EXTRA_ALUMNO_ID = "consulta_alumno_id"
        const val EXTRA_ALUMNO_NOMBRE = "consulta_alumno_nombre"
    }

    private var consultaProfesor = false

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
        consultaProfesor = intent.getBooleanExtra(EXTRA_CONSULTA_PROFESOR, false)
        if (consultaProfesor && !autorizarConsulta()) {
            return
        }
        setContentView(R.layout.activity_mi_rutina)
        if (consultaProfesor) {
            findViewById<TextView>(R.id.routineTitle).setText(R.string.staff_routine_title)
            val nombre = intent.getStringExtra(EXTRA_ALUMNO_NOMBRE)
                ?.takeIf { it.isNotBlank() } ?: getString(R.string.staff_unknown_student)
            val id = intent.getStringExtra(EXTRA_ALUMNO_ID).orEmpty()
            findViewById<TextView>(R.id.routineContext).text = getString(R.string.staff_routine_context, nombre, id)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val root = findViewById<View>(R.id.routineRoot)
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
            ViewCompat.requestApplyInsets(root)
        }

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

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

    override fun onResume() {
        super.onResume()
        if (consultaProfesor) {
            autorizarConsulta()
        }
    }

    private fun autorizarConsulta(): Boolean {
        val profile = DemoAccess.currentProfile
        if (profile == null || profile.role != DemoAccess.Role.PROFESOR) {
            if (!isFinishing) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            return false
        }
        return true
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
        nombre.typeface = ResourcesCompat.getFont(this, R.font.inter_bold)
        tarjeta.addView(nombre)

        val detalle = TextView(this)
        detalle.text = "${ejercicio.series} · ${ejercicio.repeticiones}"
        detalle.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
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
        notas.typeface = ResourcesCompat.getFont(this, R.font.inter_regular)
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
        realizado.text = if (consultaProfesor) {
            getString(R.string.staff_routine_read_only)
        } else {
            "Realizado"
        }
        realizado.typeface = ResourcesCompat.getFont(this, R.font.inter_medium)
        realizado.isEnabled = !consultaProfesor
        realizado.textSize = 14f
        realizado.setTextColor(0xFFFFFFFF.toInt())
        realizado.buttonTintList = ColorStateList.valueOf(0xFFFFD54F.toInt())
        realizado.isChecked = ejerciciosCompletados.contains(clave)
        actualizarAspectoTarjeta(tarjeta, nombre, realizado.isChecked)
        realizado.setOnCheckedChangeListener { _, marcado ->
            if (consultaProfesor) {
                return@setOnCheckedChangeListener
            }
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
            ).apply {
                bottomMargin = 16
            }
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
