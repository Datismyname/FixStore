package com.hadistudios.fixstore

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.hadistudios.fixstore.repairshop.ChooseBrandActivity
import com.hadistudios.fixstore.repairshop.OrderRespondedStoresActivity
import com.hadistudios.fixstore.util.FirestoreUtil
import com.hadistudios.fixstore.util.LocationUtil
import com.hadistudios.fixstore.util.NavigationDrawerSelector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    internal lateinit var fusedLocationClient: FusedLocationProviderClient
    internal lateinit var locationCallback: LocationCallback
    internal lateinit var locationRequest: LocationRequest

    internal var shopLocation: Location? = null
    internal var initShopLocation = true
    internal var locationUpdateState = false



    companion object {
        internal const val LOCATION_PERMISSION_REQUEST_CODE = 1
        internal const val REQUEST_CHECK_SETTINGS = 2

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = drawer_layout_main
        val toolbar= toolbar_main
        val navView = nav_view_main
        val nds = NavigationDrawerSelector(this, drawerLayout)

        LocationUtil.name = "h"
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)


                if (locationResult.lastLocation != null && locationResult.lastLocation.latitude > 0 && locationResult.lastLocation.longitude > 0) {

                    LocationUtil.lastLocation = locationResult.lastLocation

                    Log.e("FIXSTOREee", "lastLocation: ${LocationUtil.lastLocation}")
                    fusedLocationClient.removeLocationUpdates(locationCallback)

                }
            }
        }
        createLocationRequest()


        setSupportActionBar(toolbar)
        ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close).syncState()
        navView.setNavigationItemSelectedListener(nds.ndl())

        FirestoreUtil.getCurrentUser {
            textView_nav_header_title.text = it.name
        }

        fix_button.setOnClickListener {
            startActivity<ChooseBrandActivity>()
        }

        store_button.setOnClickListener {

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



    internal fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 1000
        // 3
        locationRequest.fastestInterval = 500
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
    /*override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }*/

    // 3
    override fun onResume() {
        super.onResume()
        if (locationUpdateState) {
            startLocationUpdates()
        }

    }








}
