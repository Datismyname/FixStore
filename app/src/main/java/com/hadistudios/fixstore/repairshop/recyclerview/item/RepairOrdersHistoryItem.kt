package com.hadistudios.fixstore.repairshop.recyclerview.item

import android.graphics.Color
import android.util.Log
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.model.RepairOrder
import com.hadistudios.fixstore.repairshop.model.RepairShop
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_repair_orders_history.*
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.*

class RepairOrdersHistoryItem(val order:RepairOrder): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_phone_type.text = order.phoneType
        viewHolder.textView_problem_title.text = order.problemTitle
        viewHolder.textView_shop_name.text = order.acceptedStoreName
        setOrderTime( viewHolder )
        setStatus( viewHolder )


    }

    override fun getLayout() = R.layout.item_repair_orders_history


    private fun setOrderTime(viewHolder: ViewHolder){
        if (order.orderTime != null) {
            val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
            viewHolder.textView_order_time.text = dateFormat.format(order.orderTime)
        }
    }

    private fun setStatus( viewHolder: ViewHolder ){
         when (order.orderStatus){

             1 -> {
                 viewHolder.textView_order_status.text = "تم تقديم عرض"
                 viewHolder.textView_order_status.textColor = "#FF992C".toColor()
             }
             2 -> {
                 viewHolder.textView_order_status.text = "جاري اصدار فاتورة أولية"
                 viewHolder.textView_order_status.textColor = "#d071f2".toColor()
             }
             3 -> {
                 viewHolder.textView_order_status.text = "في انتظار تسليم الجهاز"
             }
             4 -> {
                 viewHolder.textView_order_status.text = "تم الإنتهاء من الصيانة"

             }
             5 -> {
                 viewHolder.textView_order_status.text = "جاري تسليم الجهاز"

             }
             6 -> {
                 viewHolder.textView_order_status.text = "تم الإنتهاء من الطلب"

             }
             99 -> {
                 viewHolder.textView_order_status.text = "الطلب ملغي"
             }


         }


    }

    fun String.toColor(): Int = Color.parseColor(this)

}