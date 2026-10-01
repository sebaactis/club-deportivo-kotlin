package com.example.clubdeportivo.data.personas

import java.math.BigDecimal
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

/** Registro demo independiente de clases, login y credenciales; vive solo en memoria. */
object PersonaStore {
    enum class Tipo(val etiqueta: String) { SOCIO("Socio"), NO_SOCIO("No Socio") }
    enum class Estado(val etiqueta: String) {
        ACTIVA("Activa"), VENCIDA("Vencida"), SIN_PAGAR("Sin pagar")
    }

    data class Persona(
        val id: String,
        val nombre: String,
        val apellido: String,
        val dni: String,
        val tipo: Tipo,
        val vencimientoNullable: Long?
    ) {
        // Compara días locales, no horas: una cuota que vence hoy sigue activa.
        fun estado(hoy: Long = System.currentTimeMillis()): Estado = when {
            vencimientoNullable == null -> Estado.SIN_PAGAR
            dia(vencimientoNullable) < dia(hoy) -> Estado.VENCIDA
            else -> Estado.ACTIVA
        }
    }

    enum class Error { NOMBRE_VACIO, APELLIDO_VACIO, DNI_INVALIDO, DNI_DUPLICADO, NO_ENCONTRADA }
    sealed class Resultado {
        data class Guardada(val persona: Persona) : Resultado()
        data class Eliminada(val persona: Persona) : Resultado()
        data class Fallo(val error: Error) : Resultado()
    }

    data class Tarifas(val mensualCentavos: Long, val diariaCentavos: Long)
    enum class Concepto(val etiqueta: String, val dias: Int, val tipo: Tipo) {
        CUOTA_MENSUAL("Cuota mensual", 30, Tipo.SOCIO),
        PASE_DIARIO("Pase diario", 1, Tipo.NO_SOCIO)
    }
    enum class ErrorCobro {
        IMPORTE_INVALIDO, PERSONA_NO_ENCONTRADA, TARIFAS_SIN_CONFIGURAR,
        TIPO_INVALIDO, RESUMEN_OBSOLETO, OPERACION_INVALIDA
    }

    /** Valores históricos inmutables; las fechas son milisegundos y el importe es en centavos. */
    @ConsistentCopyVisibility
    data class ResumenCobro internal constructor(
        val operacionId: String,
        val persona: Persona,
        val concepto: Concepto,
        val tarifas: Tarifas,
        val importeCentavos: Long,
        val diaLocal: Long,
        val zonaHoraria: String,
        val baseVencimiento: Long,
        val nuevoVencimiento: Long,
        val esRenovacion: Boolean
    )

    data class Comprobante(
        val id: String,
        val operacionId: String,
        val personaId: String,
        val nombre: String,
        val apellido: String,
        val dni: String,
        val tipo: Tipo,
        val concepto: Concepto,
        val importeCentavos: Long,
        val fechaHora: Long,
        val vencimientoAnterior: Long?,
        val nuevoVencimiento: Long,
        val moneda: String = "ARS"
    )

    sealed class ResultadoCobro {
        data class TarifasGuardadas(val tarifas: Tarifas) : ResultadoCobro()
        data class Resumen(val resumen: ResumenCobro) : ResultadoCobro()
        data class Confirmado(val comprobante: Comprobante) : ResultadoCobro()
        data class Fallo(val error: ErrorCobro) : ResultadoCobro()
    }

    private var tarifasActuales: Tarifas? = null
    private val comprobantes = mutableListOf<Comprobante>()
    private val operaciones = mutableMapOf<String, Pair<ResumenCobro, Comprobante>>()

    fun getTarifas(): Tarifas? = tarifasActuales

