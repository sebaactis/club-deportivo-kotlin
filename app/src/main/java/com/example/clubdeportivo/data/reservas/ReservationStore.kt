package com.example.clubdeportivo.data.reservas

import java.util.UUID

data class BookedClass(
    val className: String,
    val day: String,
    val time: String,
    val id: String = UUID.randomUUID().toString(),
)

enum class ReservationResult(val message: String) {
    SUCCESS("Reserva guardada."),
    INVALID("Selecciona un día, una clase y un horario válidos."),
    DUPLICATE("Ya tienes una reserva para esa clase, día y horario."),
    UNKNOWN_ID("Esta reserva ya no está disponible. Vuelve a Mis clases."),
}

object ReservationStore {
    private val bookedClasses = mutableListOf<BookedClass>()

    fun add(bookedClass: BookedClass): ReservationResult {
        if (bookedClass.id.isBlank() || !isValid(bookedClass)) {
            return ReservationResult.INVALID
        }
        if (getById(bookedClass.id) != null || isDuplicate(bookedClass)) {
            return ReservationResult.DUPLICATE
        }
        bookedClasses.add(bookedClass)
        return ReservationResult.SUCCESS
    }

    fun getAll(): List<BookedClass> = bookedClasses.toList()

    fun getById(id: String): BookedClass? = bookedClasses.firstOrNull { it.id == id }

    fun update(id: String, replacement: BookedClass): ReservationResult {
        val index = bookedClasses.indexOfFirst { it.id == id }
        if (index == -1) {
            return ReservationResult.UNKNOWN_ID
        }
        if (!isValid(replacement)) {
            return ReservationResult.INVALID
        }
        if (isDuplicate(replacement, id)) {
            return ReservationResult.DUPLICATE
        }
        bookedClasses[index] = replacement.copy(id = id)
        return ReservationResult.SUCCESS
    }

    fun delete(id: String): ReservationResult {
        val index = bookedClasses.indexOfFirst { it.id == id }
        if (index == -1) {
            return ReservationResult.UNKNOWN_ID
        }
        bookedClasses.removeAt(index)
        return ReservationResult.SUCCESS
    }

    private fun isValid(booking: BookedClass): Boolean =
        ReservationCatalog.isValid(booking.className, booking.day, booking.time)

    private fun isDuplicate(booking: BookedClass, excludedId: String? = null): Boolean =
        bookedClasses.any {
            it.id != excludedId &&
                it.className == booking.className &&
                it.day == booking.day &&
                it.time == booking.time
        }
}
