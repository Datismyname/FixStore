package com.hadistudios.fixstore.repairshop.model

import android.location.Location
import com.google.firebase.firestore.GeoPoint

data class RepairShop(val name:String, val rating:Double?, val location: GeoPoint) {

    constructor(): this("", 0.0, GeoPoint(0.0,0.0))


}