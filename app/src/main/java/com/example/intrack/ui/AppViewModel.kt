package com.example.intrack.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.*


class AppViewModel : ViewModel() {
    private val currentUser by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    private val db = Firebase.firestore

    fun getQrCodeBitmap(content: String): ImageBitmap {
        val size = 320 //pixels
        val bits = QRCodeWriter().encode("$currentUser/$content", BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) 0x000000 else 0xFFFFFF)
                }
            }
        }.asImageBitmap()
    }

    fun uploadAsset(
        assetName: String, address: String, quantity: String, imageStream: InputStream
    ) {
        if (currentUser == "") {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val uid = currentUser
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val storageRef = FirebaseStorage.getInstance().reference
            val imagesRef = storageRef.child("images/$fileName")
            val uploadTask: UploadTask = imagesRef.putStream(imageStream)

            uploadTask.addOnSuccessListener {
                val asset = hashMapOf(
                    "name" to assetName,
                    "address" to address,
                    "quantity" to quantity,
                    "image" to "images/$fileName",
                    "qr" to "$currentUser/$assetName",
                    "avail" to true
                )
                db.collection("assets").document("$uid/personal").set(asset)
            }.addOnFailureListener {
                // todo on fail
            }
        }
    }
}