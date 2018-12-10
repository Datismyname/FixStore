package com.hadistudios.fixstore

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import com.hadistudios.fixstore.repairshop.ChooseBrandActivity
import com.hadistudios.fixstore.util.FirestoreUtil
import com.hadistudios.fixstore.util.NavigationDrawerSelector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout = drawer_layout_main
        val toolbar= toolbar_main
        val navView = nav_view_main
        val nds = NavigationDrawerSelector(this, drawerLayout)


        setSupportActionBar(toolbar)
        ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close).syncState()
        navView.setNavigationItemSelectedListener(nds.ndl())

        FirestoreUtil.getCurrentUser {
            textView_nav_header_title.text = it.name
        }

        fix_button.setOnClickListener {
            startActivity<ChooseBrandActivity>()
        }

        store_button.setOnClickListener {

        }

    }
}
