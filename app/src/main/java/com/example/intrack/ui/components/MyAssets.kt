package com.example.intrack.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.intrack.model.Asset
import java.util.Locale

@Composable
fun MyAssets(navController: NavHostController, assetList: LiveData<List<Asset>>, onAssetUpdate: (Asset) -> Unit) {
    val assets = assetList.observeAsState(initial = List(18) { Asset() })
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(text = "Assets", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            items(
                items = assets.value
            ) {
                AssetItem(it, navController, onAssetUpdate)
                Divider(thickness = 1.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun AssetItem(asset: Asset, navController: NavHostController, onAssetUpdate: (Asset) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAssetUpdate(asset)
                navController.navigate("detail")
            }, verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            loading = {
                CircularProgressIndicator()
            },
            model = ImageRequest.Builder(LocalContext.current).data(asset.image).crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(50.dp),
            contentScale = ContentScale.Fit
        )
        Column {
            Text(
                text = asset.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                fontWeight = FontWeight.Bold
            )
            asset.quantity?.let { Text(text = "Quantity: $it") }
            when (asset.rented) {
                0 -> Text(text = "Not Available", color = Color(0xFFE20F28))
                1 -> Text(text = "Available", color = Color(0xFF0B9230))
                2 -> Text(text = "My Asset", color = Color(0xFF146EBD))
                3 -> Text(text = "Requested", color = Color(0xFFF57C00))
                else -> Text(text = "Rented", color = Color(0xFFCAE20D))
            }
        }
    }
}