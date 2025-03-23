package com.example.proyecto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.proyecto.databinding.ActivityMenuPacienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Menu_paciente : AppCompatActivity() {

    private lateinit var binding: ActivityMenuPacienteBinding
    private lateinit var uidUsuario: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var paciente: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("users")


        uidUsuario = intent.getStringExtra("USER_UID") ?: ""
        val userRef = database.getReference("users/${uidUsuario}")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    paciente = dataSnapshot.getValue(User::class.java)!!
                    binding.textView13.text = paciente.name
                    binding.textView14.text = paciente.PersonaAsociada
                    binding.textView15.text = paciente.email
                    binding.textView16.text = paciente.genero
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        binding.imageView3.resumeAnimation()
        binding.seguimiento.setOnClickListener() {
            val intent = Intent(this, Seguimiento::class.java)
            intent.putExtra("USER_EMAIL", paciente.email)
            this.startActivity(intent)
        }
        binding.tutores.setOnClickListener() {
            val intent = Intent(this, Habilitar_tutor::class.java)
            intent.putExtra("USER_EMAIL", paciente.email)
            intent.putExtra("USER_UID", paciente.userID)
            this.startActivity(intent)
        }
        binding.actividades.setOnClickListener() {
            val intent = Intent(this, Activar_actividades::class.java)
            intent.putExtra("USER_UID", paciente.userID)
            this.startActivity(intent)
        }
        binding.informe.setOnClickListener() {
            val intent = Intent(this, Informe::class.java)
            intent.putExtra("USER_UID", paciente.userID)
            this.startActivity(intent)
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuLogOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}