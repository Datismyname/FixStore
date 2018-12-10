package com.hadistudios.fixstore.repairshop

import com.hadistudios.fixstore.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_brand.*

class BrandItem( val brand: String, val logo:Int )  : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_brand_name.text = brand
        viewHolder.imageView_brand_logo.setImageResource( logo )

    }

    override fun getLayout() = R.layout.item_brand
}