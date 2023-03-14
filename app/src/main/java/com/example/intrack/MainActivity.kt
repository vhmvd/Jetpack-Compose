package com.example.intrack

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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
import com.example.intrack.ui.theme.InTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : ComponentActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private var photoSelected = mutableStateOf(false)
    private var name = mutableStateOf("")
    private var address = mutableStateOf("")
    private var quantity = mutableStateOf("1")

    private var currentAsset = Asset()

    private var uri: Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
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
    fun AssetInfoScreen(data: String) {
        val asset = viewModel.docMuteableLiveData.observeAsState(Asset())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                loading = {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp))
                },
                model = ImageRequest.Builder(LocalContext.current).data(asset.value.image)
                    .crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
            Column {
                asset.value.name.let { s ->
                    Text(text = s.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    }, fontWeight = FontWeight.Bold)
                }
                asset.value.quantity?.let { Text(text = "Quantity: $it") }
                when (asset.value.rented) {
                    0 -> Text(text = "Not Available", color = Color(0xFFE20F28))
                    1 -> Text(text = "Available", color = Color(0xFF0B9230))
                    2 -> Text(text = "My Asset", color = Color(0xFF146EBD))
                    else -> Text(text = "Rented", color = Color(0xFFCAE20D))
                }
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Request Asset")
                }
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

    @Composable
    fun RequestColumn() {

    }

    @Composable
    fun Requests() {
        Text(text = "Requests")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {

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
                    LogInOrSignUpScreen(navController)
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
                AssetInfoScreen(tData)
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
                                })
                            } else if (selectedItem == 1) {
                                Requests()
                            }
                        }
                    })
                }
            }

            composable(route = "add") {
                AddAssetScreen(navController)
            }

            composable(route = "save") {
                SaveAsset(navController)
            }

            composable(route = "store") {
                MyAssets(navController)
            }

            composable(route = "detail") {
                AssetDetail()
            }

            composable(route = "register") {
                Register(navController = navController)
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
    fun AssetDetail(asset: Asset = currentAsset) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            asset.qr?.let {
                Image(
                    bitmap = viewModel.getQrCodeBitmap(it),
                    contentDescription = "QR",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                )
            }
            Text(
                text = asset.address.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                fontWeight = FontWeight.Bold
            )
        }
    }

    @Composable
    fun AssetItem(asset: Asset, navController: NavHostController) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    currentAsset = asset
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
                    else -> Text(text = "Rented", color = Color(0xFFCAE20D))
                }
            }
        }
    }

    @Composable
    fun MyAssets(navController: NavHostController) {
        val assets = viewModel.assetsMuteableLiveData.observeAsState(initial = List(18) { Asset() })
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = CenterHorizontally
            ) {
                item {
                    Text(text = "Assets", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                items(
                    items = assets.value
                ) {
                    AssetItem(it, navController)
                    Divider(thickness = 1.dp, color = Color.LightGray)
                }
            }
        }
    }

    @Composable
    fun SaveAsset(navController: NavHostController) {
        val success = viewModel.successfulUpload.collectAsState()
        val uploading = viewModel.loading.collectAsState()

        if (uploading.value.not()) {
            if (success.value) {
                navController.navigateUp()
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp), Arrangement.Top, Alignment.CenterHorizontally
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
                        viewModel.uploadAsset(
                            name.value, address.value, quantity.value, uri!!
                        )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InputBox(value: String, onTextChanged: (String) -> Unit, label: String) {
        OutlinedTextField(
            value = value,
            onValueChange = onTextChanged,
            label = { Text(text = label) },
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    fun HomeContent(onScanQR: () -> Unit, onAddAsset: () -> Unit, onMyAssets: () -> Unit) {
        Button(
            onClick = onScanQR, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
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
                .padding(vertical = 16.dp)
        ) {
            Text(text = "My Assets")
        }

        Row() {

        }
    }

    @Composable
    fun LogInOrSignUpScreen(navController: NavHostController) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var isPasswordValid by remember { mutableStateOf(true) }
        var loading by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier)

        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.background(color = Color.White)
        ) {
            when (loading) {
                true -> CircularProgressIndicator(
                    Modifier
                        .zIndex(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = Color.White)
                        .border(
                            width = 0.dp,
                            shape = RoundedCornerShape(8.dp),
                            brush = SolidColor(value = Color.LightGray)
                        )
                )
                false -> Unit
            }
            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
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
                    onClick = {
                        loading = true
                        FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener {
                                loading = false
                                if (it.isSuccessful) {
                                    navController.navigate("home") {
                                        popUpTo("login") {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = (it.exception as FirebaseAuthException).localizedMessage
                                                ?: "Try again later"
                                        )
                                    }
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    Text(text = "Login")
                }
                Divider(thickness = 1.dp, modifier = Modifier.padding(8.dp))
                Button(
                    onClick = {
                        navController.navigate("register")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp)
                ) {
                    Text(text = "Create Account")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PasswordTextField(
        password: String,
        onTextChanged: (String) -> Unit,
        isPasswordVisible: Boolean,
        onPasswordVisibilityChanged: () -> Unit,
        isPasswordValid: Boolean,
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = onTextChanged,
            label = { Text(text = "Password") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (isPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = onPasswordVisibilityChanged) {
                    Icon(imageVector = image, description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = isPasswordValid.not(),
            supportingText = {
                if (isPasswordValid.not()) {
                    Text(text = "Password length should be at least 6 characters.")
                }
            },
            maxLines = 1
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EmailTextField(email: String, onTextChanged: (String) -> Unit) {
        OutlinedTextField(
            value = email,
            onValueChange = onTextChanged,
            label = { Text(text = "Email") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}