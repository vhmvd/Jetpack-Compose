package com.example.intrack.model

import com.google.firebase.firestore.PropertyName

data class Asset(
    @PropertyName("address") val address: String = "Loading..",
    @PropertyName("image") val image: String = "Loading..",
    @PropertyName("name") val name: String = "Loading..",
    @PropertyName("qr") val qr: String? = null,
    @PropertyName("quantity") val quantity: String? = null,
    @PropertyName("rented") var rented: Int = 0,
    @PropertyName("request") var request: String = ""
)
