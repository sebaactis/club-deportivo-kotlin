package com.example.clubdeportivo

data class BookedClass(
    val className: String,
    val day: String,
    val time: String,
)
object ReservationStore {
    private val bookedClasses = mutableListOf<BookedClass>()
    fun add(bookedClass: BookedClass) {
        bookedClasses.add(bookedClass)
    }
    fun getAll(): List<BookedClass> = bookedClasses
}
