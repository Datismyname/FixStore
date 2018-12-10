package com.hadistudios.fixstore.repairshop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.hadistudios.fixstore.R
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main_problem.*
import org.jetbrains.anko.startActivity

class MainProblemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_problem)

        val listOfProblems = ArrayList<ProblemItem>()

        listOfProblems.add( ProblemItem( "الشاشة", "الشاشة مكسورة أو اللمس لايعمل", R.drawable.brokenscreen ) )
        listOfProblems.add( ProblemItem( "البطارية أو مدخل الشاحن", "البطارية تنقص بسرعة أو لا تشحن", R.drawable.battery ) )
        listOfProblems.add( ProblemItem( "الهاتف لا يعمل", "الهاتف لا يعمل كما يجب أو لا يشتغل أبداً", R.drawable.tools ) )

        recycler_view_problem.layoutManager = LinearLayoutManager( this )

        recycler_view_problem.adapter = GroupAdapter<ViewHolder>().apply {

            add( Section( listOfProblems ) )
            setOnItemClickListener( onItemClick )

        }




    }



    private val onItemClick = OnItemClickListener{ item, view ->

        if ( item is ProblemItem ){

            startActivity<ProblemDescriptionFormActivity>(
                    RepairConstants.BRAND to intent.getStringExtra( RepairConstants.BRAND ),
                    RepairConstants.PROBLEM_TITLE to item.title,
                    RepairConstants.PROBLEM_PREDESCRIPTION to item.description


            )
            finish()

        }

    }


}
