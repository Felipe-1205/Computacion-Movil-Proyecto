package com.example.proyecto

import android.Manifest
import android.R
import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.InputType
import android.view.View
import java.util.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.proyecto.databinding.ActivityRegistrarseBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale


class Registrarse : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarseBinding
    private lateinit var mAuth: FirebaseAuth
    private val TAG = "Regis"
    val PATH_UBI = "location/"
    val PATH_ACT = "activiades/"
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef:DatabaseReference
    val PATH_USERS = "users/"
    private lateinit var editTextFecha: EditText
    private val calendar = Calendar.getInstance()
    private lateinit var uriCamera: Uri
    val PERM_CAMERA_CODE = 1000
    val PERM_GALERY_GROUP_CODE = 2000
    var outputPath: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private val mGetContentGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uriLocal ->
            if (uriLocal != null) {
                loadimage(uriLocal)
            }
        }
    private val mGetContentCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                loadimage(uriCamera)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val generos = arrayOf("hombre", "mujer", "otro")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, generos)
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.genero.adapter = adapter
        intfile()

        binding.camara.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    mGetContentCamera.launch(uriCamera)
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    Toast.makeText(this, "El permiso de Camara es necesario para usar esta actividad 😭", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERM_CAMERA_CODE)
                }
            }
        }

        binding.galeria.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    mGetContentGallery.launch("image/*")
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    Toast.makeText(this, "El permiso de Galeria es necesario para usar esta actividad 😭", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.plus(Manifest.permission.READ_MEDIA_IMAGES)
                        permissions.plus(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                    requestPermissions(permissions, PERM_GALERY_GROUP_CODE)
                }
            }
        }


        editTextFecha = binding.fecha
        binding.fecha.setOnClickListener {
            mostrarDatePicker()
        }

        binding.encargado.setOnCheckedChangeListener { _, isChecked ->
            binding.correoEncargado.isEnabled = !isChecked
            binding.correoEncargado.setText("")
        }
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()



        binding.button.setOnClickListener() {
            val isChecked2 = binding.tyc.isChecked
            if (isChecked2) {
                validarRegistro()
            }
            else {
                Toast.makeText(this, "Acepta Los Terminos Y COndiciones", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_CAMERA_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGetContentCamera.launch(uriCamera)
                } else {
                    Toast.makeText(this, "Me acaban de negar los permisos de Camara 😭", Toast.LENGTH_SHORT).show()
                }
            }
            PERM_GALERY_GROUP_CODE -> {
                for ((index, permission) in permissions.withIndex()) {
                    Log.d("PermissionResult", "$permission: ${grantResults[index]}")
                }

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGetContentGallery.launch("image/*")
                } else {
                    Toast.makeText(this, "Me acaban de negar los permisos de Galeria 😭", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun loadimage(uriLocal: Uri) {
        try {
            binding.preview.removeAllViews()
            val imageStream: InputStream? = contentResolver.openInputStream(uriLocal)
            val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            imageView.setImageBitmap(selectedImage)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true
            binding.preview.addView(imageView)
            outputPath=uriLocal
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
    private fun intfile() {
        val file = File(filesDir, "picFromCamera")
        uriCamera = FileProvider.getUriForFile(this, "com.example.proyecto.fileprovider", file)
    }
    private fun regis() {
        mAuth.createUserWithEmailAndPassword(
            binding.correo.text.toString().trim(),
            binding.ContraseA.text.toString().trim()
        ).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                val user = mAuth.currentUser
                if (user != null) {
                    // Actualizar la información del usuario
                    val upcrb = UserProfileChangeRequest.Builder()
                    upcrb.setDisplayName("${binding.name.text}")
                    upcrb.setPhotoUri(Uri.parse("path/to/pic"))
                    user.updateProfile(upcrb.build())
                    val preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.putString("USER_ID", user.uid)
                    editor.apply()
                    // Crear un objeto User con los datos del usuario
                    val myUser = User()
                    myUser.name = "${binding.name.text}"
                    myUser.email = binding.correo.text.toString()
                    myUser.roll = if (binding.encargado.isChecked) 1 else 0
                    myUser.genero = binding.genero.selectedItem.toString()
                    myUser.PersonaAsociada = binding.correoEncargado.text.toString()
                    myUser.userID = mAuth.uid


                    if(binding.encargado.isChecked) {
                    } else {
                        myUser.usuarios.add(binding.correoEncargado.text.toString())
                    }
                    val storageRef = storage.reference.child("images/${mAuth.uid}/profile.jpg")
                    val uploadTask = outputPath?.let { storageRef.putFile(it) }
                    if (uploadTask != null) {
                        uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                            }
                            storageRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                            } else {
                                Toast.makeText(this@Registrarse, "Error al obtener la URL de descarga", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    myRef = database.getReference("$PATH_USERS${user.uid}")
                    myRef.setValue(myUser)


                    val myUbi = Ubicacion()
                    myUbi.latitud = 0.0
                    myUbi.longitud = 0.0
                    myUbi.email = binding.correo.text.toString()
                    myRef = database.getReference("$PATH_UBI${user.uid}")
                    myRef.setValue(myUbi)

                    if(binding.encargado.isChecked) {
                    } else {
                        val myActi = Actividades()
                        myActi.actividad1
                        myActi.actividad2
                        myActi.actividad3
                        myActi.actividad4
                        myActi.actividad5
                        myActi.actividad6
                        myRef = database.getReference("$PATH_ACT${user.uid}")
                        myRef.setValue(myActi)
                    }


                    if(binding.encargado.isChecked) {
                    } else {
                        val databaseRef = FirebaseDatabase.getInstance().reference.child(PATH_USERS)
                        val query = databaseRef.orderByChild("email").equalTo(binding.correoEncargado.text.toString())
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (userSnapshot in snapshot.children) {
                                    val uid = userSnapshot.key


                                    val userRef = database.getReference("users/${uid}")
                                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                val user = dataSnapshot.getValue(User::class.java)
                                                if (user != null) {
                                                    user.usuarios.add(binding.correo.text.toString())
                                                    userRef.setValue(user)
                                                }
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {
                                        }
                                    })

                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@Registrarse, "Error en la consulta", Toast.LENGTH_SHORT).show()
                            }
                        }
                        query.addListenerForSingleValueEvent(valueEventListener)
                    }

                    updateUI(user)

                }
            }
            if (!task.isSuccessful) {
                Toast.makeText(this@Registrarse, "error registrando usuario", Toast.LENGTH_SHORT)
                    .show()
                task.exception?.message?.let { Log.e(TAG, it) }
            }
        })
    }

    private fun mostrarDatePicker() {
        val fechaMaxima = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -5)
        val fechaMinima = calendar.timeInMillis
        editTextFecha.inputType = InputType.TYPE_NULL
        editTextFecha.setOnClickListener(null)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
                editTextFecha.setText(fechaSeleccionada)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = fechaMaxima
        datePickerDialog.datePicker.minDate = fechaMinima

        datePickerDialog.show()
    }
    private fun validarRegistro() {
        val correo = binding.correo.text.toString().trim()
        val nombre = binding.name.text.toString().trim()
        val contrasena = binding.ContraseA.text.toString().trim()
        val correoEncargado = binding.correoEncargado.text.toString().trim()
        val fechaNacimiento = binding.fecha.text.toString().trim()
        var numero: Int=1

        if (fechaNacimiento.isEmpty()) {
            Toast.makeText(this, "Ingrese Una fecha", Toast.LENGTH_SHORT).show()
            binding.fecha.setText("")
            return
        }

        if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingrese Un correo Valido", Toast.LENGTH_SHORT).show()
            binding.correo.setText("")
            return
        }

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese Un nombre Valido", Toast.LENGTH_SHORT).show()
            binding.name.setText("")
            return
        }

        if (contrasena.length < 6) {
            Toast.makeText(this, "Ingrese Una contraseña Valida", Toast.LENGTH_SHORT).show()
            binding.ContraseA.setText("")
            return
        }

        if(binding.encargado.isChecked) {
            regis()
        } else {
            if (correoEncargado.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correoEncargado).matches()) {
                Toast.makeText(this, "Ingrese Un correo de un asociado Valido", Toast.LENGTH_SHORT).show()
                binding.correoEncargado.setText("")
                return
            }
            val databaseRef = FirebaseDatabase.getInstance().reference.child(PATH_USERS)// Listener para obtener los datos de la base de datos
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Itera sobre los usuarios en la base de datos
                    for (userSnapshot in snapshot.children) {
                        // Obtiene el correo electrónico del usuario actual
                        val userEmail = userSnapshot.child("email").getValue(String::class.java)

                        // Compara el correo electrónico actual con el correoEncargado
                        if (userEmail == correoEncargado) {
                            regis()
                            numero=0
                        }
                    }
                    if (numero!=0){
                        Toast.makeText(this@Registrarse, "Correo de asociado no encontrado", Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Registrarse, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
            databaseRef.addListenerForSingleValueEvent(valueEventListener)
        }
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            if (binding.encargado.isChecked) {
                Log.e("Activado", "currentUser no es nulo")
                val intent = Intent(baseContext, Main_encargado::class.java)
                startActivity(intent)
            } else {
                Log.e("Desactivado", "checkbox no está marcado")
                val intent = Intent(baseContext, Main_paciente::class.java)
                startActivity(intent)
            }
        } else {
            binding.correo.setText("")
            binding.ContraseA.setText("")
        }
    }
}