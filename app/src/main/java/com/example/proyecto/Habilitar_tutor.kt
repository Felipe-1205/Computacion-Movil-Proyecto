package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyecto.databinding.ActivityHabilitarTutorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Habilitar_tutor : AppCompatActivity() {

    private lateinit var binding: ActivityHabilitarTutorBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private  var correousuario: String = ""
    private var comprovacion: Int =0
    private lateinit var uidUsuario: String
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabilitarTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        binding.editTextTextEmailAddress.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                // Llamar a la función de búsqueda
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.guardar2.setOnClickListener() {
            if(comprovacion==0){
                finish()
            } else {
                if(binding.checkBox7.isChecked){
                    uidUsuario = intent.getStringExtra("USER_UID") ?: ""
                    val userRef = database.getReference("users/${uidUsuario}")
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                var validacion: Int =0
                                val user = dataSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    for (correo in user.usuarios){
                                        if (correo==binding.editTextTextEmailAddress.text.toString()) {
                                            validacion=1
                                        }
                                    }
                                    if (validacion==0){
                                        user.usuarios.add(binding.editTextTextEmailAddress.text.toString())
                                        userRef.setValue(user)
                                        val usersRef = database.getReference("users")
                                        val query = usersRef.orderByChild("email").equalTo(binding.editTextTextEmailAddress.text.toString())

                                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    // Se encontró al menos un usuario con el correo electrónico dado
                                                    for (userSnapshot in dataSnapshot.children) {
                                                        val user2 = userSnapshot.getValue(User::class.java)
                                                        if (user2 != null) {
                                                            user2.usuarios.add(correousuario)
                                                            usersRef.child(userSnapshot.key!!).setValue(user2)
                                                        }

                                                    }
                                                } else {
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                            }
                                        })
                                    }
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                } else {
                    uidUsuario = intent.getStringExtra("USER_UID") ?: ""
                    val userRef = database.getReference("users/${uidUsuario}")
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                var validacion: Int =0
                                val user = dataSnapshot.getValue(User::class.java)
                                if (user != null) {
                                    for (correo in user.usuarios){
                                        if (correo==binding.editTextTextEmailAddress.text.toString()) {
                                            validacion=1
                                        }
                                    }
                                    if (validacion==1){
                                        if(binding.editTextTextEmailAddress.text.toString()!=user.PersonaAsociada){
                                            user.usuarios.removeIf{ it==binding.editTextTextEmailAddress.text.toString() }
                                            userRef.setValue(user)
                                            val usersRef = database.getReference("users")
                                            val query = usersRef.orderByChild("email").equalTo(binding.editTextTextEmailAddress.text.toString())

                                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        // Se encontró al menos un usuario con el correo electrónico dado
                                                        for (userSnapshot in dataSnapshot.children) {
                                                            val user2 = userSnapshot.getValue(User::class.java)
                                                            if (user2 != null) {
                                                                user2.usuarios.removeIf{ it==correousuario}
                                                                usersRef.child(userSnapshot.key!!).setValue(user2)
                                                            }

                                                        }
                                                    } else {
                                                    }
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }
                finish()
            }
        }
    }

    private fun performSearch() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(User::class.java)
                if (user != null && user.email == binding.editTextTextEmailAddress.text.toString()) {
                    if(user.roll==1){
                        comprovacion=1
                        correousuario = intent.getStringExtra("USER_EMAIL") ?: ""
                        binding.textView20.text = user.email
                        binding.nombre.text = user.name
                        for (correo in user.usuarios){
                            if (correo==correousuario) {
                                binding.checkBox7.isChecked = true
                            }
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
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
