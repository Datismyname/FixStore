package com.hadistudios.fixstore.repairshop.model

import java.util.*


data class RepairOrder(
        val brand: String,
        val phoneType: String,
        val problemTitle: String,
        val problemPreDescription: String,
        val problemDescription: String,
        val orderStatus: Int,
        val userId: String?,
        val orderTime: Date?,
        val acceptedStoreName: String?,
        val acceptedStoreId: String?,
        val repairOrderId: String?
        ) {

    constructor(): this( "","","","","",0,null,null,null, null, null)

}