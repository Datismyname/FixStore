package com.hadistudios.fixstore.repairshop

import android.support.design.widget.TabLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.repairshop.fragment.DeliveryActivity
import com.hadistudios.fixstore.repairshop.fragment.FindByMapActivity
import com.hadistudios.fixstore.repairshop.fragment.FindByRatingActivity
import kotlinx.android.synthetic.main.activity_find_store.*

class FindStoreActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_store)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))


        fab.hide()

        container.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 1){
                    this@FindStoreActivity.fab.show()

                }else{
                    this@FindStoreActivity.fab.hide()
                }
                when(position){

                    0 -> {
                        Toast.makeText(this@FindStoreActivity," i'm in Rating Fragment", Toast.LENGTH_SHORT).show()

                    }

                    1 -> {
                        Toast.makeText(this@FindStoreActivity," i'm in maps Fragment", Toast.LENGTH_SHORT).show()
                    }

                    2 -> {
                        Toast.makeText(this@FindStoreActivity," i'm in All Fragment", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_find_store, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            when(position){

                0 -> {
                    return DeliveryActivity()
                }

                1 -> {
                    return FindByMapActivity()
                }

                2 -> {
                    return FindByRatingActivity()
                }

            }

            return null
            //return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when(position){

                0 -> {return "Rating"}

                1 -> { return "Map" }

                2 -> { return "All" }

            }

            return null
        }
    }


}
