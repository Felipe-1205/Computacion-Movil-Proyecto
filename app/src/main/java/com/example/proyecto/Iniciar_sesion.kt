package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityIniciarSesionBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Iniciar_sesion : AppCompatActivity() {

    private lateinit var binding: ActivityIniciarSesionBinding
    private lateinit var mAuth: FirebaseAuth
    private val TAG = "lOGIN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.iniciarSesion.setOnClickListener() {
            sigin()
        }
    }
    private fun sigin() {
        mAuth.signInWithEmailAndPassword(
            binding.correo.text.toString().trim(),
            binding.contraseA.text.toString().trim()
        ).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInWithEmail:success")
                val user = mAuth.currentUser
                val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putString("USER_ID", user?.uid)
                editor.apply()
                updateUI(user)
            } else {
                Log.w(TAG, "signInWithEmail:failure", task.exception)
                Toast.makeText(
                    this@Iniciar_sesion, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
                updateUI(null)
            }
        })
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val userId = currentUser.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user: User? = dataSnapshot.getValue(User::class.java)
                        val roll = user?.roll
                        if (roll==1) {
                            val intent = Intent(baseContext, Main_encargado::class.java)
                            startActivity(intent)
                        } else if (roll==0) {
                            val intent = Intent(baseContext, Main_paciente::class.java)
                            startActivity(intent)
                        } else {

                        }
                        finish()  // Opcional: para cerrar la actividad actual
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar posibles errores
                }
            })
        } else {
            binding.correo.text = null
            binding.contraseA.text = null
        }
    }


}