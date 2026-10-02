package com.example.clubdeportivo.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.acceso.AccessAppearance
import com.example.clubdeportivo.ui.acceso.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AccessAppearance.restore(savedInstanceState)
        setContentView(R.layout.activity_main)
        AccessAppearance.installInsets(this)

        findViewById<View>(R.id.btnSocio).setOnClickListener {
            navigateToLogin("SOCIO")
        }
        findViewById<View>(R.id.btnProfesor).setOnClickListener {
            navigateToLogin("PROFESOR")
        }
        findViewById<View>(R.id.btnRegister).setOnClickListener {
            navigateToLogin("SOCIO", true)
        }
    }

    override fun onResume() {
        super.onResume()
        bindAppearance()
    }

    private fun bindAppearance() {
        AccessAppearance.bind(this, changed = ::bindAppearance)
        AccessAppearance.label(this, R.id.tvSubtitle, R.string.access_choose)
        findViewById<View>(R.id.btnSocio).contentDescription =
            AccessAppearance.text(this, R.string.access_member) + ". " +
                AccessAppearance.text(this, R.string.access_member_help)
        findViewById<View>(R.id.btnProfesor).contentDescription =
            AccessAppearance.text(this, R.string.access_staff) + ". " +
                AccessAppearance.text(this, R.string.access_staff_help)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        AccessAppearance.save(outState)
        super.onSaveInstanceState(outState)
    }

    private fun navigateToLogin(userType: String, register: Boolean = false) {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                putExtra("USER_TYPE", userType)
                putExtra(LoginActivity.EXTRA_OPEN_REGISTRATION, register)
            }
        )
    }
}