    /** Sin miles ni redondeo; admite espacios exteriores y coma o punto decimal. */
    fun parsearCentavos(entrada: String): Long? {
        val texto = entrada.trim()
        if (!texto.matches(Regex("[0-9]+([.,][0-9]{1,2})?"))) return null
        return try {
            BigDecimal(texto.replace(',', '.')).movePointRight(2).longValueExact()
                .takeIf { it > 0 }
        } catch (_: ArithmeticException) {
            null
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun configurarTarifas(mensual: String, diaria: String): ResultadoCobro {
        val mensualCentavos = parsearCentavos(mensual)
            ?: return ResultadoCobro.Fallo(ErrorCobro.IMPORTE_INVALIDO)
        val diariaCentavos = parsearCentavos(diaria)
            ?: return ResultadoCobro.Fallo(ErrorCobro.IMPORTE_INVALIDO)
        return configurarTarifas(mensualCentavos, diariaCentavos)
    }

    fun configurarTarifas(mensualCentavos: Long, diariaCentavos: Long): ResultadoCobro {
        if (mensualCentavos <= 0 || diariaCentavos <= 0) {
            return ResultadoCobro.Fallo(ErrorCobro.IMPORTE_INVALIDO)
        }
        val nuevas = Tarifas(mensualCentavos, diariaCentavos)
        tarifasActuales = nuevas
        return ResultadoCobro.TarifasGuardadas(nuevas)
    }

    fun previsualizarCobro(
        personaId: String,
        concepto: Concepto,
        operacionId: String = UUID.randomUUID().toString()
    ): ResultadoCobro {
        if (!operacionValida(operacionId) || operaciones.containsKey(operacionId)) {
            return ResultadoCobro.Fallo(ErrorCobro.OPERACION_INVALIDA)
        }
        val persona = getById(personaId)
            ?: return ResultadoCobro.Fallo(ErrorCobro.PERSONA_NO_ENCONTRADA)
        val tarifas = tarifasActuales
            ?: return ResultadoCobro.Fallo(ErrorCobro.TARIFAS_SIN_CONFIGURAR)
        if (persona.tipo != concepto.tipo) return ResultadoCobro.Fallo(ErrorCobro.TIPO_INVALIDO)
        return ResultadoCobro.Resumen(calcularResumen(persona, concepto, tarifas, operacionId))
    }

    /** Repetir exactamente un resumen aceptado devuelve su comprobante, incluso tras una baja. */
    fun confirmarCobro(resumen: ResumenCobro): ResultadoCobro {
        operaciones[resumen.operacionId]?.let { (aceptado, comprobante) ->
            return if (resumen == aceptado) ResultadoCobro.Confirmado(comprobante)
            else ResultadoCobro.Fallo(ErrorCobro.OPERACION_INVALIDA)
        }
        if (!operacionValida(resumen.operacionId)) {
            return ResultadoCobro.Fallo(ErrorCobro.OPERACION_INVALIDA)
        }
        val indice = personas.indexOfFirst { it.id == resumen.persona.id }
        if (indice < 0) return ResultadoCobro.Fallo(ErrorCobro.PERSONA_NO_ENCONTRADA)
        val persona = personas[indice]
        val tarifas = tarifasActuales
            ?: return ResultadoCobro.Fallo(ErrorCobro.TARIFAS_SIN_CONFIGURAR)
        if (persona.tipo != resumen.concepto.tipo) {
            return ResultadoCobro.Fallo(ErrorCobro.TIPO_INVALIDO)
        }
        val ahora = System.currentTimeMillis()
        val vigente = calcularResumen(persona, resumen.concepto, tarifas, resumen.operacionId, ahora)
        if (resumen != vigente) return ResultadoCobro.Fallo(ErrorCobro.RESUMEN_OBSOLETO)
        val comprobante = Comprobante(
            UUID.randomUUID().toString(), resumen.operacionId, persona.id,
            persona.nombre, persona.apellido, persona.dni, persona.tipo, resumen.concepto,
            resumen.importeCentavos, ahora, persona.vencimientoNullable, resumen.nuevoVencimiento
        )
        // Todas las validaciones preceden a la transacción en el hilo único de la UI.
        personas[indice] = persona.copy(vencimientoNullable = resumen.nuevoVencimiento)
        comprobantes.add(comprobante)
        operaciones[resumen.operacionId] = resumen to comprobante
        return ResultadoCobro.Confirmado(comprobante)
    }

    fun getComprobanteById(id: String): Comprobante? = comprobantes.firstOrNull { it.id == id }
    fun getComprobantes(): List<Comprobante> = comprobantes.asReversed().toList()
    fun getComprobantesByPersonaId(personaId: String): List<Comprobante> =
        comprobantes.asReversed().filter { it.personaId == personaId }

    private fun operacionValida(id: String): Boolean = try {
        UUID.fromString(id).toString() == id
    } catch (_: IllegalArgumentException) {
        false
    }

    private fun calcularResumen(
        persona: Persona,
        concepto: Concepto,
        tarifas: Tarifas,
        operacionId: String,
        ahora: Long = System.currentTimeMillis()
    ): ResumenCobro {
        val zona = TimeZone.getDefault()
        val hoy = inicioDia(ahora, zona)
        val anterior = persona.vencimientoNullable?.let { inicioDia(it, zona) }
        val renovacion = anterior != null && anterior >= hoy
        val base = if (renovacion) anterior!! else hoy
        val nuevo = Calendar.getInstance(zona).apply {
            timeInMillis = base
            add(Calendar.DAY_OF_YEAR, concepto.dias)
        }.timeInMillis
        val importe = when (concepto) {
            Concepto.CUOTA_MENSUAL -> tarifas.mensualCentavos
            Concepto.PASE_DIARIO -> tarifas.diariaCentavos
        }
        return ResumenCobro(operacionId, persona, concepto, tarifas, importe,
            hoy, zona.id, base, nuevo, renovacion)
    }

    private fun inicioDia(fecha: Long, zona: TimeZone): Long = Calendar.getInstance(zona).apply {
        timeInMillis = fecha
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val personas = mutableListOf(
        demo("Ana (demo)", "Ejemplo", "30123456", Tipo.SOCIO, fechaRelativa(15)),
        demo("Bruno (demo)", "Ejemplo", "31234567", Tipo.SOCIO, fechaRelativa(-1)),
        demo("Carla (demo)", "Ejemplo", "32345678", Tipo.NO_SOCIO, null)
    )

    fun getAll(): List<Persona> = personas.toList()
    fun getById(id: String): Persona? = personas.firstOrNull { it.id == id }

    /** DNI demo: solo 7–8 dígitos ASCII, sin puntos/espacios internos ni todos ceros. */
    fun dniValido(dni: String): Boolean {
        val limpio = dni.trim()
        return limpio.matches(Regex("[0-9]{7,8}")) && limpio.any { it != '0' }
    }

    fun create(nombre: String, apellido: String, dni: String, tipo: Tipo): Resultado {
        validar(nombre, apellido, dni, null)?.let { return Resultado.Fallo(it) }
        val persona = Persona(UUID.randomUUID().toString(), nombre.trim(), apellido.trim(),
            dni.trim(), tipo, null)
        personas.add(persona)
        return Resultado.Guardada(persona)
    }

    // No recibe vencimiento: editar datos personales no representa un cobro.
    fun update(id: String, nombre: String, apellido: String, dni: String, tipo: Tipo): Resultado {
        val indice = personas.indexOfFirst { it.id == id }
        if (indice < 0) return Resultado.Fallo(Error.NO_ENCONTRADA)
        validar(nombre, apellido, dni, id)?.let { return Resultado.Fallo(it) }
        val actualizada = personas[indice].copy(nombre = nombre.trim(), apellido = apellido.trim(),
            dni = dni.trim(), tipo = tipo)
        personas[indice] = actualizada
        return Resultado.Guardada(actualizada)
    }

    fun delete(id: String): Resultado {
        val persona = getById(id) ?: return Resultado.Fallo(Error.NO_ENCONTRADA)
        personas.remove(persona)
        return Resultado.Eliminada(persona)
    }

    private fun validar(nombre: String, apellido: String, dni: String, id: String?): Error? = when {
        nombre.isBlank() -> Error.NOMBRE_VACIO
        apellido.isBlank() -> Error.APELLIDO_VACIO
        !dniValido(dni) -> Error.DNI_INVALIDO
        personas.any { it.dni == dni.trim() && it.id != id } -> Error.DNI_DUPLICADO
        else -> null
    }

    private fun demo(nombre: String, apellido: String, dni: String, tipo: Tipo, fecha: Long?) =
        Persona(UUID.randomUUID().toString(), nombre, apellido, dni, tipo, fecha)

    private fun fechaRelativa(dias: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, dias)
    }.timeInMillis

    private fun dia(fecha: Long): Int = Calendar.getInstance().run {
        timeInMillis = fecha
        get(Calendar.YEAR) * 10000 + (get(Calendar.MONTH) + 1) * 100 + get(Calendar.DAY_OF_MONTH)
    }
}
