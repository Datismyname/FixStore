package com.hadistudios.fixstore.repairshop.recyclerview.item

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.model.RepairShop
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_order_responded_stores.*

class OrderRespondedStoresItem(val repairShop: RepairShop, val repairShopOfferPrice:Double, val repairShopId: String ) : Item(){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? =null
    private var shopLocation: Location? = null
    private var initShopLocation = true
    private var locationUpdateState = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }
    var distance: String = ""

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                lastLocation = locationResult.lastLocation

                if ( initShopLocation ){
                    shopLocation = locationResult.lastLocation
                    initShopLocation = false
                }

                if ( shopLocation != null ) {
                    shopLocation!!.latitude = repairShop.location.latitude
                    shopLocation!!.longitude = repairShop.location.longitude
                }

                if ( lastLocation != null )
                    distance = lastLocation!!.distanceTo(shopLocation).toString()


            }
        }
        createLocationRequest()

    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_shop_name.text = repairShop.name
        viewHolder.textView_price.text = repairShopOfferPrice.toString() + " ريال "
        viewHolder.textView_distance.text = distance
        viewHolder.imageView_rating.setImageResource( chooseRatingIcon( repairShop.rating ) )

    }

    override fun getLayout() = R.layout.item_order_responded_stores



    private fun chooseRatingIcon(rating: Double? ): Int{

        when (rating!!) {

            in 0.0..0.4 -> return R.drawable.ic_five_stars

            in 0.5..0.9 -> return R.drawable.ic_five_stars

            in 1.0..1.4 -> return R.drawable.ic_five_stars

            in 1.5..1.9 -> return R.drawable.ic_five_stars

            in 2.0..2.4 -> return R.drawable.ic_five_stars

            in 2.5..2.9 -> return R.drawable.ic_five_stars

            in 3.0..3.4 -> return R.drawable.ic_five_stars

            in 3.5..3.9 -> return R.drawable.ic_five_stars

            in 4.0..4.4 -> return R.drawable.ic_five_stars

            in 4.5..4.9 -> return R.drawable.ic_rating_45

            5.0 ->  return R.drawable.ic_five_stars

            else -> return R.drawable.ic_five_stars

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


    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    }*/



}