package com.example.clubdeportivo.data.reservas

/** Catálogo de ejemplo: los mismos horarios se ofrecen en cada día habilitado. */
object ReservationCatalog {
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val classes = linkedMapOf(
        "Funcional" to listOf("09:00", "18:00", "19:30"),
        "Musculación" to listOf("08:00", "12:00", "20:00"),
        "Spinning" to listOf("07:00", "17:00", "21:00"),
    )

    fun isValid(className: String?, day: String?, time: String?): Boolean =
        day in days && classes[className]?.contains(time) == true
}
