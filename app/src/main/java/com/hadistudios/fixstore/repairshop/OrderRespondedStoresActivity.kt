package com.hadistudios.fixstore.repairshop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.hadistudios.fixstore.AppConstants
import com.hadistudios.fixstore.ChatActivity
import com.hadistudios.fixstore.MainActivity
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.recyclerview.item.OrderRespondedStoresItem
import com.hadistudios.fixstore.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_order_responded_stores.*
import org.jetbrains.anko.*

class OrderRespondedStoresActivity : AppCompatActivity() {

    private var repairOrderId:String? = null
    private lateinit var shopsSection: Section
    private var shouldInitRecyclerView = true

    private lateinit var respondsListenerRegistration:ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_responded_stores)

        repairOrderId = intent.getStringExtra( RepairConstants.REPAIR_ORDER_ID)



            if ( !repairOrderId.isNullOrEmpty() )
                respondsListenerRegistration = FirestoreUtil.addStoresRespondsListener(repairOrderId!!, this::updateRecyclerView)





    }

    override fun onBackPressed() {

        startActivity( intentFor<RepairOrdersHistoryActivity>().newTask().clearTask() )



    }


    private fun updateRecyclerView( items: List<Item> ){

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

            alert("عرض \"${item.repairShopName}\" هو ${item.repairShopPrice} ريال، هل أنت متأكد من هذا العرض؟"){
                positiveButton("نعم") {

                    FirestoreUtil.updateRepairOrder( repairOrderId!!, mapOf( "orderStatus" to  mutableMapOf( "codeName" to "accepted",  "codeNumber" to 2 ) , "acceptedStoreName" to item.repairShopName, "acceptedStoreId" to item.repairShopId ) ){

                        this@OrderRespondedStoresActivity.startActivity<ChatActivity>(

                                AppConstants.USER_NAME to item.repairShopName,
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








}
