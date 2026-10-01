package com.example.clubdeportivo.ui.recepcion

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.personas.PersonaStore

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ReceptionActivity : AppCompatActivity() {
    private lateinit var consulta: AppCompatEditText
    private lateinit var soloVencidos: AppCompatCheckBox
    private lateinit var botonTipo: AppCompatButton
    private lateinit var resumen: AppCompatButton
    private lateinit var resultado: TextView
    private lateinit var lista: LinearLayout
    // Índices estables para Bundle y selector: todos, socios, no socios.
    private var filtroTipo = 0
    private val tipos = arrayOf("Todos", "Socios", "No Socios")
    private val formulario = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            limpiarFiltros()
            Toast.makeText(this, "Persona guardada. Se muestran todas las personas.", Toast.LENGTH_LONG).show()
        }
        refrescar()
    }

    private val cobro = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            Toast.makeText(this, "Cobro simulado registrado. Lista y vencimientos actualizados.", Toast.LENGTH_LONG).show()
        }
        // Conserva filtros; una persona renovada puede dejar de aparecer en Solo vencidos.
        refrescar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reception)
        consulta = findViewById(R.id.etBuscarDniReception)
        soloVencidos = findViewById(R.id.cbSoloVencidosReception)
        botonTipo = findViewById(R.id.btnTipoReception)
        resumen = findViewById(R.id.btnResumenVencidos)
        resultado = findViewById(R.id.tvResultadoReception)
        lista = findViewById(R.id.listaPersonasReception)

        filtroTipo = (savedInstanceState?.getInt("tipo", 0) ?: 0).coerceIn(0, 2)
        consulta.setText(savedInstanceState?.getString("consulta") ?: "")
        soloVencidos.isChecked = savedInstanceState?.getBoolean("vencidos", false) ?: false
        findViewById<AppCompatButton>(R.id.btnVolverReception).setOnClickListener { finish() }
        findViewById<AppCompatButton>(R.id.btnNuevaPersonaReception).setOnClickListener {
            formulario.launch(Intent(this, PersonaFormActivity::class.java))
        }
        findViewById<AppCompatButton>(R.id.btnConfigurarTarifasReception).setOnClickListener {
            cobro.launch(Intent(this, CobroActivity::class.java))
        }
        findViewById<AppCompatButton>(R.id.btnHistorialGeneralReception).setOnClickListener {
            startActivity(Intent(this, HistorialPagosActivity::class.java))
        }
        findViewById<AppCompatButton>(R.id.btnLimpiarFiltrosReception).setOnClickListener {
            limpiarFiltros()
        }
        botonTipo.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Filtrar por tipo")
                .setSingleChoiceItems(tipos, filtroTipo) { dialog, opcion ->
                    filtroTipo = opcion
                    refrescar()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        soloVencidos.setOnCheckedChangeListener { _, _ -> refrescar() }
        resumen.setOnClickListener {
            // El resumen abre todos los vencidos, incluso si había otros filtros.
            filtroTipo = 0
            consulta.setText("")
            soloVencidos.isChecked = true
            refrescar()
        }
        consulta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refrescar()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("consulta", consulta.text.toString())
        outState.putInt("tipo", filtroTipo)
        outState.putBoolean("vencidos", soloVencidos.isChecked)
        super.onSaveInstanceState(outState)
    }

    private fun limpiarFiltros() {
        filtroTipo = 0
        consulta.setText("")
        soloVencidos.isChecked = false
        refrescar()
    }

    private fun confirmarBaja(persona: PersonaStore.Persona) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar persona")
            .setMessage("¿Eliminar a ${persona.nombre} ${persona.apellido}, DNI ${persona.dni}, del registro demo? Esta acción no modifica clases ni reservas.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                val resultado = PersonaStore.delete(persona.id)
                val texto = if (resultado is PersonaStore.Resultado.Eliminada)
                    "Persona eliminada." else "No se pudo eliminar: la persona ya no existe."
                Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
                refrescar()
            }
            .show()
    }

    private fun refrescar() {
        val todas = PersonaStore.getAll()
        val hoy = System.currentTimeMillis()
        val vencidas = todas.count { it.estado(hoy) == PersonaStore.Estado.VENCIDA }
        resumen.text = "Vencidos del registro: $vencidas · Ver todos"
        botonTipo.text = "Tipo: ${tipos[filtroTipo]} · Cambiar"
        lista.removeAllViews()
        val dni = consulta.text.toString().trim()
        if (dni.isNotEmpty() && !PersonaStore.dniValido(dni)) {
            resultado.text = "DNI inválido: usa 7 u 8 dígitos, sin puntos ni espacios internos; no todos ceros."
            return
        }
        val visibles = todas.filter { persona ->
            (dni.isEmpty() || persona.dni == dni) &&
                (filtroTipo == 0 || persona.tipo == if (filtroTipo == 1)
                    PersonaStore.Tipo.SOCIO else PersonaStore.Tipo.NO_SOCIO) &&
                (!soloVencidos.isChecked || persona.estado(hoy) == PersonaStore.Estado.VENCIDA)
        }
        resultado.text = when {
            todas.isEmpty() -> "Todavía no hay personas en el registro demo."
            visibles.isEmpty() -> "No hay coincidencias para este DNI y los filtros seleccionados."
            else -> "Personas encontradas: ${visibles.size}"
        }
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-AR"))
        for (persona in visibles) {
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setBackgroundResource(R.drawable.bg_card)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            }
            tarjeta.addView(TextView(this).apply {
                text = "${persona.nombre} ${persona.apellido}"
                textSize = 18f
                setTextColor(Color.WHITE)
            })
            val fecha = persona.vencimientoNullable?.let { formato.format(Date(it)) } ?: "Sin fecha"
            tarjeta.addView(TextView(this).apply {
                text = "DNI: ${persona.dni} · ${persona.tipo.etiqueta}\n" +
                    "Estado: ${persona.estado(hoy).etiqueta}\nVencimiento: $fecha"
                textSize = 14f
                setTextColor(Color.parseColor("#94A3B8"))
                setPadding(0, dp(8), 0, 0)
            })
            val acciones = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            acciones.addView(AppCompatButton(this).apply {
                text = "Editar"
                isAllCaps = false
                minHeight = dp(48)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                contentDescription = "Editar a ${persona.nombre} ${persona.apellido}, DNI ${persona.dni}"
                setOnClickListener {
                    formulario.launch(Intent(this@ReceptionActivity, PersonaFormActivity::class.java)
                        .putExtra(PersonaFormActivity.EXTRA_PERSONA_ID, persona.id))
                }
            })
            acciones.addView(AppCompatButton(this).apply {
                text = "Eliminar"
                isAllCaps = false
                minHeight = dp(48)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                contentDescription = "Eliminar a ${persona.nombre} ${persona.apellido}, DNI ${persona.dni}"
                setOnClickListener { confirmarBaja(persona) }
            })
            tarjeta.addView(acciones)
            tarjeta.addView(AppCompatButton(this).apply {
                text = if (persona.tipo == PersonaStore.Tipo.SOCIO) "Cobrar cuota" else "Pase diario"
                isAllCaps = false
                minHeight = dp(48)
                contentDescription = "$text para ${persona.nombre} ${persona.apellido}, DNI ${persona.dni}"
                setOnClickListener {
                    cobro.launch(Intent(this@ReceptionActivity, CobroActivity::class.java)
                        .putExtra(CobroActivity.EXTRA_PERSONA_ID, persona.id))
                }
            })
            tarjeta.addView(AppCompatButton(this).apply {
                text = "Historial"
                isAllCaps = false
                minHeight = dp(48)
                contentDescription = "Historial de ${persona.nombre} ${persona.apellido}, DNI ${persona.dni}"
                setOnClickListener {
                    startActivity(Intent(this@ReceptionActivity, HistorialPagosActivity::class.java)
                        .putExtra(HistorialPagosActivity.EXTRA_PERSONA_ID, persona.id))
                }
            })
            lista.addView(tarjeta)
        }
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).roundToInt()
}
