package com.example.intrack.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*


class AppViewModel : ViewModel() {
    private val currentUser by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    private val db = Firebase.firestore

    private fun getQrCodeBitmap(content: String): ImageBitmap {
        val size = 320 //pixels
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) 0x000000 else 0xFFFFFF)
                }
            }
        }.asImageBitmap()
    }

    fun uploadAsset(assetName: String, address: String, quantity: String, qr: ImageBitmap = getQrCodeBitmap("$currentUser/$assetName"), image: ImageBitmap?, uid: String = currentUser){
        val fileName = UUID.randomUUID().toString() +".jpg"
        val database = FirebaseDatabase.getInstance()
        val refStorage = database.reference.child("images/$fileName")

        val asset = hashMapOf(
            "name" to assetName,
            "address" to address,
            "quantity" to quantity
        )
        db.collection("assets").document(uid).set(asset)
    }
}