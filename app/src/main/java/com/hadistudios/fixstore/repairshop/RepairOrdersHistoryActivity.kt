package com.hadistudios.fixstore.repairshop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.hadistudios.fixstore.AppConstants
import com.hadistudios.fixstore.ChatActivity
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.recyclerview.item.RepairOrdersHistoryItem
import com.hadistudios.fixstore.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_repair_orders_history.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

class RepairOrdersHistoryActivity : AppCompatActivity() {

    private lateinit var shopsSection: Section
    private var shouldInitRecyclerView = true

    private lateinit var respondsListenerRegistration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_orders_history)



            respondsListenerRegistration = FirestoreUtil.addAcceptedRepairOrdersListener(this::updateRecyclerView)





    }


    private fun updateRecyclerView( items: List<Item> ){

        fun init(){

            recycler_view_order_history.apply {
                layoutManager = LinearLayoutManager(this@RepairOrdersHistoryActivity)

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


        if ( item is RepairOrdersHistoryItem ){

            if ( item.order.orderStatus == 1 ){

                startActivity<OrderRespondedStoresActivity>(
                        RepairConstants.REPAIR_ORDER_ID to item.order.repairOrderId
                )

            }else{

                this.startActivity<ChatActivity>(

                        AppConstants.USER_NAME to item.order.acceptedStoreName,
                        AppConstants.USER_ID to item.order.acceptedStoreId,
                        RepairConstants.REPAIR_ORDER_ID to item.order.repairOrderId,
                        RepairConstants.REPAIR_ORDER_STATUS to item.order.orderStatus.toString()


                )

            }




        }

    }




}
