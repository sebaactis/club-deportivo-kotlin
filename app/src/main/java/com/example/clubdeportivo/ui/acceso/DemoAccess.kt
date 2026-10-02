package com.example.clubdeportivo.ui.acceso

object DemoAccess {
    enum class Role { SOCIO, PROFESOR }

    data class Profile(
        val id: String,
        val name: String,
        val dni: String,
        val role: Role
    )

    private data class Account(val profile: Profile, var password: String)

    private val accounts = mutableListOf(
        Account(Profile("demo-member-1", "Martín Gómez", "30123456", Role.SOCIO), "socio123"),
        Account(Profile("demo-staff-1", "Lucía Fernández", "25225123", Role.PROFESOR), "profe123")
    )
    private var nextMemberId = 2
    var currentProfile: Profile? = null
        private set

    fun role(value: String?): Role = if (value == "PROFESOR") Role.PROFESOR else Role.SOCIO

    fun normalizeDni(value: String): String? {
        val trimmed = value.trim()
        if (!Regex("(?:[0-9]{7,8}|[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3})").matches(trimmed)) return null
        return trimmed.replace(".", "").takeIf { it.any { digit -> digit != '0' } }
    }

    fun members(): List<Profile> = accounts.map { it.profile }.filter { it.role == Role.SOCIO }

    fun find(dni: String, role: Role): Profile? {
        val canonical = normalizeDni(dni) ?: return null
        return accounts.firstOrNull { it.profile.dni == canonical && it.profile.role == role }?.profile
    }

    fun authenticate(dni: String, password: String, role: Role): Profile? {
        val profile = find(dni, role) ?: return null
        return accounts.firstOrNull { it.profile.id == profile.id && it.password == password }?.profile
    }

    fun exists(dni: String): Boolean {
        val canonical = normalizeDni(dni) ?: return false
        return accounts.any { it.profile.dni == canonical }
    }

    fun register(name: String, dni: String, password: String): Profile? {
        val canonical = normalizeDni(dni) ?: return null
        if (name.trim().isEmpty() || password.length < 6 || exists(canonical)) return null
        val profile = Profile("demo-member-${nextMemberId++}", name.trim(), canonical, Role.SOCIO)
        accounts.add(Account(profile, password))
        return profile
    }

    fun reset(dni: String, role: Role, password: String): Boolean {
        if (password.length < 6) return false
        val profile = find(dni, role) ?: return false
        accounts.first { it.profile.id == profile.id }.password = password
        return true
    }

    fun memberCard(id: String): Profile? = members().firstOrNull { it.id == id }

    fun clubCode(code: String): Profile? = if (code.trim() == "CLUB2025") {
        find("25225123", Role.PROFESOR)
    } else {
        null
    }

    fun activate(profile: Profile, role: Role): Boolean {
        if (profile.role != role || accounts.none { it.profile == profile }) return false
        currentProfile = profile
        return true
    }
}
