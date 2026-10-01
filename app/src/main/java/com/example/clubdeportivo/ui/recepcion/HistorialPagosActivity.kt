package com.example.clubdeportivo.ui.recepcion

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.personas.PersonaStore

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/** Consulta de snapshots: no cobra ni depende del registro actual de personas. */
class HistorialPagosActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PERSONA_ID = "com.example.clubdeportivo.HISTORIAL_PERSONA_ID"
        const val EXTRA_COMPROBANTE_ID = "com.example.clubdeportivo.HISTORIAL_COMPROBANTE_ID"
        private const val AVISO = "Comprobante simulado. No fiscal. Sin validez para acceso al club."
    }

    private var personaId: String? = null
    private var seleccionadoId: String? = null
    private var detalleMostrado = false
    private var dialogo: AlertDialog? = null
    private lateinit var lista: LinearLayout
    private lateinit var resultado: TextView
    private lateinit var detalle: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            personaId = leerId(EXTRA_PERSONA_ID)
            seleccionadoId = leerId(EXTRA_COMPROBANTE_ID)
            seleccionadoId?.let { require(comprobantePermitido(it) != null) }
            if (savedInstanceState != null) {
                require(savedInstanceState.getString("personaId") == personaId)
                seleccionadoId = savedInstanceState.getString("seleccionadoId")
                require(seleccionadoId == null || uuidValido(seleccionadoId!!))
                detalleMostrado = savedInstanceState.getBoolean("detalleMostrado")
            }
            seleccionadoId?.let { require(comprobantePermitido(it) != null) }
        } catch (_: RuntimeException) {
            rechazar()
            return
        }
        setContentView(R.layout.activity_historial_pagos)
        lista = findViewById(R.id.listaHistorialPagos)
        resultado = findViewById(R.id.tvResultadoHistorial)
        detalle = findViewById(R.id.btnDetalleHistorial)
        findViewById<TextView>(R.id.tvTituloHistorial).text =
            if (personaId == null) "Historial general" else "Historial por persona"
        findViewById<TextView>(R.id.tvAlcanceHistorial).text = personaId?.let {
            "Persona ID: $it\nDatos originales de cada cobro, incluso después de editar o eliminar la persona."
        } ?: "Todos los comprobantes, del más reciente al más antiguo. Se conservan tras eliminar personas."
        findViewById<AppCompatButton>(R.id.btnVolverHistorial).setOnClickListener { finish() }
        detalle.setOnClickListener { seleccionadoId?.let { mostrarDetalle(it) } }
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        refrescar()
        if (!detalleMostrado) seleccionadoId?.let { mostrarDetalle(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("personaId", personaId)
        outState.putString("seleccionadoId", seleccionadoId)
        outState.putBoolean("detalleMostrado", detalleMostrado)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        dialogo?.dismiss()
        super.onDestroy()
    }

    private fun leerId(clave: String): String? {
        if (!intent.hasExtra(clave)) return null
        @Suppress("DEPRECATION")
        val valor = intent.extras?.get(clave)
        require(valor is String && uuidValido(valor))
        return valor
    }

    private fun uuidValido(id: String): Boolean = try {
        UUID.fromString(id).toString() == id
    } catch (_: IllegalArgumentException) {
        false
    }

    private fun comprobantePermitido(id: String): PersonaStore.Comprobante? =
        PersonaStore.getComprobanteById(id)?.takeIf {
            personaId == null || it.personaId == personaId
        }

    private fun refrescar() {
        if (seleccionadoId != null && comprobantePermitido(seleccionadoId!!) == null) {
            rechazar()
            return
        }
        detalle.visibility = if (seleccionadoId == null) View.GONE else View.VISIBLE
        val comprobantes = personaId?.let { PersonaStore.getComprobantesByPersonaId(it) }
            ?: PersonaStore.getComprobantes()
        resultado.text = when {
            comprobantes.isNotEmpty() -> "Comprobantes: ${comprobantes.size} · Más recientes primero"
            personaId != null -> "No hay comprobantes para este identificador de persona."
            else -> "Todavía no hay comprobantes en el historial general."
        }
        lista.removeAllViews()
        for (recibo in comprobantes) {
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setBackgroundResource(R.drawable.bg_card)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            }
            tarjeta.addView(TextView(this).apply {
                text = "${recibo.nombre} ${recibo.apellido}\nDNI: ${recibo.dni} · ${recibo.tipo.etiqueta}\n" +
                    "${recibo.concepto.etiqueta} · ${dinero(recibo.importeCentavos)}\n" +
                    "Fecha y hora: ${fecha(recibo.fechaHora, true)}\n" +
                    "Nuevo vencimiento: ${fecha(recibo.nuevoVencimiento)}"
                textSize = 16f
                setTextColor(Color.WHITE)
            })
            tarjeta.addView(AppCompatButton(this).apply {
                text = "Ver comprobante"
                isAllCaps = false
                minHeight = dp(48)
                contentDescription = "Ver comprobante de ${recibo.nombre} ${recibo.apellido}, del ${fecha(recibo.fechaHora, true)}"
                setOnClickListener { mostrarDetalle(recibo.id) }
            })
            lista.addView(tarjeta)
        }
    }

    private fun mostrarDetalle(id: String) {
        if (dialogo != null || isFinishing) return
        val recibo = comprobantePermitido(id) ?: run {
            rechazar()
            return
        }
        seleccionadoId = id
        detalleMostrado = true
        detalle.visibility = View.VISIBLE
        dialogo = AlertDialog.Builder(this)
            .setTitle("Comprobante simulado")
            .setMessage("Comprobante ID: ${recibo.id}\nOperación ID: ${recibo.operacionId}\n" +
                "Fecha y hora: ${fecha(recibo.fechaHora, true)}\n\n" +
                "Persona ID: ${recibo.personaId}\n${recibo.nombre} ${recibo.apellido}\n" +
                "DNI: ${recibo.dni}\nTipo original: ${recibo.tipo.etiqueta}\n" +
                "Concepto: ${recibo.concepto.etiqueta}\nImporte: ${dinero(recibo.importeCentavos)}\n\n" +
                "Vencimiento anterior: ${fecha(recibo.vencimientoAnterior)}\n" +
                "Nuevo vencimiento: ${fecha(recibo.nuevoVencimiento)}\n\n$AVISO")
            .setPositiveButton("Cerrar", null)
            .create().also { ventana ->
                ventana.setOnDismissListener { dialogo = null }
                ventana.show()
            }
    }

    private fun rechazar() {
        Toast.makeText(this, "No se puede abrir el historial: identificador inválido, comprobante inexistente o de otra persona.", Toast.LENGTH_LONG).show()
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun dinero(centavos: Long): String =
        "ARS ${BigDecimal.valueOf(centavos, 2).toPlainString().replace('.', ',')}"

    private fun fecha(valor: Long?, conHora: Boolean = false): String = valor?.let {
        SimpleDateFormat(if (conHora) "dd/MM/yyyy HH:mm:ss" else "dd/MM/yyyy",
            Locale.forLanguageTag("es-AR")).format(Date(it))
    } ?: "Sin fecha"

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).roundToInt()
}
