package com.example.intrack.model

import com.google.firebase.firestore.PropertyName

data class Asset(
    @PropertyName("address") val address: String? = null,
    @PropertyName("image") val image: String? = null,
    @PropertyName("name") val name: String? = null,
    @PropertyName("qr") val qr: String? = null,
    @PropertyName("quantity") val quantity: String? = null,
    @PropertyName("rented") var rented: Boolean? = null
)
