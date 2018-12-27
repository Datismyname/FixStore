package com.hadistudios.fixstore.repairshop

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.hadistudios.fixstore.AppConstants
import com.hadistudios.fixstore.ChatActivity
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.UserLocationObject
import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.startActivity

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private lateinit var mView: View
    private lateinit var mMapView: MapView

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var lastLocationMarker: Marker? = null
    var firstTime = true
    var cameraTracksLocation = true
    lateinit var locationUrl:String


    // 1
    private lateinit var locationCallback: LocationCallback
    // 2
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        // 3
        private const val REQUEST_CHECK_SETTINGS = 2
    }
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                UserLocationObject.lastLocation = p0.lastLocation



                if ( firstTime ) {
                    map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lastLocation.latitude, lastLocation.longitude)))
                    firstTime = false
                }

            }
        }

        createLocationRequest()




        button_send_location.setOnClickListener {

            startActivity<ChatActivity>(
                    AppConstants.USER_NAME to intent.getStringExtra( AppConstants.USER_NAME ),
                    AppConstants.USER_ID to intent.getStringExtra( AppConstants.USER_ID ),
                    RepairConstants.REPAIR_ORDER_ID to intent.getStringExtra( RepairConstants.REPAIR_ORDER_ID ),
                    RepairConstants.REPAIR_ORDER_STATUS to intent.getStringExtra( RepairConstants.REPAIR_ORDER_STATUS ),
                    "USER_LOCATION" to "http://maps.google.com/?q=${UserLocationObject.lastLocation.latitude},${UserLocationObject.lastLocation.longitude}"
            )

        }

    }









    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }


    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 5000
        // 3
        locationRequest.fastestInterval = 2500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    override fun onResume() {
        super.onResume()
        if (locationUpdateState) {
            startLocationUpdates()
        }

    }
























    override fun onMapReady(googleMap: GoogleMap) {
        /*mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/




        map = googleMap

        map.uiSettings.isZoomControlsEnabled = false


        map.setOnCameraMoveListener {
            if (lastLocationMarker != null ) {
                lastLocationMarker!!.remove()
            }
            if (dropPin != null) {
                dropPin.visibility = View.VISIBLE
                dropPin.setImageResource(R.drawable.drop_pin_move)
            }

        }


        map.setOnCameraMoveStartedListener {reason->
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    //Toast.makeText(context, "The user gestured on the map. cameraTracksLocation= $cameraTracksLocation",Toast.LENGTH_SHORT).show()
                    cameraTracksLocation = false
                    /*activity.fab.setImageResource(R.drawable.drop_pin_move)
                    if (lastLocationMarker != null ) {
                        lastLocationMarker!!.remove()
                    }
                    dropPin.visibility = View.VISIBLE
                    dropPin.setImageResource(R.drawable.drop_pin_move)*/
                    //placeMarkerOnMap(map.cameraPosition.target)
                }
                GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION -> {
                    //Toast.makeText(context, "The user tapped something on the map. cameraTracksLocation= $cameraTracksLocation", Toast.LENGTH_SHORT).show()
                    //cameraTracksLocation = true
                    firstTime = false
                    //placeMarkerOnMap(map.cameraPosition.target)
                }
                GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> {
                    //Toast.makeText(context, "The app moved the camera.", Toast.LENGTH_SHORT).show()
                    //placeMarkerOnMap(map.cameraPosition.target)
                }

            }


        }

        map.setOnCameraIdleListener {
            //activity.fab.setImageResource(R.drawable.drop_pin_wave)
            //Toast.makeText(context,"setOnCameraIdleListener", Toast.LENGTH_SHORT ).show()

            if (dropPin != null) dropPin.visibility = View.GONE

            placeMarkerOnMap(map.cameraPosition.target)

        }






        map.setOnMarkerClickListener(this)


        // Add a marker in Sydney and move the camera
        val riyadh = LatLng(24.7253981,46.2620271)
        /*lastLocationMarker = map.addMarker(MarkerOptions().position(riyadh)
                .title("Marker in Sydney")
                .snippet("her is me")
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.memarker))
        )*/
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(riyadh, 2.5f))

        setUpMap()



    }






    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                map.moveCamera( CameraUpdateFactory.newLatLngZoom(currentLatLng, 12.5f) )
            }else{
                val riyadh = LatLng(24.7253981,46.2620271)
                map.moveCamera( CameraUpdateFactory.newLatLngZoom(riyadh, 2.5f) )
            }
        }
    }


    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun placeMarkerOnMap(location: LatLng) {

        // 1
        val markerOptions = MarkerOptions().position(location)
                .title("إلقني هنا")
                .snippet("her is me")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_pin))


        // 2

        if (lastLocationMarker != null ) {

            lastLocationMarker!!.remove()


        }

        lastLocationMarker = map.addMarker(markerOptions)
        lastLocationMarker!!.showInfoWindow()


    }














}





