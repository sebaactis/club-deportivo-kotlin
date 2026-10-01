package com.example.clubdeportivo.ui.recepcion

import com.example.clubdeportivo.R

import com.example.clubdeportivo.data.personas.PersonaStore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Configuración y cobro demo: solo la confirmación visible modifica la vigencia. */
class CobroActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_PERSONA_ID = "com.example.clubdeportivo.COBRO_PERSONA_ID"
    }

    private var personaId: String? = null
    private var operacionId = UUID.randomUUID().toString()
    private var terminado = false
    private var comprobanteId: String? = null
    private var textoComprobante = ""
    private var confirmando = false
    private var dialogo: AlertDialog? = null
    private var resumen: PersonaStore.ResumenCobro? = null
    private lateinit var mensual: AppCompatEditText
    private lateinit var diaria: AppCompatEditText
    private lateinit var tarifas: TextView
    private lateinit var identidad: TextView
    private lateinit var vistaResumen: TextView
    private lateinit var mensaje: TextView
    private lateinit var cobrar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.hasExtra(EXTRA_PERSONA_ID)) {
            personaId = try {
                @Suppress("DEPRECATION")
                val valor = intent.extras?.get(EXTRA_PERSONA_ID)
                valor as? String
            } catch (_: RuntimeException) {
                null
            }

            val reciboGuardado = savedInstanceState?.getString("comprobanteId")
                ?.let { PersonaStore.getComprobanteById(it) }

            val finalizadoGuardado = savedInstanceState?.getBoolean("terminado") == true &&
                reciboGuardado != null && reciboGuardado.personaId == personaId

            if (personaId.isNullOrBlank() ||
                (PersonaStore.getById(personaId!!) == null && !finalizadoGuardado)) {
                rechazarPersona()
                return
            }
        }
        if (savedInstanceState != null && savedInstanceState.getString("personaId") != personaId) {
            rechazarPersona()
            return
        }

        setContentView(R.layout.activity_cobro)
        mensual = findViewById(R.id.etTarifaMensual)
        diaria = findViewById(R.id.etTarifaDiaria)
        tarifas = findViewById(R.id.tvTarifasActuales)
        identidad = findViewById(R.id.tvPersonaCobro)
        vistaResumen = findViewById(R.id.tvResumenCobro)
        mensaje = findViewById(R.id.tvMensajeCobro)
        cobrar = findViewById(R.id.btnPrevisualizarCobro)

        findViewById<TextView>(R.id.tvTituloCobro).text =
            if (personaId == null) "Configurar tarifas" else "Cobro simulado"
        findViewById<AppCompatButton>(R.id.btnVolverCobro).setOnClickListener { finish() }
        findViewById<AppCompatButton>(R.id.btnGuardarTarifas).setOnClickListener { guardarTarifas() }

        cobrar.setOnClickListener { mostrarConfirmacion() }

        findViewById<AppCompatButton>(R.id.btnVerComprobanteCobro).setOnClickListener {
            val recibo = comprobanteFinalizado() ?: return@setOnClickListener
            startActivity(Intent(this, HistorialPagosActivity::class.java)
                .putExtra(HistorialPagosActivity.EXTRA_PERSONA_ID, recibo.personaId)
                .putExtra(HistorialPagosActivity.EXTRA_COMPROBANTE_ID, recibo.id))
        }

        if (savedInstanceState != null) {
            operacionId = savedInstanceState.getString("operacionId") ?: operacionId
            terminado = savedInstanceState.getBoolean("terminado")
            comprobanteId = savedInstanceState.getString("comprobanteId")
            textoComprobante = savedInstanceState.getString("textoComprobante", "")
            mensual.setText(savedInstanceState.getString("mensual", ""))
            diaria.setText(savedInstanceState.getString("diaria", ""))
            mensaje.text = savedInstanceState.getString("mensaje", "")
        } else {
            PersonaStore.getTarifas()?.let {
                mensual.setText(decimal(it.mensualCentavos))
                diaria.setText(decimal(it.diariaCentavos))
            }
        }
        if (terminado) setResult(RESULT_OK)
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) refrescar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("personaId", personaId)
        outState.putString("operacionId", operacionId)
        outState.putBoolean("terminado", terminado)
        outState.putString("comprobanteId", comprobanteId)
        outState.putString("textoComprobante", textoComprobante)

        if (::mensual.isInitialized) {
            outState.putString("mensual", mensual.text.toString())
            outState.putString("diaria", diaria.text.toString())
            outState.putString("mensaje", mensaje.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        dialogo?.dismiss()
        super.onDestroy()
    }

    private fun guardarTarifas() {
        if (confirmando || terminado || isFinishing) return
        val importeMensual = PersonaStore.parsearCentavos(mensual.text.toString())
        val importeDiario = PersonaStore.parsearCentavos(diaria.text.toString())
        val error = "Importe inválido: debe ser positivo, sin separadores de miles, con hasta 2 decimales y dentro del rango admitido."

        mensual.error = if (importeMensual == null) error else null
        diaria.error = if (importeDiario == null) error else null

        if (importeMensual == null || importeDiario == null) {
            mensaje.text = "$error No se guardó ninguna tarifa."
            (if (importeMensual == null) mensual else diaria).requestFocus()
            return
        }
        when (val resultado = PersonaStore.configurarTarifas(importeMensual, importeDiario)) {
            is PersonaStore.ResultadoCobro.TarifasGuardadas -> {
                mensaje.text = "Ambas tarifas guardadas. Solo afectan cobros futuros."
                refrescar()
            }
            is PersonaStore.ResultadoCobro.Fallo -> mensaje.text = explicar(resultado.error)
            else -> mensaje.text = "No se pudieron guardar las tarifas."
        }
    }

    private fun refrescar() {
        val actuales = PersonaStore.getTarifas()

        tarifas.text = actuales?.let {
            "Tarifas guardadas: mensual ${dinero(it.mensualCentavos)} · diaria ${dinero(it.diariaCentavos)}"
        } ?: "Tarifas sin configurar. Guarda ambos importes para habilitar el cobro."
        findViewById<AppCompatButton>(R.id.btnGuardarTarifas).isEnabled = !terminado && !confirmando

        mensual.isEnabled = !terminado && !confirmando
        diaria.isEnabled = !terminado && !confirmando
        cobrar.visibility = if (personaId == null) View.GONE else View.VISIBLE

        val reciboFinal = comprobanteFinalizado()

        findViewById<AppCompatButton>(R.id.btnVerComprobanteCobro).visibility =
            if (reciboFinal == null) View.GONE else View.VISIBLE

        if (terminado) {
            resumen = null
            reciboFinal?.let {
                identidad.text = "${it.nombre} ${it.apellido}\nDNI: ${it.dni} · ${it.tipo.etiqueta} (al cobrar)"
            }
            vistaResumen.text = textoComprobante
            cobrar.text = "Cobro finalizado · Vuelve a Recepción"
            cobrar.isEnabled = false
            return
        }
        val id = personaId ?: run {
            identidad.text = "Solo configuración: esta pantalla no cobra a ninguna persona. Volver no guarda cambios."
            vistaResumen.text = ""
            return
        }
        val persona = PersonaStore.getById(id) ?: run {
            rechazarPersona()
            return
        }

        identidad.text = "${persona.nombre} ${persona.apellido}\nDNI: ${persona.dni} · ${persona.tipo.etiqueta}\n" +
            "Estado: ${persona.estado().etiqueta}\nVencimiento actual: ${fecha(persona.vencimientoNullable)}"

        val concepto = if (persona.tipo == PersonaStore.Tipo.SOCIO)
            PersonaStore.Concepto.CUOTA_MENSUAL else PersonaStore.Concepto.PASE_DIARIO
        when (val resultado = PersonaStore.previsualizarCobro(id, concepto, operacionId)) {
            is PersonaStore.ResultadoCobro.Resumen -> {
                resumen = resultado.resumen
                vistaResumen.text = describir(resultado.resumen)
                cobrar.isEnabled = !confirmando
            }
            is PersonaStore.ResultadoCobro.Fallo -> {
                resumen = null
                vistaResumen.text = explicar(resultado.error)
                cobrar.isEnabled = false
            }
            else -> {
                resumen = null
                vistaResumen.text = "No se pudo preparar el cobro."
                cobrar.isEnabled = false
            }
        }
    }

    private fun mostrarConfirmacion() {
        if (confirmando || terminado || isFinishing || personaId == null) return
        refrescar()

        val pendiente = resumen ?: return
        confirmando = true
        refrescar()

        dialogo = AlertDialog.Builder(this)
            .setTitle("Confirmar cobro simulado")
            .setMessage("${pendiente.persona.nombre} ${pendiente.persona.apellido} · DNI ${pendiente.persona.dni}\n\n" +
                describir(pendiente) + "\n\nDemo: no se mueve dinero real. No es un comprobante fiscal ni una credencial de acceso.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar cobro simulado") { _, _ -> confirmar(pendiente) }
            .create().also { ventana ->
                ventana.setOnDismissListener {
                    confirmando = false
                    dialogo = null
                    if (!isFinishing && !isDestroyed) refrescar()
                }
                ventana.show()
            }
    }

    private fun confirmar(pendiente: PersonaStore.ResumenCobro) {
        if (terminado || !confirmando || isFinishing) return

        when (val resultado = PersonaStore.confirmarCobro(pendiente)) {
            is PersonaStore.ResultadoCobro.Confirmado -> {
                val recibo = resultado.comprobante
                terminado = true
                comprobanteId = recibo.id
                textoComprobante = "Cobro simulado registrado\n${recibo.concepto.etiqueta} · ${dinero(recibo.importeCentavos)}\n" +
                    "Comprobante demo: ${recibo.id}\nNuevo vencimiento: ${fecha(recibo.nuevoVencimiento)}\n" +
                    "Sin validez fiscal ni como credencial."
                mensaje.text = "Operación finalizada. No se volverá a cobrar desde esta pantalla."
                setResult(RESULT_OK)
            }

            is PersonaStore.ResultadoCobro.Fallo -> {
                mensaje.text = explicar(resultado.error)
                if (resultado.error == PersonaStore.ErrorCobro.PERSONA_NO_ENCONTRADA) {
                    rechazarPersona()
                }
                if (resultado.error == PersonaStore.ErrorCobro.OPERACION_INVALIDA) {
                    operacionId = UUID.randomUUID().toString()
                }
            }
            else -> mensaje.text = "No se pudo confirmar el cobro. No se registró una operación."
        }
    }

    private fun describir(valor: PersonaStore.ResumenCobro): String =
        "${valor.concepto.etiqueta} · ${valor.concepto.dias} días calendario\n" +
            "Importe total: ${dinero(valor.importeCentavos)}\n" +
            "Vencimiento anterior: ${fecha(valor.persona.vencimientoNullable)}\n" +
            "Base de cálculo: ${fecha(valor.baseVencimiento)}\nNuevo vencimiento: ${fecha(valor.nuevoVencimiento)}\n" +
            if (valor.esRenovacion) "Renovación anticipada: la vigencia está activa (incluye hoy). Se extiende desde su vencimiento, no desde hoy."
            else "Se calcula desde el día local de hoy."

    private fun explicar(error: PersonaStore.ErrorCobro): String = when (error) {
        PersonaStore.ErrorCobro.IMPORTE_INVALIDO -> "Importe inválido: usa un valor positivo sin miles y con hasta 2 decimales."
        PersonaStore.ErrorCobro.PERSONA_NO_ENCONTRADA -> "La persona ya no existe. No se realizó el cobro."
        PersonaStore.ErrorCobro.TARIFAS_SIN_CONFIGURAR -> "Cobro bloqueado: configura y guarda ambas tarifas."
        PersonaStore.ErrorCobro.TIPO_INVALIDO -> "Cambió el tipo de persona. No se cobró; revisa el nuevo concepto y confirma otra vez."
        PersonaStore.ErrorCobro.RESUMEN_OBSOLETO -> "Los datos, tarifas o fechas cambiaron. No se cobró; revisa el nuevo resumen y confirma otra vez."
        PersonaStore.ErrorCobro.OPERACION_INVALIDA -> "Identificador de operación inválido o ya utilizado. No se realizó un nuevo cobro."
    }

    private fun rechazarPersona() {
        Toast.makeText(this, "No se puede cobrar: identificador inválido o persona inexistente.", Toast.LENGTH_LONG).show()
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun comprobanteFinalizado(): PersonaStore.Comprobante? =
        if (!terminado || personaId == null) null
        else comprobanteId?.let { PersonaStore.getComprobanteById(it) }
            ?.takeIf { it.personaId == personaId }

    private fun decimal(centavos: Long): String = BigDecimal.valueOf(centavos, 2).toPlainString()
    private fun dinero(centavos: Long): String = "ARS ${decimal(centavos).replace('.', ',')}"
    private fun fecha(valor: Long?): String = valor?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-AR")).format(Date(it))
    } ?: "Sin fecha"
}
