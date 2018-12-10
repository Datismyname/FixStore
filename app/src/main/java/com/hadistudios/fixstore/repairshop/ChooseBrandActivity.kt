package com.hadistudios.fixstore.repairshop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.hadistudios.fixstore.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_choose_brand.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity

class ChooseBrandActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_brand)

        val listOfBrands = ArrayList<BrandItem>()

        listOfBrands.add( BrandItem( "آبل", R.drawable.apple ) )
        listOfBrands.add( BrandItem( "سامسونج", R.drawable.samsung ) )
        listOfBrands.add( BrandItem( "هواوي", R.drawable.huawei ) )

        recycler_view_brands.apply {
            layoutManager = LinearLayoutManager(this@ChooseBrandActivity)
            adapter = GroupAdapter<ViewHolder>().apply {
                add( Section( listOfBrands ) )
                setOnItemClickListener( onItemClick )
            }
        }

    }


    private val onItemClick = OnItemClickListener{ item, view ->

        if ( item is BrandItem ){

            startActivity<MainProblemActivity>( RepairConstants.BRAND to item.brand )

        }

    }


}
