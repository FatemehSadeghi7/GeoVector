package com.example.geovector.presentation.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.geovector.data.local.entity.LocationEntity
import com.example.geovector.di.AppModule
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(onLogout: () -> Unit = {}) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val locationDao = remember { AppModule.provideLocationDao(context) }
        val userDao = remember { AppModule.provideDatabase(context).userDao() }

        val isDark = isSystemInDarkTheme()

        var username by remember { mutableStateOf("") }
        var userLocation by remember { mutableStateOf<LatLng?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }
        var isTracking by remember { mutableStateOf(false) }
        var sessionId by remember { mutableLongStateOf(0L) }
        var showDownload by remember { mutableStateOf(false) }
        var finishedSessionId by remember { mutableLongStateOf(0L) }
        var showLogoutDialog by remember { mutableStateOf(false) }
        var initialLocationSet by remember { mutableStateOf(false) }
        var trackingStartTime by remember { mutableLongStateOf(0L) }
        var elapsedTime by remember { mutableLongStateOf(0L) }
        var totalDistance by remember { mutableStateOf(0f) }

        val accentColor = Color(0xFF2E7D32)
        val dangerColor = Color(0xFFE53935)
        val cardBg = if (isDark) Color(0xFF1A1A2E).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.95f)
        val textPrimary = if (isDark) Color.White else Color(0xFF1B5E20)
        val textSecondary = if (isDark) Color(0xFFB0BEC5) else Color(0xFF607D8B)

        LaunchedEffect(Unit) {
            val user = userDao.getLoggedInUser()
            username = user?.username ?: "test_user"
        }

        val points by locationDao.getPointsBySession(sessionId).collectAsState(initial = emptyList())
        val polylinePoints = remember(points) {
            points.map { LatLng(it.latitude, it.longitude) }
        }

        LaunchedEffect(points) {
            if (points.size >= 2) {
                var dist = 0f
                for (i in 1 until points.size) {
                    val r = FloatArray(1)
                    android.location.Location.distanceBetween(
                        points[i - 1].latitude, points[i - 1].longitude,
                        points[i].latitude, points[i].longitude, r
                    )
                    dist += r[0]
                }
                totalDistance = dist
            }
        }

        LaunchedEffect(isTracking) {
            if (isTracking) {
                trackingStartTime = System.currentTimeMillis()
                while (isTracking) {
                    elapsedTime = System.currentTimeMillis() - trackingStartTime
                    delay(1000)
                }
            } else {
                elapsedTime = 0L
            }
        }

        val finishedPoints by locationDao.getPointsBySession(finishedSessionId).collectAsState(initial = emptyList())

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(35.6892, 51.3890), 12f)
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasPermission =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }

        LaunchedEffect(Unit) {
            if (!hasPermission) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        DisposableEffect(hasPermission) {
            var initialCallback: LocationCallback? = null

            if (hasPermission && !initialLocationSet) {
                isLoading = true
                val client = LocationServices.getFusedLocationProviderClient(context)
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 500L
                ).setMinUpdateDistanceMeters(0f)
                    .setWaitForAccurateLocation(true)
                    .build()

                initialCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { loc ->
                            if (loc != null) {
                                val latLng = LatLng(loc.latitude, loc.longitude)
                                userLocation = latLng
                                initialLocationSet = true
                                isLoading = false

                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 18f), 800
                                    )
                                }
                                client.removeLocationUpdates(this)
                            }
                        }
                    }
                }

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    client.requestLocationUpdates(request, initialCallback, Looper.getMainLooper())
                }
            }

            onDispose {
                initialCallback?.let {
                    LocationServices.getFusedLocationProviderClient(context)
                        .removeLocationUpdates(it)
                }
            }
        }

        DisposableEffect(isTracking, username) {
            var callback: LocationCallback? = null

            if (isTracking && hasPermission) {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000L
                ).setMinUpdateDistanceMeters(0f)
                    .setWaitForAccurateLocation(true)
                    .build()

                callback = object : LocationCallback() {
                    var lastSaved: LatLng? = null

                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { loc ->
                            val latLng = LatLng(loc.latitude, loc.longitude)

                            userLocation = latLng
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(latLng), 300
                                )
                            }

                            val distance = lastSaved?.let { last ->
                                val r = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    last.latitude, last.longitude,
                                    latLng.latitude, latLng.longitude, r
                                )
                                r[0]
                            }

                            if (distance == null || distance >= 5f) {
                                lastSaved = latLng
                                scope.launch {
                                    locationDao.insert(
                                        LocationEntity(
                                            username = username,
                                            sessionId = sessionId,
                                            latitude = loc.latitude,
                                            longitude = loc.longitude
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                }
            }

            onDispose {
                callback?.let {
                    LocationServices.getFusedLocationProviderClient(context)
                        .removeLocationUpdates(it)
                }
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )

        Box(modifier = Modifier.fillMaxSize()) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasPermission),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true
                )
            ) {
                if (polylinePoints.size >= 2) {
                    Polyline(
                        points = polylinePoints,
                        color = Color(0xFF2196F3),
                        width = 14f
                    )
                }
            }

            AnimatedVisibility(
                visible = username.isNotEmpty(),
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp, end = 72.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    shadowElevation = 8.dp,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = username.firstOrNull()?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        )
                    }
                }
            }

            Surface(
                onClick = { showLogoutDialog = true },
                shape = CircleShape,
                color = dangerColor,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Logout,
                        contentDescription = "خروج",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isTracking,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardBg,
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // زمان
                        InfoItem(
                            icon = Icons.Outlined.Timer,
                            label = "زمان",
                            value = formatElapsedTime(elapsedTime),
                            color = Color(0xFF1565C0),
                            textColor = textPrimary,
                            labelColor = textSecondary
                        )

                        // خط جدا کننده
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(textSecondary.copy(alpha = 0.2f))
                        )

                        // مسافت
                        InfoItem(
                            icon = Icons.Outlined.Route,
                            label = "مسافت",
                            value = formatDistance(totalDistance),
                            color = accentColor,
                            textColor = textPrimary,
                            labelColor = textSecondary
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(textSecondary.copy(alpha = 0.2f))
                        )

                        InfoItem(
                            icon = Icons.Outlined.PinDrop,
                            label = "نقاط",
                            value = "${points.size}",
                            color = Color(0xFFE65100),
                            textColor = textPrimary,
                            labelColor = textSecondary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = showDownload,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExportButton(
                            text = "XLSX",
                            icon = Icons.Outlined.TableChart,
                            color = Color(0xFF1B5E20),
                            onClick = {
                                scope.launch { exportToXlsx(context, finishedPoints, username) }
                            }
                        )
                        ExportButton(
                            text = "SVG",
                            icon = Icons.Outlined.Image,
                            color = Color(0xFFE65100),
                            onClick = {
                                scope.launch { exportToSvg(context, finishedPoints, username) }
                            }
                        )
                    }
                }

                val trackColor by animateColorAsState(
                    if (isTracking) dangerColor else accentColor, label = "trackColor"
                )

                FloatingActionButton(
                    onClick = {
                        if (!hasPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                            return@FloatingActionButton
                        }
                        if (isTracking) {
                            isTracking = false
                            finishedSessionId = sessionId
                            showDownload = true
                        } else {
                            showDownload = false
                            totalDistance = 0f
                            sessionId = System.currentTimeMillis()
                            isTracking = true
                        }
                    },
                    containerColor = trackColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .then(
                            if (isTracking) Modifier.border(
                                width = 3.dp,
                                color = dangerColor.copy(alpha = pulseAlpha),
                                shape = CircleShape
                            ) else Modifier
                        ),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        if (isTracking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isTracking) "پایان" else "شروع",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (hasPermission) {
                        scope.launch {
                            isLoading = true
                            val loc = getAccurateLocation(context)
                            if (loc != null) {
                                userLocation = loc
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(loc, 18f), 500
                                )
                            }
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
                    .size(52.dp),
                containerColor = if (isDark) Color(0xFF16213E).copy(alpha = 0.95f) else Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = accentColor,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.MyLocation,
                        contentDescription = "موقعیت من",
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ========== دیالوگ خروج ==========
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = if (isDark) Color(0xFF16213E) else Color.White,
                    icon = {
                        Surface(
                            shape = CircleShape,
                            color = dangerColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Logout,
                                    contentDescription = null,
                                    tint = dangerColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            "خروج از حساب",
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    },
                    text = {
                        Text(
                            "آیا مطمئنی که می‌خواهی خارج بشی؟\nتمام اطلاعات مسیر پاک می‌شود.",
                            color = textSecondary,
                            lineHeight = 24.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                val currentUsername = username
                                scope.launch(Dispatchers.IO) {
                                    if (isTracking) isTracking = false
                                    locationDao.deleteByUsername(currentUsername)
                                    userDao.deleteByUsername(currentUsername)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "خروج موفق", Toast.LENGTH_SHORT).show()
                                        onLogout()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = dangerColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("بله، خروج", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("انصراف", color = textSecondary)
                        }
                    }
                )
            }
        }
    }
}


