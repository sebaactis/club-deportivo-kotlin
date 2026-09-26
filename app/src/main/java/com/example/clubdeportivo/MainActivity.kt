package com.example.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSocio = findViewById<LinearLayout>(R.id.btnSocio)
        val btnProfesor = findViewById<LinearLayout>(R.id.btnProfesor)

        btnSocio.setOnClickListener {
            navigateToLogin("SOCIO")
        }

        btnProfesor.setOnClickListener {
            navigateToLogin("PROFESOR")
        }
    }

    private fun navigateToLogin(userType: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("USER_TYPE", userType)
        startActivity(intent)
    }
}