package net.metalbrain.paysmart.ui.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
fun AddressMapPreview(
    lat: Double,
    lng: Double,
    markerTitle: String,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { googleMap ->
                val latLng = LatLng(lat, lng)
                googleMap.uiSettings.isZoomControlsEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(latLng).title(markerTitle))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
    )
}
