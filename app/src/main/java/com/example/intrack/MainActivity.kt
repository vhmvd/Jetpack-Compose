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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.*
import com.example.intrack.ui.AppViewModel
import com.example.intrack.ui.InTrackAppBar
import com.example.intrack.ui.InTrackScreen
import com.example.intrack.ui.camera.QRCode
import com.example.intrack.ui.theme.InTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val viewModel = AppViewModel()
    var photoSelected = mutableStateOf(false)
    var name = mutableStateOf("")
    var address = mutableStateOf("")
    var quantity = mutableStateOf("")

    var uri: Uri? = null
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
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scan))
        val progress by animateLottieCompositionAsState(composition = composition)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                alignment = Alignment.Center,
                modifier = Modifier.size(400.dp)
            )
            Text(text = data, fontStyle = FontStyle.Normal, fontWeight = FontWeight.W700)
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
                    LogInOrSignUpScreen(navController)
                }
            }

            composable(route = "qr") {
                QRCodeScanner {
                    navController.navigateUp()
                    navController.navigate(route = "info/$it")
                }
            }

            composable(route = "info/{data}") {
                val data = it.arguments?.getString("data") ?: "Error reading the QR."
                AssetInfoScreen(data)
            }

            composable(route = "home") {
                val snackbarHostState = remember { SnackbarHostState() }
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
                        InTrackAppBar(currentScreen = InTrackScreen.Home,
                            canNavigateBack = false,
                            navigateUp = { },
                            logout = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo("home") {
                                        inclusive = true
                                    }
                                }
                            })
                    }, bottomBar = {
                        var selectedItem by remember { mutableStateOf(0) }
                        val items = listOf("Home", "Notifications")

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
                            HomeContent(onScanQR = {
                                navController.navigate("qr")
                            }, onAddAsset = {
                                navController.navigate("add")
                            })
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
        }
    }

    @Composable
    fun SaveAsset(navController: NavHostController) {
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
                bitmap = getQrCodeBitmap(name.value),
                contentDescription = "QR",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            )
            Button(
                onClick = {
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        } else {
                            uri?.let {
                                ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(
                                        contentResolver, it
                                    )
                                )
                            }
                        }
                        bitmap?.asImageBitmap()?.let {
                            viewModel.uploadAsset(
                                assetName = name.value,
                                address = address.value,
                                image = null,
                                quantity = quantity.value
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "Confirm Save")
            }
        }
    }

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
    fun HomeContent(onScanQR: () -> Unit, onAddAsset: () -> Unit) {
        Button(
            onClick = onScanQR, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "Scar QR code")
        }

        Button(
            onClick = onAddAsset, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Add assets")
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

        // Lottie
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.truck))
        val progress by animateLottieCompositionAsState(composition = composition, iterations = 5)

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier)

        Box(contentAlignment = Alignment.Center) {
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
                    .width(IntrinsicSize.Max)
                    .padding(16.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    alignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
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