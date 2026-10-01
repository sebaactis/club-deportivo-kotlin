package com.example.clubdeportivo

import com.example.clubdeportivo.model.reservas.ReservationSelection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationSelectionTest {
    @Test
    fun requiredClassAndTimeMustBeSelected() {
        assertFalse(ReservationSelection().hasRequiredChoices())
        assertFalse(ReservationSelection(className = "Yoga").hasRequiredChoices())
        assertFalse(ReservationSelection(time = "09:00").hasRequiredChoices())
        assertTrue(ReservationSelection(className = "Yoga", time = "09:00").hasRequiredChoices())
    }

    @Test
    fun summaryIncludesSelectedClassDateAndTime() {
        val selection = ReservationSelection(className = "Yoga", date = "Monday", time = "09:00")

        assertEquals("Yoga - Monday - 09:00", selection.summary())
    }

    @Test
    fun summaryIsUnavailableUntilRequiredChoicesAreSelected() {
        assertNull(ReservationSelection(className = "Yoga").summary())
    }
}
