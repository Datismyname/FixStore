package com.hadistudios.fixstore.util

import android.content.Context
import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.widget.Toast
import com.hadistudios.fixstore.MainActivity
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.RepairOrdersHistoryActivity

class NavigationDrawerSelector (var context: Context, var drawerLayout: DrawerLayout){





    /* override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }*/
    fun ndl(): NavigationView.OnNavigationItemSelectedListener {
        Toast.makeText(context,"ndl()", Toast.LENGTH_SHORT).show()
        return NavigationView.OnNavigationItemSelectedListener {
            Toast.makeText(context,"OnNavigationItemSelectedListener", Toast.LENGTH_SHORT).show()

            // Handle navigation view item clicks here.
            var intent = Intent(context, MainActivity::class.java)

            when (it.itemId) {
                R.id.nav_camera -> {
                    // Handle the camera action
                    intent = Intent(context, MainActivity::class.java)
                }
                R.id.nav_gallery -> {
                    intent = Intent(context, RepairOrdersHistoryActivity::class.java)
                }
                R.id.nav_slideshow -> {
                    //intent = Intent(context, ThirdActivity::class.java)

                }
                R.id.nav_manage -> {

                }
                R.id.nav_share -> {

                }
                R.id.nav_send -> {

                }
            }


            ContextCompat.startActivity(context, intent, null)


            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

}