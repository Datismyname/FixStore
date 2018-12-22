package com.hadistudios.fixstore.repairshop.fragment


import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.ui.IconGenerator
import com.hadistudios.fixstore.AppConstants
import com.hadistudios.fixstore.ChatActivity
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.RepairShopItem
import com.hadistudios.fixstore.util.FirestoreUtil
import org.jetbrains.anko.startActivity


class FindByMapActivity:Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{


    private lateinit var mView: View
    private lateinit var mMapView: MapView

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var lastLocationMarker: Marker? = null
    var firstTime = true
    var cameraTracksLocation = true

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



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView =  inflater.inflate(R.layout.fragment_find_by_map, container, false)



        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mMapView = mView.findViewById(R.id.map_view_find_by_map)

        if (mMapView != null){


            mMapView.onCreate(null)
            mMapView.onResume()
            mMapView.getMapAsync(this)
            mMapView.setPadding(0,0,0,200)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)



                lastLocation = p0.lastLocation

                trackMyLocation(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }

        createLocationRequest()


    }


    /* override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)



         // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         *//*val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*//*



        *//*fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)



                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }

        createLocationRequest()*//*
    }*/

    override fun onMarkerClick(p0: Marker?): Boolean {

        //cameraTracksLocation = true

        p0!!.showInfoWindow()


        shopsAndMarkersList.forEach{
            if ( it[0] == p0.id ){

                activity!!.startActivity<ChatActivity>(

                        AppConstants.USER_NAME to it[1],
                        AppConstants.USER_ID to it[2]


                )

            }
        }


        return true
    }



    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this.context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity!!,
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
        val client = LocationServices.getSettingsClient(this.activity!!)
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
                    e.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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











    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = false


        map.setOnCameraMoveListener {
            if (lastLocationMarker != null ) {
                lastLocationMarker!!.remove()
            }

        }


        map.setOnCameraMoveStartedListener {reason->
            when (reason) {
                OnCameraMoveStartedListener.REASON_GESTURE -> {
                    Toast.makeText(context, "The user gestured on the map. cameraTracksLocation= $cameraTracksLocation",Toast.LENGTH_SHORT).show()
                    cameraTracksLocation = false
                    /*activity.fab.setImageResource(R.drawable.drop_pin_move)
                    if (lastLocationMarker != null ) {
                        lastLocationMarker!!.remove()
                    }
                    dropPin.visibility = View.VISIBLE
                    dropPin.setImageResource(R.drawable.drop_pin_move)*/
                    //placeMarkerOnMap(map.cameraPosition.target)
                }
                OnCameraMoveStartedListener.REASON_API_ANIMATION -> {
                    Toast.makeText(context, "The user tapped something on the map. cameraTracksLocation= $cameraTracksLocation", Toast.LENGTH_SHORT).show()
                    cameraTracksLocation = true
                    firstTime = false
                    //placeMarkerOnMap(map.cameraPosition.target)
                }
                OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION -> {
                    Toast.makeText(context, "The app moved the camera.", Toast.LENGTH_SHORT).show()
                    //placeMarkerOnMap(map.cameraPosition.target)
                }

            }


        }

        map.setOnCameraIdleListener {
            //activity.fab.setImageResource(R.drawable.drop_pin_wave)
           // Toast.makeText(context,"setOnCameraIdleListener", Toast.LENGTH_SHORT ).show()

          //  placeMarkerOnMap(map.cameraPosition.target)

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

        FirestoreUtil.getRepairShopsByNearest( this::addShopsMarkers )

       // FirestoreUtil.getRepairShops {



            //shopMarker.setRotation(-90)

            /*for (element in it){
                var listOfRepairShopItems: RepairShopItem = element


            }*/

            /*for (i in 0 until it.size){


                toast(it[i].name)

                map.addMarker(MarkerOptions().position(LatLng(it[i].location.latitude,it[i].location.longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap( shopMarker.makeIcon(  it[i].rating.toString() + "\n"  ) ) )
                        //.anchor(shopMarker.anchorU, shopMarker.anchorV)
                ).showInfoWindow()

            }*/


        //}



        setUpMap()

    }

    private val shopsAndMarkersList = mutableListOf<MutableList<String>>()
    private fun addShopsMarkers( repairShopItems: List<RepairShopItem> ){

        val shopMarkerIcon =  IconGenerator( context )
        shopMarkerIcon.setBackground(AppCompatResources.getDrawable( context!!, R.drawable.ic_five_stars ) )

        var shopMarker:Marker


        for (repairShopItem in repairShopItems){

            shopMarker = map.addMarker(MarkerOptions().position(LatLng(repairShopItem.repairShop.location.latitude,repairShopItem.repairShop.location.longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap( shopMarkerIcon.makeIcon(  repairShopItem.repairShop.rating.toString() + "\n"  ) ) )
                    //.anchor(shopMarker.anchorU, shopMarker.anchorV)
            )

            shopMarker.showInfoWindow()

            shopsAndMarkersList.add( mutableListOf( shopMarker.id, repairShopItem.repairShop.name, repairShopItem.repairShopId ) )


        }


    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this.context!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this.activity!!) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                trackMyLocation(currentLatLng)
            }
        }
    }



    private fun placeMarkerOnMap(location: LatLng) {
        Toast.makeText(context,"placeMarkerOnMap function fired", Toast.LENGTH_SHORT ).show()
        //Toast.makeText(context,"cameraTracksLocation = $cameraTracksLocation", Toast.LENGTH_SHORT ).show()
        // 1
        val markerOptions = MarkerOptions().position(location)
                .title("إلقني هنا")
                .snippet("her is me")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_pin))


        // 2

        if (lastLocationMarker != null ) {
            // Toast.makeText(context , "if statment lastLocationMarker " + lastLocationMarker , Toast.LENGTH_LONG).show()
            lastLocationMarker!!.remove()


        }

        lastLocationMarker = map.addMarker(markerOptions)
        lastLocationMarker!!.showInfoWindow()


    }


    fun trackMyLocation(location: LatLng){

        if(cameraTracksLocation) {

            var cameraUpdate = if (firstTime){
                CameraUpdateFactory.newLatLngZoom(location, 14f)
            }else{
                CameraUpdateFactory.newLatLng(location)
            }

            map.animateCamera(cameraUpdate)
        }
    }



/*
    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
        map.animateCamera(cameraUpdate)
        locationManager.removeUpdates(this)
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }*/






}