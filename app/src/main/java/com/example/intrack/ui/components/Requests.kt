package com.example.intrack.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.intrack.model.Asset
import com.example.intrack.ui.AppViewModel

@Composable
fun Requests(viewModel: AppViewModel) {
    LaunchedEffect(key1 = "Request") {
        viewModel.getMyRequests()
    }
    val requests = viewModel.requestsListLiveData.observeAsState(initial = List(18) { Asset() })
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            if (requests.value.isEmpty()) {
                Text(text = "No Requests")
            } else {
                Text(text = "Requests")
            }
        }
        items(requests.value) {
            RequestItem(it, viewModel)
        }
    }
}

@Composable
fun RequestItem(asset: Asset, viewModel: AppViewModel) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = asset.name, modifier = Modifier.padding(end = 4.dp))
        RequestButtons(asset, viewModel)
    }
}

@Composable
fun RequestButtons(asset: Asset, viewModel: AppViewModel) {
    Row {
        Button(
            onClick = {
                viewModel.acceptAssetRequest(asset)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            modifier = Modifier.padding(2.dp)
        ) {
            Text(text = "Accept")
        }
        Button(
            onClick = {
                viewModel.declineAssetRequest(asset)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.padding(2.dp)
        ) {
            Text(text = "Decline")
        }
    }
}