package com.hadistudios.fixstore.repairshop

import com.hadistudios.fixstore.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_problem.*

class ProblemItem( val title: String, val description: String, val icon:Int )  : Item(){

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_title.text = title
        viewHolder.textView_description.text = description
        viewHolder.imageView_problem_icon.setImageResource( icon )

    }

    override fun getLayout() = R.layout.item_problem
}