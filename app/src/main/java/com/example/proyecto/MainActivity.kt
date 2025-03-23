package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBienbenido.setOnClickListener() {
            startActivity(Intent(this, Iniciar_sesion::class.java))
        }
        binding.buttonRegistrarse.setOnClickListener() {
            startActivity(Intent(this, Registrarse::class.java))
        }
    }
}