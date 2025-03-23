package com.example.proyecto

class User {
    var name: String? = null
    var email: String? = null
    var userID: String? = null
    var PersonaAsociada: String? = null
    var genero: String? = null
    var roll: Int = 0 // 1 encargado, 0 paciente
    val usuarios: ArrayList<String> = ArrayList()

    constructor() // Constructor por defecto
}