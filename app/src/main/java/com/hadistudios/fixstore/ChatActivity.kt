package com.hadistudios.fixstore

import android.app.Activity
import android.content.Context
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
import com.hadistudios.fixstore.repairshop.RepairOrdersHistoryActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream
import java.util.*
import com.hadistudios.fixstore.customview.FloatingView
import android.widget.LinearLayout
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Build
import android.support.v4.content.ContextCompat.getSystemService
import android.view.LayoutInflater
import android.view.ViewAnimationUtils
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import com.google.android.gms.location.places.ui.PlacePicker
import com.hadistudios.fixstore.recyclerview.item.TextMessageItem
import com.hadistudios.fixstore.repairshop.MapActivity
import com.xwray.groupie.OnItemClickListener

private const val RC_PLACE_PICKER = 1
private const val RC_SELECT_IMAGE = 2
private const val RC_CAPTURE_IMAGE = 3

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
        updateOrderStatusUI()

        FirestoreUtil.getOrCreateChatChannel( otherUserId, orderId ){ channelId ->


            currentChannelId = channelId

            messagesListenerRegistration = FirestoreUtil.addChatMessagesListener(channelId, this, this::updateRecyclerView)





            imageView_send.setOnClickListener {

                val messageToSend = TextMessage( editText_message.text.toString(), Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name )

                editText_message.setText("")

                FirestoreUtil.sendMessage( messageToSend, channelId )

            }




            toolbar_chat_first_layer.post {

                val toolbarViewWidth = toolbar_chat_first_layer.width.toFloat()


                toolbar_chat_second_layer.visibility = View.VISIBLE

                //now move it to the right side out of the screen
                toolbar_chat_second_layer.animate()
                        .translationX( 2*toolbarViewWidth )
                        .setDuration(0)



                button_delivery.setOnClickListener {



                    when( orderStatus.toInt() ){

                        2 -> {

                            FirestoreUtil.updateRepairOrder(orderId, mapOf("orderStatus" to  mutableMapOf( "codeName" to "wait for delivery",  "codeNumber" to 4) ) ) {
                                orderStatus = "4"

                                // move first toolbar to the left to be out of the screen
                                toolbar_chat_first_layer.animate()
                                        .translationXBy(-1 * toolbarViewWidth  )
                                        .setDuration(500)

                                // move second toolbar to the left to be shown on the screen
                                toolbar_chat_second_layer.animate()
                                        .translationX(0.0f)
                                        .setDuration(500)


                            }

                        }

                        in 3..4-> {

                            FirestoreUtil.updateRepairOrder( orderId, mapOf("orderStatus" to  mutableMapOf( "codeName" to "delivered",  "codeNumber" to 6 ) ) ){
                                orderStatus = "6"
                                updateOrderStatusUI()


                            }

                        }
//
                        else -> { }



                    }



                }

            }


           /* button_delivered.setOnClickListener {

                FirestoreUtil.updateRepairOrder( orderId, mapOf("orderStatus" to 6) ){
                    textView_status.text = "تم استلام الجهاز"


                }

            }*/

            fab_send_image.setOnClickListener {

                val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                // inflate the custom popup layout
                val inflatedView: View

                inflatedView = layoutInflater.inflate(R.layout.activity_chat_attachment_popup_dialog, null, false)


                val layoutGallery = inflatedView.findViewById<View>(R.id.layoutGallery) as LinearLayout
                val layoutPhoto = inflatedView.findViewById<View>(R.id.layoutPhoto) as LinearLayout
                val layoutVideo = inflatedView.findViewById<View>(R.id.layoutVideo) as LinearLayout

                layoutGallery.setOnClickListener {

                    val intent = Intent().apply {

                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                        putExtra( Intent.EXTRA_MIME_TYPES, arrayOf( "image/jpeg", "image/png" ) )

                    }
                    startActivityForResult(Intent.createChooser( intent, "Select image" ), RC_SELECT_IMAGE)
                    FloatingView.dismissWindow()


                }

                layoutPhoto.setOnClickListener {

                    val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)

                    val permissions = arrayOf("android.permission.CAMERA")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, RC_CAPTURE_IMAGE)
                    }

                    startActivityForResult(intent, RC_CAPTURE_IMAGE)

                    FloatingView.dismissWindow()

                }


                layoutVideo.setOnClickListener {

                    val builder = PlacePicker.IntentBuilder()

                    startActivityForResult( builder.build(this), RC_PLACE_PICKER )

                }

                FloatingView.onShowPopup(this, inflatedView)



                /*relativeLayout_attachment.animate()
                        .translationY(0.0f)
                        .duration = 500*/





            }

        }

    }



    override fun onBackPressed() {
        startActivity( intentFor<RepairOrdersHistoryActivity>().newTask().clearTask() )
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

        if ( requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null  ){

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


        }else if( requestCode == RC_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.extras != null ){

            if ( data.extras!!.get("data") != null ){


                val selectedImageBitMap = data.extras!!.get("data") as Bitmap

                val outputStream = ByteArrayOutputStream()

                selectedImageBitMap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

                val selectedImageBytes = outputStream.toByteArray()

                StorageUtil.uploadMessageImage(selectedImageBytes){ imagePath ->

                    val messageToSend = ImageMessage( imagePath, Calendar.getInstance().time,
                            FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name )

                    FirestoreUtil.sendMessage( messageToSend, currentChannelId )
                }




            }

        }else if ( requestCode == RC_PLACE_PICKER && resultCode == Activity.RESULT_OK  ){

            val place = PlacePicker.getPlace( this, data )
            val locationUrl = "http://www.google.com/maps/search/?api=1&query=${place.latLng.latitude}%2C${place.latLng.longitude}"

                val messageToSend = TextMessage( locationUrl , Calendar.getInstance().time,
                        FirebaseAuth.getInstance().currentUser!!.uid, otherUserId, currentUser.name )

                FirestoreUtil.sendMessage( messageToSend, currentChannelId )



        }



        }

    private fun updateRecyclerView(messages: List<Item> ){

        fun init(){

            recycler_view_messages.apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {

                    messagesSection = Section( messages )

                    add( messagesSection )



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




        recycler_view_messages.apply {
            layoutManager!!.smoothScrollToPosition(this, null, adapter!!.itemCount )

        }

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
