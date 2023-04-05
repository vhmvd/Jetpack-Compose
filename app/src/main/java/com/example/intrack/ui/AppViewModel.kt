package com.example.intrack.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intrack.model.Asset
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*


class AppViewModel : ViewModel() {
    private val currentUser by lazy {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    private val db = Firebase.firestore

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _successfulUpload = MutableStateFlow(false)
    val successfulUpload: StateFlow<Boolean> = _successfulUpload

    private val _assetsMuteableLiveData = MutableLiveData<List<Asset>>()
    val assetsMuteableLiveData: LiveData<List<Asset>> = _assetsMuteableLiveData

    private val _requestsLiveData = MutableLiveData<Boolean>()
    val requestsLiveData: LiveData<Boolean> = _requestsLiveData

    private val _requestsListLiveData = MutableLiveData<List<Asset>>()
    val requestsListLiveData: LiveData<List<Asset>> = _requestsListLiveData

    private val _docMuteableLiveData = MutableLiveData<Asset>()
    val docMuteableLiveData: LiveData<Asset> = _docMuteableLiveData

    private val _assetCount = MutableStateFlow<Long>(0)
    val assetCount: StateFlow<Long> = _assetCount

    private val _rentedAssetCount = MutableStateFlow<Long>(0)
    val rentedAssetCount: StateFlow<Long> = _rentedAssetCount

    fun getQrCodeBitmap(content: String): ImageBitmap {
        val size = 320 //pixels
        val bits = QRCodeWriter().encode("$content", BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) 0x000000 else 0xFFFFFF)
                }
            }
        }.asImageBitmap()
    }

    fun uploadAsset(
        assetName: String, address: String, quantity: String, imageBytes: ByteArray
    ) {
        if (currentUser == "") {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            _successfulUpload.value = false

            val fileName = UUID.randomUUID().toString() + ".jpg"
            val storageRef = FirebaseStorage.getInstance().reference
            val imagesRef = storageRef.child("images/$fileName")
            val uploadTask = imagesRef.putBytes(imageBytes, storageMetadata {
                contentType = "image/jpg"
            })
            val document = db.collection("Users").document(currentUser).collection("Assets")
                .document(assetName)

            uploadTask.addOnSuccessListener {
                it.storage.downloadUrl.addOnCompleteListener { task ->
                    val image = task.result.toString()
                    if (task.isSuccessful) {
                        val asset = hashMapOf(
                            "name" to assetName,
                            "address" to address,
                            "quantity" to quantity,
                            "image" to image,
                            "qr" to "$currentUser/$assetName",
                            "rented" to 2
                        )
                        document.set(asset).addOnCompleteListener {
                            _successfulUpload.value = it.isSuccessful
                        }
                    }
                    _loading.value = false
                }
            }.addOnFailureListener {
                _loading.value = false
            }
        }
    }

    fun getMyAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(currentUser).collection("Assets").get()
                .addOnSuccessListener { result ->
                    _assetsMuteableLiveData.postValue(result.mapNotNull {
                        it?.toObject(Asset::class.java)
                    })
                }
        }
    }

    fun getMyRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(currentUser).collection("Requests").get()
                .addOnSuccessListener { result ->
                    _requestsListLiveData.postValue(result.mapNotNull {
                        it?.toObject(Asset::class.java)
                    })
                }
        }
    }

    fun requestAsset(dat: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = dat.split("/").also {
                if (it.size < 2) return@launch
            }
            db.collection("Users").document(data[0]).collection("Assets").document(data[1]).get()
                .addOnSuccessListener { result ->
                    result?.let {
                        it.toObject(Asset::class.java)
                    } ?: Asset()
                    db.collection("Users").document(currentUser).collection("Assets")
                        .document(data[1]).set(result.data?.apply {
                            this["rented"] = 3
                        } ?: HashMap<String, Any>()).addOnSuccessListener {
                            _requestsLiveData.postValue(true)
                            db.collection("Users").document(data[0]).collection("Requests")
                                .document(data[1]).set(result.data?.apply {
                                    this["request"] = currentUser
                                } ?: HashMap<String, Any>())
                        }
                }
        }
    }

    fun getDocument(dat: String) {
        val data = dat.split("/").also {
            if (it.size < 2) return
        }
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(data[0]).collection("Assets").document(data[1]).get()
                .addOnSuccessListener { result ->
                    val doc = result?.let {
                        it.toObject(Asset::class.java)
                    } ?: Asset()
                    _docMuteableLiveData.postValue(doc)
                }
        }
    }

    fun acceptAssetRequest(asset: Asset) {
        val name = asset.name
        val requester = asset.request
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(requester).collection("Assets").document(name)
                .update("rented", 4)
        }
    }

    fun declineAssetRequest(asset: Asset) {
        val name = asset.name
        val requester = asset.request
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(requester).collection("Assets").document(name).delete()
            db.collection("Users").document(currentUser).collection("Requests").document(name)
                .delete()
        }
    }

    fun getMyAssetsCount() {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(currentUser).collection("Assets").count()
                .get(AggregateSource.SERVER).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _assetCount.value = task.result.count
                    }
                }
        }
    }

    fun getRentAssetsCount() {
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("Users").document(currentUser).collection("Assets")
                .whereNotEqualTo("rented", 2).count().get(AggregateSource.SERVER)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _rentedAssetCount.value = task.result.count
                    }
                }
        }
    }

}
