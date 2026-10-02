package com.example.clubdeportivo.ui.socio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.acceso.DemoAccess
import com.example.clubdeportivo.ui.inicio.MainActivity

class CredencialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credencial)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val root = findViewById<View>(R.id.socioCredentialRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)

        findViewById<View>(R.id.btnVolverCredencial).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val profile = DemoAccess.currentProfile
        if (profile == null || profile.role != DemoAccess.Role.SOCIO) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        findViewById<TextView>(R.id.tvCredencialNombre).text = profile.name
        findViewById<TextView>(R.id.tvCredencialDni).text =
            getString(R.string.socio_home_credential_dni, profile.dni)

        val numero = findViewById<TextView>(R.id.tvCredencialNumero)
        val cuota = findViewById<TextView>(R.id.tvCredencialCuota)
        if (profile.id == "demo-member-1") {
            numero.setText(R.string.socio_home_credential_demo_number)
            cuota.setText(R.string.socio_home_credential_demo_quota)
        } else {
            numero.setText(R.string.socio_home_credential_no_number)
            cuota.setText(R.string.socio_home_no_quota)
        }
    }
}
