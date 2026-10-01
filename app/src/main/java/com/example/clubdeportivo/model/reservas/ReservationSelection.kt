package com.example.clubdeportivo.model.reservas

/** Holds the user's current reservation choices without depending on Android. */
data class ReservationSelection(
    val className: String? = null,
    val date: String? = null,
    val time: String? = null,
) {
    fun hasRequiredChoices(): Boolean = !className.isNullOrBlank() && !time.isNullOrBlank()

    /** Returns a confirmation summary only when the class, date, and time are selected. */
    fun summary(): String? {
        if (!hasRequiredChoices()) return null
        val selectedDate = date?.takeIf { it.isNotBlank() } ?: return null
        return "$className - $selectedDate - $time"
    }
}
