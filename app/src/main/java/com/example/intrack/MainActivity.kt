package com.example.intrack

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.decodeBitmap
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.example.intrack.model.Asset
import com.example.intrack.ui.AppViewModel
import com.example.intrack.ui.InTrackAppBar
import com.example.intrack.ui.InTrackScreen
import com.example.intrack.ui.camera.QRCode
import com.example.intrack.ui.components.AssetDetail
import com.example.intrack.ui.components.EmailTextField
import com.example.intrack.ui.components.HomeContent
import com.example.intrack.ui.components.InputBox
import com.example.intrack.ui.components.LogInOrSignUpScreen
import com.example.intrack.ui.components.MyAssets
import com.example.intrack.ui.components.PasswordTextField
import com.example.intrack.ui.components.Requests
import com.example.intrack.ui.theme.InTrackTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*


class MainActivity : ComponentActivity() {

    private var viewModel = AppViewModel()
    private var photoSelected = mutableStateOf(false)
    private var name = mutableStateOf("")
    private var address = mutableStateOf("")
    private var quantity = mutableStateOf("1")

    private var currentAsset = Asset()

    private var uri: Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        // Callback is invoked after the user selects a media item or closes the photo picker
        it?.let {
            photoSelected.value = true
            uri = it
        } ?: Log.d("PhotoPicker", "No media selected")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InTrackTheme {
                NormalFlow()
            }
        }
    }

    @Composable
    fun AddAssetScreen(navController: NavHostController) {
        var sliderPosition by remember { mutableStateOf(1f) }
        var name by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }

        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = CenterHorizontally
            ) {
                Column {
                    Text(text = "Name", fontWeight = FontWeight.W700)
                    InputBox(value = name, onTextChanged = {
                        name = it
                        this@MainActivity.name.value = it
                    }, label = "Name of asset")
                }
                Column {
                    Row {
                        Text(text = "Quantity: ")
                        Text(text = "${sliderPosition.toInt()}")
                    }
                    Slider(
                        modifier = Modifier.semantics { contentDescription = "Add Item" },
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            quantity.value = it.toInt().toString()
                        },
                        valueRange = 1f..100f
                    )
                }
                Column {
                    Text(text = "Address", fontWeight = FontWeight.W700)
                    InputBox(
                        value = address, onTextChanged = {
                            address = it
                            this@MainActivity.address.value = it
                        }, label = "Address of asset"
                    )
                }
                Column(
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ElevatedCard(
                        Modifier
                            .size(200.dp)
                            .clickable(enabled = true) {
                                photoSelected.value = false
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) {
                        if (photoSelected.value) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(uri)
                                    .crossfade(true).build(),
                                contentDescription = "Asset Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "Asset Image",
                                Modifier
                                    .align(CenterHorizontally)
                                    .size(150.dp)
                            )
                            Text(text = "Add image", Modifier.align(CenterHorizontally))
                        }
                    }
                }
                Button(onClick = {
                    photoSelected.value = false
                    navController.navigateUp()
                    navController.navigate("save")
                }) {
                    Text(text = "Add Asset")
                }
            }
        }
    }

    @Composable
    fun NormalFlow() {
        InTrackTheme {
            val navController = rememberNavController()
            NavGraph(navController)
        }
    }

    @Composable
    fun AssetInfoScreen(data: String, navController: NavHostController) {
        val asset = viewModel.docMuteableLiveData.observeAsState(Asset())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                loading = {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                },
                model = ImageRequest.Builder(LocalContext.current).data(asset.value.image)
                    .crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White),
                contentScale = ContentScale.Fit
            )
            Column(horizontalAlignment = CenterHorizontally) {
                asset.value.name.let { s ->
                    Text(text = s.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    }, fontWeight = FontWeight.Bold)
                }
                asset.value.quantity?.let {
                    Text(text = "Quantity: $it")
                    if (it.toInt() > 0) {
                        Text(text = "Available", color = Color(0xFF0B9230))
                    } else {
                        Text(text = "Not Available", color = Color(0xFFE20F28))
                    }
                }
            }
            Button(onClick = {
                viewModel.requestAsset(data)
                navController.navigateUp()
            }) {
                Text(text = "Request Asset")
            }
        }
    }

    @Composable
    fun QRCodeScanner(onReadQR: (String) -> Unit) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFeature = remember {
            ProcessCameraProvider.getInstance(context)
        }
        var hasCamPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasCamPermission = granted
            })
        LaunchedEffect(key1 = true) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
        ) {
            if (hasCamPermission) {
                AndroidView(factory = { context ->
                    val previewView = PreviewView(context)
                    val preview = Preview.Builder().build()
                    val selector =
                        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    val imageAnalysis = ImageAnalysis.Builder().setTargetResolution(
                        Size(
                            previewView.width, previewView.height
                        )
                    ).setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST).build()
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context), QRCode(onReadQR)
                    )
                    try {
                        cameraProviderFeature.get().bindToLifecycle(
                            lifecycleOwner, selector, preview, imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NavGraph(navController: NavHostController) {
        val firebase = FirebaseAuth.getInstance()
        val currentUser = firebase.currentUser
        val destination = currentUser?.let { "home" } ?: "login"
        NavHost(
            navController = navController, startDestination = destination
        ) {
            composable(route = "login") {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    LogInOrSignUpScreen(navController) { viewModel = AppViewModel() }
                }
            }

            composable(route = "qr") {
                QRCodeScanner {
                    val new = it.replace("/", ";")
                    navController.navigateUp()
                    navController.navigate(route = "info/$new")
                }
            }

            composable(route = "info/{data}") {
                val data = it.arguments?.getString("data") ?: "Error reading the QR."
                val tData = data.replace(";", "/")
                viewModel.getDocument(tData)
                AssetInfoScreen(tData, navController)
            }

            composable(route = "home") {
                val snackbarHostState = remember { SnackbarHostState() }
                var selectedItem by remember { mutableStateOf(0) }
                val items = listOf("Home", "Requests")
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
                        InTrackAppBar(currentScreen = if (selectedItem == 0) {
                            InTrackScreen.Home
                        } else {
                            InTrackScreen.Requests
                        }, canNavigateBack = false, navigateUp = { }, logout = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo("home") {
                                    inclusive = true
                                }
                            }

                        })
                    }, bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(icon = {
                                    Icon(
                                        if (index == 0) {
                                            Icons.Filled.Home
                                        } else {
                                            Icons.Filled.Notifications
                                        }, contentDescription = item
                                    )
                                },
                                    label = { Text(item) },
                                    selected = selectedItem == index,
                                    onClick = { selectedItem = index })
                            }
                        }
                    }, content = { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp)
                        ) {
                            if (selectedItem == 0) {
                                HomeContent(onScanQR = {
                                    navController.navigate("qr")
                                }, onAddAsset = {
                                    navController.navigate("add")
                                }, onMyAssets = {
                                    viewModel.getMyAssets()
                                    navController.navigate("store")
                                }, onBrowseAssets = {
                                    viewModel.getMyAssets()
                                    navController.navigate("browser")
                                }, viewModel
                                )
                            } else if (selectedItem == 1) {
                                Requests(viewModel)
                            }
                        }
                    })
                }
            }

            composable(route = "add") { AddAssetScreen(navController) }
            composable(route = "save") { SaveAsset(navController) }
            composable(route = "store") {
                MyAssets(navController, viewModel.assetsMuteableLiveData) { currentAsset = it }
            }
            composable(route = "detail") {
                AssetDetail(currentAsset, navController, viewModel, contentResolver)
            }
            composable(route = "register") { Register(navController = navController) }
            composable(route = "browser") {
                LaunchedEffect(key1 = "getAllAssets") {
                    viewModel.getAllAssets()
                }
                MyAssets(
                    navController, viewModel.allAssetLiveData
                ) { currentAsset = it }
            }
        }
    }

    @Composable
    fun Register(navController: NavHostController) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var isPasswordValid by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmailTextField(email = email, onTextChanged = { email = it })
            PasswordTextField(
                password = password,
                onTextChanged = {
                    password = it
                    isPasswordValid = password.length >= 6
                },
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityChanged = { isPasswordVisible = isPasswordVisible.not() },
                isPasswordValid = isPasswordValid,
            )
            Button(
                enabled = password.length >= 6, onClick = {
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("home") {
                                    popUpTo("login") {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Text(text = "Register")
            }
        }
    }

    @Composable
    fun SaveAsset(navController: NavHostController) {
        val success = viewModel.successfulUpload.collectAsState()
        val uploading = viewModel.loading.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        if (uploading.value.not()) {
            if (success.value) {
                navController.navigateUp()
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp), Arrangement.Top, CenterHorizontally
        ) {
            Row(Modifier.padding(8.dp)) {
                Text("Name: ", fontWeight = FontWeight.W700)
                Text(text = name.value)
            }
            Row(Modifier.padding(8.dp)) {
                Text("Address: ", fontWeight = FontWeight.W700)
                Text(text = address.value)
            }
            Row(Modifier.padding(8.dp)) {
                Text("Quantity: ", fontWeight = FontWeight.W700)
                Text(text = quantity.value)
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true)
                    .build(),
                contentDescription = "Asset Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
            Image(
                bitmap = viewModel.getQrCodeBitmap(name.value),
                contentDescription = "QR",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            )
            Button(
                onClick = {
                    if (uri != null) {
                        coroutineScope.launch {
                            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.createSource(contentResolver, uri!!)
                                    .decodeBitmap { _, _ ->
                                    }
                            } else {
                                MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            }
                            val baos = ByteArrayOutputStream()
                            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
                            viewModel.uploadAsset(
                                name.value, address.value, quantity.value, baos.toByteArray()
                            )
                        }
                    }
                }, modifier = Modifier.padding(16.dp), enabled = uploading.value.not()
            ) {
                Text(text = "Confirm Save")
            }
            if (uploading.value) {
                CircularProgressIndicator()
            }
        }
    }

}
