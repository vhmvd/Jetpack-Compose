package com.example.intrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.intrack.ui.AppViewModel

@Composable
fun HomeContent(
    onScanQR: () -> Unit,
    onAddAsset: () -> Unit,
    onMyAssets: () -> Unit,
    onBrowseAssets: () -> Unit,
    viewModel: AppViewModel
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .aspectRatio(1f)
                .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No. of Assets",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier,
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val assetCount = viewModel.assetCount.collectAsState()
                LaunchedEffect(key1 = "totalAssets") {
                    viewModel.getMyAssetsCount()
                }
                Text(
                    text = "${assetCount.value}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 64.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .aspectRatio(1f)
                .border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Rent Assets",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier,
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val rentAssetCount = viewModel.rentedAssetCount.collectAsState()
                LaunchedEffect(key1 = "rentAssets") {
                    viewModel.getRentAssetsCount()
                }
                Text(
                    text = "${rentAssetCount.value}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 64.sp
                )
            }
        }
    }
    Button(
        onClick = onScanQR, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(text = "Scan QR Code")
    }

    Button(
        onClick = onAddAsset, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = "Add Assets")
    }

    Button(
        onClick = onMyAssets, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = "My Assets")
    }

    Button(
        onClick = onBrowseAssets, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = "Browse Assets")
    }
}