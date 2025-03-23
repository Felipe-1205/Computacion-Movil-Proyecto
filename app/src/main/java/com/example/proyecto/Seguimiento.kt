package com.example.proyecto

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.proyecto.databinding.ActivitySeguimientoBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Seguimiento : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensorListener: SensorEventListener
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var mMap: GoogleMap
    private var doctor: String = "0,0"
    private var paciente: String = "0,0"
    private lateinit var binding: ActivitySeguimientoBinding
    private val markers = HashMap<String, Marker?>()
    var poly:Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeguimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 500) {
                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@Seguimiento, R.raw.night))
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@Seguimiento, R.raw.light))
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }
    }


    private fun drawRoute(routeResponse: RouteResponse?) {
        poly?.remove()
        poly = null
        val polyIineOptions = PolylineOptions()
        polyIineOptions.color(Color.RED)
        polyIineOptions.width(10f)
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyIineOptions.add(LatLng(it[1],it[0]))
        }
        runOnUiThread{
            poly =mMap.addPolyline(polyIineOptions)
        }
    }
    private fun createRoute(){
        CoroutineScope(Dispatchers.IO).launch{
            val call = getRetrofit().create(DirectionsApi::class.java) .getRoute("5b3ce3597851110001cf6248ff05090905a047a191b86924c42fcc9f",doctor,paciente)
            if (call.isSuccessful){
                drawRoute(call.body())
                Log.i("seguimiento","fuinciona")
            } else {
                Log.i("seguimiento","fallo")
            }
        }
    }

    fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            . baseUrl("https://api.openrouteservice.org/")
            . addConverterFactory(GsonConverterFactory.create())
            .build()
    }



    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            lightSensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightSensorListener)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isZoomControlsEnabled = true

        val autenticado = ContextCompat.getDrawable(this, R.drawable.baseline_medical_services_24)
        val markerautenticado = autenticado?.let { vectorToBitmap(it) }
        val markerOptions1 = MarkerOptions()
            .position(LatLng(0.0, 0.0))
            .icon(markerautenticado?.let { BitmapDescriptorFactory.fromBitmap(it) })
        val customMarker1 = mMap.addMarker(markerOptions1)
        markers["autenticado"] = customMarker1


        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val userRef = database.getReference("location/${currentUser.uid}")

            // Escucha los cambios en la ubicación del usuario autenticado en tiempo real
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val ubicacion = dataSnapshot.getValue(Ubicacion::class.java)
                    if (ubicacion != null) {
                        val nuevaUbicacion1 = LatLng(ubicacion.latitud, ubicacion.longitud)
                        val customMarker1 = markers["autenticado"]
                        if (customMarker1 != null) {
                            doctor=(ubicacion.longitud.toString()+","+ubicacion.latitud.toString())
                            updateMarker(customMarker1, nuevaUbicacion1)
                            val zoomLevel = 10.0f // Ajusta el valor según tus necesidades
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nuevaUbicacion1, zoomLevel))
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Ocurrió un error
                }
            })
        }

        val otro = ContextCompat.getDrawable(this, R.drawable.baseline_psychology_24)
        val markerotro = otro?.let { vectorToBitmap(it) }
        val markerOptions2 = MarkerOptions()
            .position(LatLng(0.0, 0.0))
            .icon(markerotro?.let { BitmapDescriptorFactory.fromBitmap(it) })
        val customMarker2 = mMap.addMarker(markerOptions2)
        markers["otro"] = customMarker2

        val databaseReference = FirebaseDatabase.getInstance().reference
        val userEmail = intent.getStringExtra("USER_EMAIL")

        // Escucha los cambios en la ubicación del otro usuario en tiempo real
        databaseReference.child("location")
            .orderByChild("email")
            .equalTo(userEmail)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val ubicacion = snapshot.getValue(Ubicacion::class.java)
                    if (ubicacion != null) {
                        val nuevaUbicacion2 = LatLng(ubicacion.latitud, ubicacion.longitud)
                        val customMarker2 = markers["otro"]
                        if (customMarker2 != null) {
                            paciente=(ubicacion.longitud.toString()+","+ubicacion.latitud.toString())
                            updateMarker(customMarker2, nuevaUbicacion2)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val ubicacion = snapshot.getValue(Ubicacion::class.java)
                    if (ubicacion != null) {
                        val nuevaUbicacion2 = LatLng(ubicacion.latitud, ubicacion.longitud)
                        val customMarker2 = markers["otro"]
                        if (customMarker2 != null) {
                            paciente=(ubicacion.longitud.toString()+","+ubicacion.latitud.toString())
                            updateMarker(customMarker2, nuevaUbicacion2)
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Algo ha sido eliminado de la base de datos, puedes manejarlo si es necesario
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Algo ha sido movido en la base de datos, puedes manejarlo si es necesario
                }

                override fun onCancelled(error: DatabaseError) {
                    // Ocurrió un error
                }
            })
    }
    private fun vectorToBitmap(vector: Drawable): Bitmap {
        val width = vector.intrinsicWidth
        val height = vector.intrinsicHeight
        vector.setBounds(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vector.draw(canvas)
        return bitmap
    }
    private fun updateMarker(marker: Marker, location: LatLng) {
        marker.position = location
        createRoute()
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