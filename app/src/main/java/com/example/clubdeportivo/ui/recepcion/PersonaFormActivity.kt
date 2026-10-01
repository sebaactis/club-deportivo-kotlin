package com.example.clubdeportivo.ui.recepcion

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.personas.PersonaStore

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText

class PersonaFormActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PERSONA_ID = "com.example.clubdeportivo.PERSONA_ID"
    }

    private var personaId: String? = null
    private var tipoSeleccionado = -1
    private var guardando = false
    private lateinit var nombre: AppCompatEditText
    private lateinit var apellido: AppCompatEditText
    private lateinit var dni: AppCompatEditText
    private lateinit var tipo: AppCompatButton
    private lateinit var guardar: AppCompatButton
    private lateinit var mensaje: TextView
    private val tipos = PersonaStore.Tipo.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Solo la ausencia del extra permite crear; un extra inválido nunca es un alta.
        if (intent.hasExtra(EXTRA_PERSONA_ID)) {
            personaId = try {
                intent.getStringExtra(EXTRA_PERSONA_ID)
            } catch (_: RuntimeException) {
                null
            }
            if (personaId.isNullOrBlank() || PersonaStore.getById(personaId!!) == null) {
                rechazarEdicion()
                return
            }
        }
        if (savedInstanceState != null && savedInstanceState.getString("personaId") != personaId) {
            rechazarEdicion()
            return
        }
        setContentView(R.layout.activity_persona_form)
        nombre = findViewById(R.id.etNombrePersona)
        apellido = findViewById(R.id.etApellidoPersona)
        dni = findViewById(R.id.etDniPersona)
        tipo = findViewById(R.id.btnTipoPersona)
        guardar = findViewById(R.id.btnGuardarPersona)
        mensaje = findViewById(R.id.tvMensajePersona)
        findViewById<TextView>(R.id.tvTituloPersona).text =
            if (personaId == null) "Nueva persona" else "Editar persona"
        findViewById<AppCompatButton>(R.id.btnVolverPersona).setOnClickListener { finish() }

        if (savedInstanceState != null) {
            nombre.setText(savedInstanceState.getString("nombre", ""))
            apellido.setText(savedInstanceState.getString("apellido", ""))
            dni.setText(savedInstanceState.getString("dni", ""))
            tipoSeleccionado = savedInstanceState.getInt("tipo", -1)
                .takeIf { it in tipos.indices } ?: -1
            mensaje.text = savedInstanceState.getString("mensaje", "")
        } else {
            personaId?.let { id ->
                PersonaStore.getById(id)?.let { persona ->
                    nombre.setText(persona.nombre)
                    apellido.setText(persona.apellido)
                    dni.setText(persona.dni)
                    tipoSeleccionado = tipos.indexOf(persona.tipo)
                }
            }
        }
        actualizarTipo()
        tipo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar tipo de persona")
                .setSingleChoiceItems(tipos.map { it.etiqueta }.toTypedArray(), tipoSeleccionado) { dialog, opcion ->
                    tipoSeleccionado = opcion
                    actualizarTipo()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        guardar.setOnClickListener { guardarPersona() }
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing && personaId?.let { PersonaStore.getById(it) == null } == true) {
            rechazarEdicion()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("personaId", personaId)
        if (::nombre.isInitialized) {
            outState.putString("nombre", nombre.text.toString())
            outState.putString("apellido", apellido.text.toString())
            outState.putString("dni", dni.text.toString())
            outState.putInt("tipo", tipoSeleccionado)
            outState.putString("mensaje", mensaje.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    private fun actualizarTipo() {
        tipo.text = tipos.getOrNull(tipoSeleccionado)?.etiqueta ?: "Seleccionar tipo (obligatorio)"
    }

    private fun guardarPersona() {
        if (guardando || isFinishing) return
        if (personaId?.let { PersonaStore.getById(it) == null } == true) {
            rechazarEdicion()
            return
        }
        val seleccion = tipos.getOrNull(tipoSeleccionado)
        if (seleccion == null) {
            mensaje.text = "Selecciona Socio o No Socio antes de guardar."
            tipo.requestFocus()
            return
        }
        guardando = true
        guardar.isEnabled = false
        val resultado = personaId?.let { id ->
            PersonaStore.update(id, nombre.text.toString(), apellido.text.toString(), dni.text.toString(), seleccion)
        } ?: PersonaStore.create(nombre.text.toString(), apellido.text.toString(), dni.text.toString(), seleccion)
        when (resultado) {
            is PersonaStore.Resultado.Guardada -> {
                setResult(RESULT_OK)
                finish()
            }
            is PersonaStore.Resultado.Fallo -> {
                if (resultado.error == PersonaStore.Error.NO_ENCONTRADA) {
                    rechazarEdicion()
                    return
                }
                mensaje.text = when (resultado.error) {
                    PersonaStore.Error.NOMBRE_VACIO -> "Ingresa el nombre."
                    PersonaStore.Error.APELLIDO_VACIO -> "Ingresa el apellido."
                    PersonaStore.Error.DNI_INVALIDO -> "DNI inválido: usa 7 u 8 dígitos, sin puntos ni espacios internos; no todos ceros."
                    PersonaStore.Error.DNI_DUPLICADO -> "Ya existe otra persona con ese DNI."
                    PersonaStore.Error.NO_ENCONTRADA -> "La persona ya no existe."
                }
                guardando = false
                guardar.isEnabled = true
            }
            is PersonaStore.Resultado.Eliminada -> {
                mensaje.text = "No se pudo guardar la persona."
                guardando = false
                guardar.isEnabled = true
            }
        }
    }

    private fun rechazarEdicion() {
        Toast.makeText(this, "No se puede editar: la persona no existe o el identificador es inválido.", Toast.LENGTH_LONG).show()
        setResult(RESULT_CANCELED)
        finish()
    }
}
