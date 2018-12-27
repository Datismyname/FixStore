package com.hadistudios.fixstore.repairshop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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



    private var repairOrderId:String? = null
    private lateinit var shopsSection: Section
    private var shouldInitRecyclerView = true

    private lateinit var respondsListenerRegistration:ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_responded_stores)


                repairOrderId = intent.getStringExtra(RepairConstants.REPAIR_ORDER_ID)


                if (!repairOrderId.isNullOrEmpty())

                    respondsListenerRegistration = FirestoreUtil.addStoresRespondsListener(repairOrderId!!) { items ->


                        button_sort_by_nearest.setOnClickListener {
                            updateRecyclerView( items.sortedBy { orderRespondedStoresItem -> orderRespondedStoresItem.distance } )

                            button_sort_by_nearest.isEnabled = false
                            button_sort_by_rating.isEnabled = true
                            button_sort_by_price.isEnabled = true
                        }

                        button_sort_by_rating.setOnClickListener {
                            updateRecyclerView( items.sortedBy { orderRespondedStoresItem -> orderRespondedStoresItem.repairShop.rating }.asReversed() )

                            button_sort_by_nearest.isEnabled = true
                            button_sort_by_rating.isEnabled = false
                            button_sort_by_price.isEnabled = true

                        }

                        button_sort_by_price.setOnClickListener {
                            updateRecyclerView( items.sortedBy { orderRespondedStoresItem -> orderRespondedStoresItem.repairShopOfferPrice } )

                            button_sort_by_nearest.isEnabled = true
                            button_sort_by_rating.isEnabled = true
                            button_sort_by_price.isEnabled = false
                        }

                        button_sort_by_nearest.performClick()


                    }









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










}
