package com.example.intrack.ui.components

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.intrack.model.Asset
import com.example.intrack.ui.AppViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@Composable
fun AssetDetail(
    asset: Asset,
    navController: NavHostController,
    viewModel: AppViewModel,
    contentResolver: ContentResolver
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = asset.name, fontWeight = FontWeight.Bold
        )
        SubcomposeAsyncImage(
            loading = {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
            },
            model = ImageRequest.Builder(LocalContext.current).data(asset.image).crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .background(Color.White),
            contentScale = ContentScale.Fit
        )
        when (asset.rented) {
            0 -> Text(text = "Not Available", color = Color(0xFFE20F28))
            1 -> Text(text = "Available", color = Color(0xFF0B9230))
            2 -> Text(text = "My Asset", color = Color(0xFF146EBD))
            3 -> Text(text = "Requested", color = Color(0xFFF57C00))
            else -> Text(text = "Rented", color = Color(0xFFCAE20D))
        }
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(enabled = asset.rented != 2,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B9230)),
                    modifier = Modifier.padding(horizontal = 2.dp),
                    onClick = {
                        asset.qr?.let { viewModel.requestAsset(it) }
                        navController.navigateUp()
                    }) {
                    Text(text = "Request Asset")
                }
                Button(enabled = asset.rented > 2,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    modifier = Modifier.padding(horizontal = 2.dp),
                    onClick = {
                        asset.qr?.let { /* TODO */ }
                        navController.navigateUp()
                    }) {
                    Text(text = "Return Asset")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    asset.qr?.let {
                        viewModel.getQrCodeBitmap(it).run {
                            saveMediaToStorage(this.asAndroidBitmap(), contentResolver)
                        }
                    }
                    navController.navigateUp()
                }) {
                    Text(text = "Save QR")
                }
            }
        }
    }
}

private fun saveMediaToStorage(bitmap: Bitmap, contentResolver: ContentResolver) {
    val filename = "${System.currentTimeMillis()}.jpg"
    var fos: OutputStream?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentResolver.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri: Uri? =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }
    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, it)
    }
}