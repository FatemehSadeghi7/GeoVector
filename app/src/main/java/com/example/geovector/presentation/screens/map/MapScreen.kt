
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await

@Composable
fun MapScreen() {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(hasLocationPermission(context))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(35.6892, 51.3890), 12f)
    }

    // پرمیشن
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // اولین بار → پرمیشن بخواه
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

    // وقتی پرمیشن داده شد → لوکیشن بگیر
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isLoading = true
            val location = getCurrentLocation(context)
            if (location != null) {
                userLocation = location
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(location, 15f),
                    1000
                )
            }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasPermission
            )
        ) {
            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "موقعیت من"
                )
            }
        }

        // دکمه رفتن به لوکیشن
        FloatingActionButton(
            onClick = {
                if (hasPermission) {
                    isLoading = true
                    getLocationAndMove(context, cameraPositionState) { loc ->
                        userLocation = loc
                        isLoading = false
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = Color.White
                )
            }
        }
    }
}

// چک پرمیشن
private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// گرفتن لوکیشن (suspend)
@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: Context): LatLng? {
    return try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val location = client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
        if (location != null) LatLng(location.latitude, location.longitude) else null
    } catch (e: Exception) {
        null
    }
}

// گرفتن لوکیشن و بردن دوربین
@SuppressLint("MissingPermission")
private fun getLocationAndMove(
    context: Context,
    cameraState: CameraPositionState,
    onResult: (LatLng?) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    client.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        CancellationTokenSource().token
    ).addOnSuccessListener { location ->
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            onResult(latLng)
            cameraState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        } else {
            onResult(null)
        }
    }.addOnFailureListener {
        onResult(null)
    }
}