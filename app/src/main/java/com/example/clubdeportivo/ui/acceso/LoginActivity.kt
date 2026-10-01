package com.example.clubdeportivo.ui.acceso

import com.example.clubdeportivo.R
import com.example.clubdeportivo.ui.profesor.HomeProfesorActivity
import com.example.clubdeportivo.ui.socio.HomeSocioActivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val userType = intent.getStringExtra("USER_TYPE") ?: "SOCIO"

        // Referencias a los elementos del layout
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val tvRoleBadge = findViewById<TextView>(R.id.tvRoleBadge)
        val etDni = findViewById<EditText>(R.id.etDni)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvTogglePassword = findViewById<TextView>(R.id.tvTogglePassword)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        // Configuración visual según el rol seleccionado
        if (userType == "PROFESOR") {
            tvRoleBadge.text = "PROFESOR"
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_blue)
            btnSubmit.setBackgroundColor(Color.parseColor("#3B82F6"))
            tvTogglePassword.setTextColor(Color.parseColor("#3B82F6"))
        } else {
            tvRoleBadge.text = "SOCIO"
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_yellow)
            btnSubmit.setBackgroundColor(Color.parseColor("#FFC82C"))
            tvTogglePassword.setTextColor(Color.parseColor("#FFC82C"))
        }

        // 1. Botón Volver
        btnBack.setOnClickListener {
            finish()
        }

        // 2. Mostrar / Ocultar Contraseña
        tvTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                tvTogglePassword.text = "Mostrar"
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                tvTogglePassword.text = "Ocultar"
            }
            isPasswordVisible = !isPasswordVisible
            etPassword.setSelection(etPassword.text.length)
        }

        // 3. Botón Ingresar (Navegación dinámica según el rol)
        btnSubmit.setOnClickListener {
            val dni = etDni.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (dni.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa tu DNI y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                if (userType == "SOCIO") {
                    val intent = Intent(this, HomeSocioActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, HomeProfesorActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}