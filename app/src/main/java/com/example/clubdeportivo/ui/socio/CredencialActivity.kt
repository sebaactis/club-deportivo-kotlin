package com.example.clubdeportivo.ui.socio

import com.example.clubdeportivo.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class CredencialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credencial)

        findViewById<AppCompatButton>(R.id.btnVolverCredencial).setOnClickListener {
            finish()
        }
    }
}
