package com.example.clubdeportivo.model.reservas

data class ReservationSelection(
    val className: String? = null,
    val date: String? = null,
    val time: String? = null,
) {
    fun hasRequiredChoices(): Boolean = !className.isNullOrBlank() && !time.isNullOrBlank()

    fun summary(): String? {
        if (!hasRequiredChoices()) {
            return null
        }
        val selectedDate = date?.takeIf { it.isNotBlank() } ?: return null
        return "$className - $selectedDate - $time"
    }
}
