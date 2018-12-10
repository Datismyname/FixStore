package com.hadistudios.fixstore

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View

import com.hadistudios.fixstore.model.ImageMessage
import com.hadistudios.fixstore.model.TextMessage
import com.hadistudios.fixstore.model.User
import com.hadistudios.fixstore.util.FirestoreUtil
import com.hadistudios.fixstore.util.StorageUtil

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.hadistudios.fixstore.repairshop.RepairConstants
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.util.*

private const val RC_SELECT_IMAGE = 2

class ChatActivity : AppCompatActivity() {

    private lateinit var currentChannelId : String
    private lateinit var currentUser: User
    private lateinit var otherUserId: String
    private lateinit var orderId: String
    private lateinit var orderStatus: String

    private lateinit var  messagesListenerRegistration: ListenerRegistration
    private var shouldInitRecyclerView = true
    private lateinit var messagesSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = intent.getStringExtra( AppConstants.USER_NAME )

        FirestoreUtil.getCurrentUser {
            currentUser = it
        }


        otherUserId = intent.getStringExtra( AppConstants.USER_ID )
        orderId = intent.getStringExtra( RepairConstants.REPAIR_ORDER_ID )
        orderStatus = intent.getStringExtra( RepairConstants.REPAIR_ORDER_STATUS )

        FirestoreUtil.getOrCreateChatChannel( otherUserId, orderId ){ channelId ->

            currentChannelId = channelId

            messagesListenerRegistration = FirestoreUtil.addChatMessagesListener(channelId, this, this::updateRecyclerView)





            imageView_send.setOnClickListener {

                val messageToSend = TextMessage( editText_message.text.toString(), Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name )

                editText_message.setText("")

                FirestoreUtil.sendMessage( messageToSend, channelId )

            }



            updateOrderStatusUI()


            button_delivery.setOnClickListener {



                when( orderStatus.toInt() ){

                    2 -> {

                        FirestoreUtil.updateRepairOrder(orderId, mapOf("orderStatus" to 3)) {
                            orderStatus = "3"
                            updateOrderStatusUI()


                        }

                    }

                    in 3..4-> {

                        FirestoreUtil.updateRepairOrder( orderId, mapOf("orderStatus" to 6) ){
                            orderStatus = "6"
                            updateOrderStatusUI()


                        }

                    }
//
                    else -> { }



                }



            }

           /* button_delivered.setOnClickListener {

                FirestoreUtil.updateRepairOrder( orderId, mapOf("orderStatus" to 6) ){
                    textView_status.text = "تم استلام الجهاز"


                }

            }*/

            fab_send_image.setOnClickListener {

                val intent = Intent().apply {

                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra( Intent.EXTRA_MIME_TYPES, arrayOf( "image/jpeg", "image/png" ) )

                }
                startActivityForResult(Intent.createChooser( intent, "Select image" ), RC_SELECT_IMAGE)

            }

        }
//
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home -> {
                intent
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if ( requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null ){

            val selectedImagePath = data.data

            val selectedImageBitMap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()

            selectedImageBitMap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes){ imagePath ->

                val messageToSend = ImageMessage( imagePath, Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name )

                FirestoreUtil.sendMessage( messageToSend, currentChannelId )
            }


        }

    }

    private fun updateRecyclerView(messages: List<Item> ){

        fun init(){

            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {

                    messagesSection = Section( messages )
                    this.add( messagesSection )
                }
            }

            shouldInitRecyclerView = false

        }

        fun updateItems() = messagesSection.update( messages )


        if ( shouldInitRecyclerView ){
            init()
        }else{
            updateItems()
        }



        recycler_view_messages.scrollToPosition(recycler_view_messages.adapter!!.itemCount -1 )

    }




    private fun updateOrderStatusUI(){

        when( orderStatus.toInt() ) {

            2 -> { }

            in 3..4 -> {

                textView_status.text = "الجهاز دخل الصيانة"

                button_delivery.text = "تم الإستلام"

            }


            6 -> {

                relativeLayout_message.visibility = View.GONE

                textView_status.text = "تم استلام الجهاز"

                button_delivery.isEnabled = false
                button_delivery.text = "انتهى الطلب"

            }

            else -> { }

        }


    }



}
