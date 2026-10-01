package com.example.clubdeportivo.ui.nutricion

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.nutricion.NutricionStore
import com.example.clubdeportivo.data.nutricion.TurnoNutricion

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class NutricionActivity : AppCompatActivity() {
    private lateinit var selectorDia: Spinner
    private lateinit var selectorHora: Spinner
    private lateinit var guardar: AppCompatButton
    private lateinit var salirEdicion: AppCompatButton
    private lateinit var mensaje: TextView
    private var idEnEdicion: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutricion)
        selectorDia = findViewById(R.id.selectorDiaNutricion)
        selectorHora = findViewById(R.id.selectorHoraNutricion)
        guardar = findViewById(R.id.btnGuardarTurno)
        salirEdicion = findViewById(R.id.btnSalirEdicion)
        mensaje = findViewById(R.id.mensajeNutricion)

        prepararSelector(selectorDia, listOf("Elige un día") + NutricionStore.dias)
        prepararSelector(selectorHora, listOf("Elige un horario") + NutricionStore.horarios)
        findViewById<TextView>(R.id.backButton).setOnClickListener { finish() }
        guardar.setOnClickListener { guardarTurno() }
        salirEdicion.setOnClickListener {
            limpiarFormulario()
            informar("Edición descartada. El turno no se modificó.")
        }

        // Conservamos el modo edición al recrear la pantalla; Android restaura los selectores.
        if (savedInstanceState?.containsKey("idEnEdicion") == true) {
            idEnEdicion = savedInstanceState.getInt("idEnEdicion")
            mostrarModoEdicion()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        idEnEdicion?.let { outState.putInt("idEnEdicion", it) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        val id = idEnEdicion
        if (id != null && NutricionStore.getAll().none { it.id == id }) {
            limpiarFormulario()
            informar("El turno que estabas editando ya no existe.")
        }
        mostrarTurnos()
    }

    private fun prepararSelector(selector: Spinner, opciones: List<String>) {
        selector.adapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, opciones
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return decorar(super.getView(position, convertView, parent))
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return decorar(super.getDropDownView(position, convertView, parent))
            }

            private fun decorar(view: View): View {
                return (view as TextView).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#102A49"))
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    minHeight = dp(48)
                    textSize = 16f
                }
            }
        }
    }

    private fun guardarTurno() {
        if (selectorDia.selectedItemPosition <= 0 || selectorHora.selectedItemPosition <= 0) {
            informar("Elige un día y un horario para continuar.")
            return
        }
        val dia = selectorDia.selectedItem.toString()
        val hora = selectorHora.selectedItem.toString()
        val id = idEnEdicion
        val resultado = if (id == null) NutricionStore.crear(dia, hora)
                        else NutricionStore.actualizar(id, dia, hora)
        when (resultado) {
            NutricionStore.Resultado.EXITO -> {
                limpiarFormulario()
                informar(if (id == null) "Turno reservado en esta demostración." else "Cambios guardados.")
            }
            NutricionStore.Resultado.INVALIDO -> informar("Elige un día y un horario de ejemplo válidos.")
            NutricionStore.Resultado.DUPLICADO -> informar("Ya tienes un turno para ese día y horario.")
            NutricionStore.Resultado.NO_ENCONTRADO -> {
                limpiarFormulario()
                informar("El turno ya no existe. Puedes reservar otro.")
            }
        }
        mostrarTurnos()
    }

    private fun editarTurno(id: Int) {
        val turno = NutricionStore.getAll().find { it.id == id }
        if (turno == null) {
            informar("El turno ya no existe.")
            mostrarTurnos()
            return
        }
        idEnEdicion = id
        selectorDia.setSelection(NutricionStore.dias.indexOf(turno.dia) + 1)
        selectorHora.setSelection(NutricionStore.horarios.indexOf(turno.hora) + 1)
        mostrarModoEdicion()
        informar("Editando ${turno.dia} · ${turno.hora}. Guarda para aplicar los cambios.")
        findViewById<View>(R.id.formularioNutricion).requestFocus()
    }

    private fun mostrarModoEdicion() {
        guardar.text = "Guardar cambios"
        salirEdicion.visibility = View.VISIBLE
    }

    private fun limpiarFormulario() {
        idEnEdicion = null
        selectorDia.setSelection(0)
        selectorHora.setSelection(0)
        guardar.text = "Reservar turno"
        salirEdicion.visibility = View.GONE
    }

    private fun confirmarCancelacion(turno: TurnoNutricion) {
        AlertDialog.Builder(this)
            .setTitle("Cancelar turno")
            .setMessage("¿Quieres cancelar el turno del ${turno.dia} a las ${turno.hora}?")
            .setNegativeButton("Conservar turno", null)
            .setPositiveButton("Cancelar turno") { _, _ ->
                val resultado = NutricionStore.eliminar(turno.id)
                if (idEnEdicion == turno.id) limpiarFormulario()
                informar(if (resultado == NutricionStore.Resultado.EXITO) "Turno cancelado."
                         else "El turno ya no existe.")
                mostrarTurnos()
            }
            .show()
    }

    private fun mostrarTurnos() {
        val lista = findViewById<LinearLayout>(R.id.listaTurnosNutricion)
        val vacio = findViewById<TextView>(R.id.sinTurnosNutricion)
        val turnos = NutricionStore.getAll()
        lista.removeAllViews()
        vacio.visibility = if (turnos.isEmpty()) View.VISIBLE else View.GONE
        for (turno in turnos) {
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(dp(16), dp(16), dp(16), dp(16))
            }
            tarjeta.addView(TextView(this).apply {
                text = "${turno.dia} · ${turno.hora}"
                textSize = 18f
                setTextColor(Color.WHITE)
            })
            // Los callbacks usan el id estable, nunca la posición en la lista.
            tarjeta.addView(boton("Modificar") { editarTurno(turno.id) })
            tarjeta.addView(boton("Cancelar") { confirmarCancelacion(turno) })
            lista.addView(tarjeta, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(12)
            })
        }
    }

    private fun boton(titulo: String, accion: () -> Unit): AppCompatButton {
        return AppCompatButton(this).apply {
            text = titulo
            setAllCaps(false)
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#102A49"))
            minHeight = dp(48)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) }
            setOnClickListener { accion() }
        }
    }

    private fun informar(texto: String) {
        mensaje.text = texto
        mensaje.visibility = View.VISIBLE
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density + 0.5f).toInt()
}
