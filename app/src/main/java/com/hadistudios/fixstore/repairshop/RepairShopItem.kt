package com.hadistudios.fixstore.repairshop

import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.model.RepairShop
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_repair_shop.*

class RepairShopItem( val repairShop: RepairShop, val repairShopId: String )  : Item(){

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_shop_name.text = repairShop.name
        viewHolder.textView_distance.text = repairShop.rating.toString()
        viewHolder.imageView_rating.setImageResource( chooseRatingIcon( repairShop.rating!! ) )

    }

    override fun getLayout() = R.layout.item_repair_shop



    private fun chooseRatingIcon(rating: Double ): Int{

        when (rating) {

            in 0.0..0.4 -> return R.drawable.ic_five_stars

            in 0.5..0.9 -> return R.drawable.ic_five_stars

            in 1.0..1.4 -> return R.drawable.ic_five_stars

            in 1.5..1.9 -> return R.drawable.ic_five_stars

            in 2.0..2.4 -> return R.drawable.ic_five_stars

            in 2.5..2.9 -> return R.drawable.ic_five_stars

            in 3.0..3.4 -> return R.drawable.ic_five_stars

            in 3.5..3.9 -> return R.drawable.ic_five_stars

            in 4.0..4.4 -> return R.drawable.ic_five_stars

            in 4.5..4.9 -> return R.drawable.ic_rating_45

            5.0 ->  return R.drawable.ic_five_stars

            else -> return R.drawable.ic_five_stars

        }


    }

}