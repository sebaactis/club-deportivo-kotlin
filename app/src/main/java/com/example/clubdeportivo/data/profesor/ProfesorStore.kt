package com.example.clubdeportivo.data.profesor

object ProfesorStore {
    const val CLASE_PROXIMA_ID = "funcional-2025-08-26-1800"

    data class Socio(val id: String, val nombre: String)

    data class Clase(
        val id: String,
        val deporte: String,
        val fecha: String,
        val horario: String,
        val lugar: String,
        val socios: List<Socio>
    )

    private val sociosEjemplo = listOf(
        Socio("socio-1042", "Martín Gómez"),
        Socio("socio-1043", "Ana Pérez"),
        Socio("socio-1044", "Diego López"),
        Socio("socio-1045", "Carla Ruiz"),
        Socio("socio-1046", "Pablo Torres"),
        Socio("socio-1047", "Sofía Díaz"),
        Socio("socio-1048", "Lucas Romero"),
        Socio("socio-1049", "Valentina Silva"),
        Socio("socio-1050", "Nicolás Castro"),
        Socio("socio-1051", "Camila Medina"),
        Socio("socio-1052", "Tomás Herrera"),
        Socio("socio-1053", "Julieta Molina"),
        Socio("socio-1054", "Federico Vega"),
        Socio("socio-1055", "Florencia Ríos"),
        Socio("socio-1056", "Mateo Acosta"),
        Socio("socio-1057", "Luciana Moreno"),
        Socio("socio-1058", "Joaquín Suárez"),
        Socio("socio-1059", "Daniela Ortiz")
    )

    val clases = listOf(
        Clase("funcional-2025-08-26-0900", "Entrenamiento Funcional",
            "Martes 26/08/2025", "09:00", "Salón 2", sociosEjemplo.take(6)),
        Clase(CLASE_PROXIMA_ID, "Entrenamiento Funcional",
            "Martes 26/08/2025", "18:00", "Salón 2", sociosEjemplo),
        Clase("funcional-2025-08-26-1930", "Entrenamiento Funcional",
            "Martes 26/08/2025", "19:30", "Salón 2", emptyList())
    )

    // La clave es clase + socio, nunca la posición en la lista.
    // Las marcas duran solamente mientras vive el proceso de la aplicación.
    private val presentes = mutableMapOf<String, MutableSet<String>>()

    fun buscarClase(id: String?): Clase? = clases.find { it.id == id }

    fun estaPresente(claseId: String, socioId: String): Boolean {
        val clase = buscarClase(claseId) ?: return false
        if (clase.socios.none { it.id == socioId }) return false
        return presentes[claseId]?.contains(socioId) == true
    }

    fun marcarPresente(claseId: String, socioId: String, presente: Boolean): Boolean {
        val clase = buscarClase(claseId) ?: return false
        if (clase.socios.none { it.id == socioId }) return false
        if (presente) {
            presentes.getOrPut(claseId) { mutableSetOf() }.add(socioId)
        } else {
            presentes[claseId]?.remove(socioId)
        }
        return true
    }

    fun contarPresentes(claseId: String): Int {
        val clase = buscarClase(claseId) ?: return 0
        return clase.socios.count { estaPresente(claseId, it.id) }
    }
}
