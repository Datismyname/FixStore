package com.hadistudios.fixstore.repairshop.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.hadistudios.fixstore.R
import kotlinx.android.synthetic.main.activity_find_store.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.hadistudios.fixstore.AppConstants
import com.hadistudios.fixstore.ChatActivity
import com.hadistudios.fixstore.repairshop.RepairConstants
import com.hadistudios.fixstore.repairshop.RepairShopItem
import com.hadistudios.fixstore.repairshop.model.RepairShop
import com.hadistudios.fixstore.util.FirestoreUtil
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.fragment_rating.*
import org.jetbrains.anko.startActivity

class FindByRatingActivity:Fragment(){

    private lateinit var shopsSection: Section
    private var shouldInitRecyclerView = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        FirestoreUtil.getRepairShops( this::updateRecyclerView )

        return inflater.inflate(R.layout.fragment_rating, container, false)
    }



    private val onItemClick = OnItemClickListener{ item, view ->

        if ( item is RepairShopItem ){

            activity!!.startActivity<ChatActivity>(

                    AppConstants.USER_NAME to item.repairShop.name,
                    AppConstants.USER_ID to item.repairShopId


            )

        }

    }



    override fun onDestroy() {
        super.onDestroy()

        shouldInitRecyclerView = true

    }


    private fun updateRecyclerView( items: List<Item> ){

        fun init(){
            recycler_view_shops.apply {
                layoutManager = LinearLayoutManager(this@FindByRatingActivity.context)

                adapter = GroupAdapter<com.xwray.groupie.kotlinandroidextensions.ViewHolder>().apply {

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




}