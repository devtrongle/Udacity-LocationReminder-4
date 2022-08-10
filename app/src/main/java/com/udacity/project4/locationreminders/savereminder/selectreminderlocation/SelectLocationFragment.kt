package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.it_reminder.*
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.random.Random

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var googleMap: GoogleMap? = null

    private var poiSelected: PointOfInterest? = null
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        onLocationSelected()
        return binding.root
    }

    private fun onLocationSelected() {
        binding.saveButton.setOnClickListener{
            marker?.apply {
                _viewModel.latitude.value = this.position.latitude
                _viewModel.longitude.value = this.position.longitude
                _viewModel.reminderSelectedLocationStr.value = this.title
                _viewModel.selectedPOI.value = poiSelected ?: PointOfInterest(this.position, Date().time.toString(), this.title)
                findNavController().popBackStack()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap?) {
        p0?.apply {
            googleMap = this
            enableMyLocation()
            googleMap?.apply {
                // Add a marker in HCM and move the camera
                val hcm = LatLng(10.819689728300116, 106.65901825254176)
                marker = addMarker(
                    MarkerOptions()
                        .position(hcm)
                        .title("Marker in HCM")
                )
                moveCamera(CameraUpdateFactory.newLatLngZoom(hcm, 12f))
                setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )

                setPoiClick(this)
                setMapLongClick(this)
            }
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            googleMap?.isMyLocationEnabled = true
        }
        else {
            sendRequestPermissions()
        }
    }

    private fun sendRequestPermissions(){
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            var isGranted = true
            results.forEach {
                if (!it.value) {
                    isGranted = false
                }
            }
            if (!isGranted) {
                _viewModel.showToast.value = getString(R.string.permission_denied_explanation)
            } else {
                enableMyLocation()
            }
        }

    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { results ->
            if (!results) {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            marker?.apply {
                remove()
            }
            marker = map.addMarker(
            MarkerOptions()
                .position(it)
                .title(String.format(getString(R.string.lat_long_snippet), it.latitude, it.longitude)))
            marker!!.showInfoWindow()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.apply {
                remove()
            }
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            marker!!.showInfoWindow()
            poiSelected = poi
        }
    }

}
