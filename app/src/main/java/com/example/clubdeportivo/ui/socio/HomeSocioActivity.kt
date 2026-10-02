package com.example.clubdeportivo.ui.socio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.data.reservas.ReservationStore
import com.example.clubdeportivo.ui.acceso.DemoAccess
import com.example.clubdeportivo.ui.inicio.MainActivity
import com.example.clubdeportivo.ui.nutricion.NutricionActivity
import com.example.clubdeportivo.ui.reservas.ReservationActivity

class HomeSocioActivity : AppCompatActivity() {
    private var profileDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_socio)
        instalarInsets()

        findViewById<View>(R.id.tvAvatar).setOnClickListener {
            mostrarPerfil()
        }
        findViewById<View>(R.id.btnPerfil).setOnClickListener {
            mostrarPerfil()
        }
        findViewById<View>(R.id.btnVerCredencial).setOnClickListener {
            startActivity(Intent(this, CredencialActivity::class.java))
        }
        findViewById<View>(R.id.btnCredencial).setOnClickListener {
            startActivity(Intent(this, CredencialActivity::class.java))
        }
        findViewById<View>(R.id.btnReservarClase).setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
        }
        findViewById<View>(R.id.btnMisClases).setOnClickListener {
            abrirMisClases()
        }
        findViewById<View>(R.id.btnClases).setOnClickListener {
            abrirMisClases()
        }
        findViewById<View>(R.id.btnProximaClase).setOnClickListener {
            abrirMisClases()
        }
        findViewById<View>(R.id.btnMiRutina).setOnClickListener {
            startActivity(Intent(this, MiRutinaActivity::class.java))
        }
        findViewById<View>(R.id.btnNutricion).setOnClickListener {
            startActivity(Intent(this, NutricionActivity::class.java))
        }
        findViewById<View>(R.id.btnInicio).apply {
            isSelected = true
            setOnClickListener {
                actualizarInicio()
                findViewById<ScrollView>(R.id.socioHomeScroll).smoothScrollTo(0, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarInicio()
    }

    override fun onPause() {
        profileDialog?.dismiss()
        profileDialog = null
        super.onPause()
    }

    private fun perfilSocio(): DemoAccess.Profile? {
        val profile = DemoAccess.currentProfile
        if (profile == null || profile.role != DemoAccess.Role.SOCIO) {
            if (!isFinishing) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            return null
        }
        return profile
    }

    private fun actualizarInicio() {
        val profile = perfilSocio() ?: return
        findViewById<TextView>(R.id.tvUserName).text = profile.name
        var initials = ""
        for (word in profile.name.split(" ")) {
            if (word.isNotBlank() && initials.length < 2) {
                initials += word.first().uppercaseChar()
            }
        }
        findViewById<TextView>(R.id.tvAvatar).text = initials

        val quota = findViewById<TextView>(R.id.tvCuota)
        val member = findViewById<TextView>(R.id.tvSocioDetalle)
        if (profile.id == "demo-member-1") {
            quota.setText(R.string.socio_home_demo_paid)
            quota.setTextColor(getColor(R.color.access_success))
            member.text = getString(R.string.socio_home_demo_member, profile.dni)
        } else {
            quota.setText(R.string.socio_home_no_quota)
            quota.setTextColor(getColor(R.color.access_text_secondary))
            member.text = getString(R.string.socio_home_unassigned_member, profile.dni)
        }

        actualizarReservas()
    }

    private fun actualizarReservas() {
        val reservas = ReservationStore.getAll()
        val cantidad = reservas.size
        findViewById<TextView>(R.id.tvMisClasesCount).text = if (cantidad == 1) {
            getString(R.string.socio_home_one_booking)
        } else {
            getString(R.string.socio_home_booking_count, cantidad)
        }

        val reserva = reservas.firstOrNull()
        val nombre = findViewById<TextView>(R.id.tvReservaNombre)
        val horario = findViewById<TextView>(R.id.tvReservaHorario)
        val detalle = findViewById<TextView>(R.id.tvReservaDetalle)
        if (reserva == null) {
            nombre.setText(R.string.socio_home_empty)
            horario.setText(R.string.socio_home_empty_help)
            detalle.setText(R.string.socio_home_open_classes)
        } else {
            nombre.text = reserva.className
            horario.text = getString(R.string.socio_home_booking_time, reserva.day, reserva.time)
            detalle.setText(R.string.socio_home_first_saved)
        }
    }

    private fun abrirMisClases() {
        startActivity(Intent(this, MisClasesActivity::class.java))
    }

    private fun mostrarPerfil() {
        val profile = perfilSocio() ?: return
        profileDialog?.dismiss()
        profileDialog = AlertDialog.Builder(this)
            .setTitle(R.string.socio_home_profile)
            .setMessage(getString(R.string.socio_home_profile_details, profile.name, profile.dni))
            .setPositiveButton(R.string.socio_home_close, null)
            .show()
    }

    private fun instalarInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val root = findViewById<View>(R.id.socioHomeRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
}
