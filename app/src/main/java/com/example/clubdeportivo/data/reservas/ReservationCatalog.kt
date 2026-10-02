package com.example.clubdeportivo.data.reservas

object ReservationCatalog {
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val classes = linkedMapOf(
        "Funcional" to listOf("09:00", "18:00", "19:30"),
        "Musculación" to listOf("08:00", "12:00", "20:00"),
        "Spinning" to listOf("07:00", "17:00", "21:00"),
    )

    data class DemoDetails(
        val teacher: String,
        val initials: String,
        val room: String,
        val durationMinutes: Int,
        val capacitySample: String,
    )

    val demoDetails = mapOf(
        "Funcional" to DemoDetails("Lucía Fernández", "LF", "Salón 2", 60, "13 de 20"),
        "Musculación" to DemoDetails("Diego Sosa", "DS", "Sala general", 60, "8 de 25"),
        "Spinning" to DemoDetails("Ana Ruiz", "AR", "Sala spin", 45, "10 de 15"),
    )

    fun isValid(className: String?, day: String?, time: String?): Boolean =
        day in days && classes[className]?.contains(time) == true
}
