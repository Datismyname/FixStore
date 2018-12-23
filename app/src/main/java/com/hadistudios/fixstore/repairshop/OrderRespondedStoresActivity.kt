package com.hadistudios.fixstore.repairshop

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.firestore.ListenerRegistration
import com.hadistudios.fixstore.AppConstants
import com.hadistudios.fixstore.ChatActivity
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.recyclerview.item.OrderRespondedStoresItem
import com.hadistudios.fixstore.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_order_responded_stores.*
import org.jetbrains.anko.*

class OrderRespondedStoresActivity : AppCompatActivity() {

    internal lateinit var fusedLocationClient: FusedLocationProviderClient
    internal lateinit var locationCallback: LocationCallback
    internal lateinit var locationRequest: LocationRequest
    internal var lastLocation: Location? =null
    internal var shopLocation: Location? = null
    internal var initShopLocation = true
    internal var locationUpdateState = false

    companion object {
        internal const val LOCATION_PERMISSION_REQUEST_CODE = 1
        internal const val REQUEST_CHECK_SETTINGS = 2
    }



    private var repairOrderId:String? = null
    private lateinit var shopsSection: Section
    private var shouldInitRecyclerView = true

    private lateinit var respondsListenerRegistration:ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_responded_stores)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)


                if (locationResult.lastLocation != null && locationResult.lastLocation.latitude > 0 && locationResult.lastLocation.longitude > 0) {
                    lastLocation = locationResult.lastLocation
                    Log.e("FIXSTOREee", "lastLocation: $lastLocation")
                    fusedLocationClient.removeLocationUpdates(locationCallback)









                repairOrderId = intent.getStringExtra(RepairConstants.REPAIR_ORDER_ID)


                if (!repairOrderId.isNullOrEmpty())
                    respondsListenerRegistration = FirestoreUtil.addStoresRespondsListener(repairOrderId!!, lastLocation) { items ->


                        button_sort_by_nearest.setOnClickListener {


                            items.forEach { orderRespondedStoresItem ->


                            }


                            updateRecyclerView(items.sortedBy { orderRespondedStoresItem -> orderRespondedStoresItem.distance }.asReversed())

                        }

                        button_sort_by_rating.setOnClickListener {
                            updateRecyclerView(items.sortedBy { orderRespondedStoresItem -> orderRespondedStoresItem.repairShop.rating })
                        }

                        button_sort_by_price.setOnClickListener {

                            updateRecyclerView(items.sortedBy { orderRespondedStoresItem -> orderRespondedStoresItem.repairShopOfferPrice })

                        }

                        //button_sort_by_nearest.performClick()


                    }
            }
            }
        }
        createLocationRequest()







    }

    override fun onBackPressed() {

        startActivity( intentFor<RepairOrdersHistoryActivity>().newTask().clearTask() )



    }


     private fun updateRecyclerView(items: List<OrderRespondedStoresItem> ){

        fun init(){


            recycler_view_order_respond.apply {
                layoutManager = LinearLayoutManager(this@OrderRespondedStoresActivity)

                adapter = GroupAdapter<ViewHolder>().apply {

                    shopsSection = Section(items)

                    add( shopsSection )

                    setOnItemClickListener( onItemClick )

                }
            }

            shouldInitRecyclerView = false
        }

        fun update() = shopsSection.update(items)



        if ( shouldInitRecyclerView ){
            init()
        }else{
            update()
        }

    }


    private val onItemClick = OnItemClickListener{ item, view ->


        if ( item is OrderRespondedStoresItem ){

            alert("عرض \"${item.repairShop.name}\" هو ${item.repairShopOfferPrice} ريال، هل أنت متأكد من هذا العرض؟"){
                positiveButton("نعم") {

                    FirestoreUtil.updateRepairOrder( repairOrderId!!, mapOf( "orderStatus" to  mutableMapOf( "codeName" to "accepted",  "codeNumber" to 2 ) , "acceptedStoreName" to item.repairShop.name, "acceptedStoreId" to item.repairShopId ) ){

                        this@OrderRespondedStoresActivity.startActivity<ChatActivity>(

                                AppConstants.USER_NAME to item.repairShop.name,
                                AppConstants.USER_ID to item.repairShopId,
                                RepairConstants.REPAIR_ORDER_ID to repairOrderId,
                                RepairConstants.REPAIR_ORDER_STATUS to "2"


                        )


                    }


                }

                negativeButton("لا") {  }

            }.show()


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






}
