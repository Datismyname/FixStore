package com.hadistudios.fixstore.customview

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.*
import android.widget.PopupWindow
import com.hadistudios.fixstore.R

class FloatingView {
/* Floating view is used to display a custom view for attachments in the chat screen */




companion object {
    lateinit var  popWindow:PopupWindow


    fun onShowPopup(activity: Activity, inflatedView: View) {

        // get device size

        val display: Display = activity.windowManager.defaultDisplay;
        val size = Point()
        display.getSize(size)
        // fill the data to the list items
        // set height depends on the device size
        popWindow = PopupWindow(inflatedView, size.x , ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popWindow.update(size.x/2, 0, size.x- 50, ViewGroup.LayoutParams.WRAP_CONTENT)

        // set a background drawable with rounders corners
        popWindow.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.rect_round_white))
        // make it focusable to show the keyboard to enter in `EditText`
        popWindow.isFocusable = true
        // make it outside touchable to dismiss the popup window
        popWindow.isOutsideTouchable = true

        popWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        // show the popup at bottom of the screen and set some margin at
        // bottom ie,

        popWindow.animationStyle = R.style.popup_window_animation
        popWindow.showAtLocation(activity.currentFocus, Gravity.BOTTOM, 0, 0)

    }

    fun dismissWindow() {

        popWindow.dismiss()
    }
}
}