@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    textColor: Color,
    labelColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = labelColor,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun ExportButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = color,
        shadowElevation = 4.dp,
        modifier = Modifier.width(64.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text,
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatDistance(meters: Float): String {
    return if (meters >= 1000) {
        String.format("%.1f km", meters / 1000)
    } else {
        String.format("%.0f m", meters)
    }
}

// ========== Export Functions ==========

private suspend fun exportToXlsx(context: Context, points: List<LocationEntity>, username: String) {
    withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val sheetData = buildString {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
                append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n")
                append("<sheetViews><sheetView tabSelected=\"1\" workbookViewId=\"0\" rightToLeft=\"true\"/></sheetViews>\n")
                append("<cols>\n")
                append("<col min=\"1\" max=\"1\" width=\"8\" bestFit=\"1\" customWidth=\"1\"/>\n")
                append("<col min=\"2\" max=\"3\" width=\"18\" bestFit=\"1\" customWidth=\"1\"/>\n")
                append("<col min=\"4\" max=\"4\" width=\"22\" bestFit=\"1\" customWidth=\"1\"/>\n")
                append("<col min=\"5\" max=\"5\" width=\"15\" bestFit=\"1\" customWidth=\"1\"/>\n")
                append("</cols>\n")
                append("<sheetData>\n")

                append("<row r=\"1\">\n")
                append("<c r=\"A1\" t=\"inlineStr\"><is><t>ردیف</t></is></c>\n")
                append("<c r=\"B1\" t=\"inlineStr\"><is><t>عرض جغرافیایی</t></is></c>\n")
                append("<c r=\"C1\" t=\"inlineStr\"><is><t>طول جغرافیایی</t></is></c>\n")
                append("<c r=\"D1\" t=\"inlineStr\"><is><t>زمان</t></is></c>\n")
                append("<c r=\"E1\" t=\"inlineStr\"><is><t>کاربر</t></is></c>\n")
                append("</row>\n")

                points.forEachIndexed { index, point ->
                    val row = index + 2
                    append("<row r=\"$row\">\n")
                    append("<c r=\"A$row\"><v>${index + 1}</v></c>\n")
                    append("<c r=\"B$row\"><v>${point.latitude}</v></c>\n")
                    append("<c r=\"C$row\"><v>${point.longitude}</v></c>\n")
                    append("<c r=\"D$row\" t=\"inlineStr\"><is><t>${dateFormat.format(Date(point.timestamp))}</t></is></c>\n")
                    append("<c r=\"E$row\" t=\"inlineStr\"><is><t>${point.username}</t></is></c>\n")
                    append("</row>\n")
                }

                append("</sheetData>\n")
                append("</worksheet>")
            }

            val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

            val workbookXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Locations" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

            val relsRoot = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

            val relsWorkbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""

            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "locations_${username}_${System.currentTimeMillis()}.xlsx"
            )

            java.util.zip.ZipOutputStream(FileOutputStream(file)).use { zip ->
                fun writeEntry(name: String, content: String) {
                    zip.putNextEntry(java.util.zip.ZipEntry(name))
                    zip.write(content.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                writeEntry("[Content_Types].xml", contentTypes)
                writeEntry("_rels/.rels", relsRoot)
                writeEntry("xl/workbook.xml", workbookXml)
                writeEntry("xl/_rels/workbook.xml.rels", relsWorkbook)
                writeEntry("xl/worksheets/sheet1.xml", sheetData)
            }

            withContext(Dispatchers.Main) {
                shareFile(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                Toast.makeText(context, "فایل XLSX ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun exportToSvg(context: Context, points: List<LocationEntity>, username: String) {
    withContext(Dispatchers.IO) {
        try {
            if (points.isEmpty()) return@withContext

            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLng = points.minOf { it.longitude }
            val maxLng = points.maxOf { it.longitude }

            val width = 800.0
            val height = 600.0
            val padding = 40.0

            fun scaleX(lng: Double): Double {
                return if (maxLng == minLng) width / 2
                else padding + (lng - minLng) / (maxLng - minLng) * (width - 2 * padding)
            }

            fun scaleY(lat: Double): Double {
                return if (maxLat == minLat) height / 2
                else padding + (maxLat - lat) / (maxLat - minLat) * (height - 2 * padding)
            }

            val pathData = points.mapIndexed { index, point ->
                val x = scaleX(point.longitude)
                val y = scaleY(point.latitude)
                if (index == 0) "M $x $y" else "L $x $y"
            }.joinToString(" ")

            val svg = buildString {
                append("""<?xml version="1.0" encoding="UTF-8"?>""")
                append("\n")
                append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">""")
                append("\n")
                append("""  <rect width="100%" height="100%" fill="#f0f0f0"/>""")
                append("\n")
                append("""  <text x="10" y="20" font-size="14" fill="#333">مسیر $username</text>""")
                append("\n")
                append("""  <path d="$pathData" fill="none" stroke="#2196F3" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>""")
                append("\n")
                points.forEach { point ->
                    val x = scaleX(point.longitude)
                    val y = scaleY(point.latitude)
                    append("""  <circle cx="$x" cy="$y" r="4" fill="#1565C0"/>""")
                    append("\n")
                }
                val sx = scaleX(points.first().longitude)
                val sy = scaleY(points.first().latitude)
                append("""  <circle cx="$sx" cy="$sy" r="7" fill="#43A047" stroke="white" stroke-width="2"/>""")
                append("\n")
                val ex = scaleX(points.last().longitude)
                val ey = scaleY(points.last().latitude)
                append("""  <circle cx="$ex" cy="$ey" r="7" fill="#E53935" stroke="white" stroke-width="2"/>""")
                append("\n")
                append("</svg>")
            }

            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "locations_${username}_${System.currentTimeMillis()}.svg"
            )
            FileWriter(file).use { it.write(svg) }

            withContext(Dispatchers.Main) {
                shareFile(context, file, "image/svg+xml")
                Toast.makeText(context, "فایل SVG ذخیره شد", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "خطا: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun shareFile(context: Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک فایل").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private suspend fun getAccurateLocation(context: Context): LatLng? {
    return try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        if (location != null) LatLng(location.latitude, location.longitude) else null
    } catch (e: Exception) { null }
}
