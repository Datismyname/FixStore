package com.hadistudios.fixstore.util

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest

object LocationUtil {
    internal lateinit var locationCallback: LocationCallback

    var lastLocation = Location("last location")

    lateinit var name:String



}