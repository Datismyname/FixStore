package com.hadistudios.fixstore.repairshop

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import com.google.firestore.v1beta1.DocumentTransform
import com.google.type.Date
import com.hadistudios.fixstore.R
import com.hadistudios.fixstore.util.FirestoreUtil
import kotlinx.android.synthetic.main.activity_problem_description_form.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.startActivity

class ProblemDescriptionFormActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_problem_description_form)

        editText_p_type.validateEmpty()

        editText_problem_description.validateEmpty()


        button_send_order.setOnClickListener {

            when {

                editText_p_type.isTrimTextEmpty() -> editText_p_type.error = "حقل مطلوب"

                editText_problem_description.isTrimTextEmpty() -> editText_problem_description.error = "حقل مطلوب"

                else -> {

                    val progressDialog = indeterminateProgressDialog("يتم ارسال الطلب الآن")

                    val brand = intent.getStringExtra( RepairConstants.BRAND )
                    val problemTitle = intent.getStringExtra( RepairConstants.PROBLEM_TITLE )
                    val phoneType = editText_p_type.text.toString().trim()
                    val problemPreDescription = intent.getStringExtra( RepairConstants.PROBLEM_PREDESCRIPTION )
                    val problemDescription = editText_problem_description.text.toString().trim()

                    Log.e("SERVERVALUEFIRE", "REQUEST_TIME.number: "+DocumentTransform.FieldTransform.ServerValue.REQUEST_TIME.number.toString())
                    Log.e("SERVERVALUEFIRE", "REQUEST_TIME.name: "+DocumentTransform.FieldTransform.ServerValue.REQUEST_TIME.name)
                    Log.e("SERVERVALUEFIRE", "REQUEST_TIME.ordinal: "+DocumentTransform.FieldTransform.ServerValue.REQUEST_TIME.ordinal)
                    Log.e("SERVERVALUEFIRE", "REQUEST_TIME: "+DocumentTransform.FieldTransform.ServerValue.REQUEST_TIME)



                    Log.e("SERVERVALUEFIRE", "REQUEST_TIME: "+DocumentTransform.FieldTransform.ServerValue.REQUEST_TIME)


                    FirestoreUtil.createRepairOrder(brand, phoneType, problemTitle, problemPreDescription, problemDescription )
                    { repairOrderId ->


                        startActivity<OrderRespondedStoresActivity>(RepairConstants.REPAIR_ORDER_ID to repairOrderId )

                        progressDialog.dismiss()

                    }









                }
            }


        }
    }









    //// editText extension functions for validation


    fun EditText.isTrimTextEmpty(): Boolean{
        if ( this.text.toString().trim().isEmpty() )
            return true

        return false
    }

    fun EditText.validateEmpty(){

        this.setOnFocusChangeListener { v, hasFocus ->

            if (hasFocus){
                this.validate( {  !this.isTrimTextEmpty() }, "حقل مطلوب")
            }

        }


    }

    fun EditText.validate(validator: (String) -> Boolean, message: String) {

        this.afterTextChanged {
            this.error = if ( validator( it ) ) null else message
        }
        this.error = if ( validator( this.text.toString() ) ) null else message

    }

    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged( s: Editable? ) {
                afterTextChanged.invoke( s.toString() )
            }

            override fun beforeTextChanged( s: CharSequence?, start: Int, count: Int, after: Int ) { }

            override fun onTextChanged( s: CharSequence?, start: Int, before: Int, count: Int ) { }

        })
    }






}


