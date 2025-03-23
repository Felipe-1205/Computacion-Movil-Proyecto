package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityActivarActividadesBinding
import com.example.proyecto.databinding.ActivityMenuPacienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Activar_actividades : AppCompatActivity() {

    private lateinit var binding: ActivityActivarActividadesBinding
    private lateinit var uidUsuario: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var paciente: Actividades



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivarActividadesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        uidUsuario = intent.getStringExtra("USER_UID") ?: ""
        val userRef = database.getReference("activiades/$uidUsuario")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    paciente = dataSnapshot.getValue(Actividades::class.java)!!
                    if (paciente.actividad1==true){
                        binding.checkBox.isChecked =true
                    }
                    if (paciente.actividad2==true){
                        binding.checkBox2.isChecked =true
                    }
                    if (paciente.actividad3==true){
                        binding.checkBox3.isChecked =true
                    }
                    if (paciente.actividad4==true){
                        binding.checkBox4.isChecked =true
                    }
                    if (paciente.actividad5==true){
                        binding.checkBox5.isChecked =true
                    }
                    if (paciente.actividad6==true){
                        binding.checkBox6.isChecked =true
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        binding.guardar.setOnClickListener() {

            val myActi = Actividades()
            myActi.actividad1=binding.checkBox.isChecked
            myActi.actividad2=binding.checkBox2.isChecked
            myActi.actividad3=binding.checkBox3.isChecked
            myActi.actividad4=binding.checkBox4.isChecked
            myActi.actividad5=binding.checkBox5.isChecked
            myActi.actividad6=binding.checkBox6.isChecked
            myRef = database.getReference("activiades/$uidUsuario")
            myRef.setValue(myActi)


            finish()
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