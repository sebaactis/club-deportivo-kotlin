package com.example.clubdeportivo.data.nutricion

data class TurnoNutricion(
    val id: Int,
    val dia: String,
    val hora: String
)

object NutricionStore {
    val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes")
    val horarios = listOf("09:00", "11:00", "16:00", "18:00")
    private val turnos = mutableListOf<TurnoNutricion>()
    private var siguienteId = 1

    enum class Resultado {
        EXITO,
        INVALIDO,
        DUPLICADO,
        NO_ENCONTRADO
    }

    fun getAll(): List<TurnoNutricion> = turnos.toList()

    fun crear(dia: String, hora: String): Resultado {
        val resultado = validar(dia, hora)
        if (resultado != Resultado.EXITO) {
            return resultado
        }

        turnos.add(TurnoNutricion(siguienteId++, dia, hora))
        return Resultado.EXITO
    }

    fun actualizar(id: Int, dia: String, hora: String): Resultado {
        val indice = turnos.indexOfFirst { it.id == id }
        if (indice == -1) {
            return Resultado.NO_ENCONTRADO
        }

        val resultado = validar(dia, hora, id)
        if (resultado != Resultado.EXITO) {
            return resultado
        }

        turnos[indice] = TurnoNutricion(id, dia, hora)
        return Resultado.EXITO
    }

    fun eliminar(id: Int): Resultado {
        val indice = turnos.indexOfFirst { it.id == id }
        if (indice == -1) {
            return Resultado.NO_ENCONTRADO
        }

        turnos.removeAt(indice)
        return Resultado.EXITO
    }

    private fun validar(dia: String, hora: String, excluirId: Int? = null): Resultado {
        if (dia !in dias || hora !in horarios) {
            return Resultado.INVALIDO
        }
        if (turnos.any { it.dia == dia && it.hora == hora && it.id != excluirId }) {
            return Resultado.DUPLICADO
        }
        return Resultado.EXITO
    }
}
