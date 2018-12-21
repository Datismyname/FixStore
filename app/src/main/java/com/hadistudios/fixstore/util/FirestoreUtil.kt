package com.hadistudios.fixstore.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.value.ServerTimestampValue
import com.google.firestore.v1beta1.DocumentTransform
import com.hadistudios.fixstore.model.*
import com.hadistudios.fixstore.recyclerview.item.ImageMessageItem
import com.hadistudios.fixstore.recyclerview.item.PersonItem
import com.hadistudios.fixstore.recyclerview.item.TextMessageItem
import com.hadistudios.fixstore.repairshop.RepairConstants
import com.hadistudios.fixstore.repairshop.RepairShopItem
import com.hadistudios.fixstore.repairshop.model.RepairOrder
import com.hadistudios.fixstore.repairshop.model.RepairShop
import com.hadistudios.fixstore.repairshop.recyclerview.item.OrderRespondedStoresItem
import com.hadistudios.fixstore.repairshop.recyclerview.item.RepairOrdersHistoryItem
import com.xwray.groupie.kotlinandroidextensions.Item

object FirestoreUtil {

    private val firestoreInstence: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /* enabling get timestamp from snapshot
    var settings =  FirebaseFirestoreSettings.Builder()
    .setTimestampsInSnapshotsEnabled(true)
    .build()
    firestoreInstence.setFirestoreSettings(settings)*/

    private val currentUserDocRef: DocumentReference get() = firestoreInstence.document("users/${FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("UID is null")}")

    private val chatChannelsCollectionReference = firestoreInstence.collection("chatChannels")

    private val repairShopsCollectionReference = firestoreInstence.collection("repairshops")

    private val repairOrdersCollectionReference = firestoreInstence.collection("repairOrders")


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit){

        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()){

                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "", "", "", mutableListOf())

                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }

            }else{
                onComplete()
            }
        }

    }

    fun updateCurrentUser( name:String = "", bio:String = "", profilePicturePath:String ? = null){

        val userFieldMap = mutableMapOf<String, Any>()

        if ( name.isNotBlank() ) userFieldMap["name"] = name
        if ( bio.isNotBlank() ) userFieldMap["bio"] = bio
        if ( profilePicturePath != null ) userFieldMap["profilePicturePath"] = profilePicturePath

        currentUserDocRef.update( userFieldMap )

    }

    fun getCurrentUser( onComplete: (User) -> Unit ){

        currentUserDocRef.get().addOnSuccessListener {

            onComplete(it.toObject( User::class.java )!!)

        }
    }

    fun getRepairShopsByNearest( onComplete:(List<RepairShopItem>) -> Unit ){

        val repairShops = mutableListOf<RepairShopItem>()

        repairShopsCollectionReference.get().addOnSuccessListener {querySnapshot ->

            querySnapshot.forEach {

                repairShops.add( RepairShopItem( it.toObject( RepairShop::class.java ), it.id ) )

            }

            onComplete(repairShops)
        }

    }

    fun getRepairShops( onComplete: (List<Item>) -> Unit ){

        val items = mutableListOf<Item>()

        repairShopsCollectionReference.orderBy("rating").get().addOnSuccessListener {querySnapshot ->

            querySnapshot.forEach {

                items.add( RepairShopItem( it.toObject( RepairShop::class.java ), it.id ) )

            }

            onComplete(items)

        }
    }


    fun createRepairOrder(
            brand:String, type:String, title:String, preDescription:String, description:String, onComplete: (String) -> Unit){

        val mapOfRepairOrder = mutableMapOf(
                "brand" to brand ,
                "phoneType" to type,
                "problemTitle" to title,
                "problemPreDescription" to preDescription,
                "problemDescription" to description,
                "orderStatus" to  mutableMapOf( "codeName" to "new",  "codeNumber" to 0 ),
                "userId" to currentUserDocRef.id,
                "orderTime" to FieldValue.serverTimestamp()
        )

        val repairOrderDocumentReference = repairOrdersCollectionReference.document()

        repairOrderDocumentReference.set( mapOfRepairOrder ).addOnSuccessListener {

            onComplete( repairOrderDocumentReference.id )

        }


    }

    fun updateRepairOrder( orderId:String, fieldsMap:Map<String,Any>, onComplete: () -> Unit){

        repairOrdersCollectionReference.document( orderId ).update( fieldsMap )

        onComplete()

    }

    fun addAcceptedRepairOrdersListener(  onListen: (List<Item>) -> Unit ): ListenerRegistration{

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        return repairOrdersCollectionReference.whereEqualTo("userId", userId).whereGreaterThan("orderStatus.codeNumber", 0 ).whereLessThanOrEqualTo("orderStatus.codeNumber", 6 )
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if (firebaseFirestoreException != null) {
                        Log.e("FIXSTOREORDER", "stores responds listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    var repairOrder:RepairOrder


                    querySnapshot!!.forEach {

                        repairOrder = RepairOrder(
                                it.getString("brand")!!,
                                it.getString("phoneType")!!,
                                it.getString("problemTitle")!!,
                                it.getString("problemPreDescription")!!,
                                it.getString("problemDescription")!!,
                                it.getLong("orderStatus.codeNumber")!!.toInt(),
                                it.getString("userId"),
                                it.getDate("orderTime"),
                                it.getString("acceptedStoreName"),
                                it.getString("acceptedStoreId"),
                                it.id
                        )


                        items.add( RepairOrdersHistoryItem( repairOrder ) )

                    }

                    onListen(items)

                }

    }

    fun addStoresRespondsListener( orderId:String, onListen: ( List<OrderRespondedStoresItem>) -> Unit ): ListenerRegistration{


        return repairOrdersCollectionReference.document( orderId ).collection("orderOffers")
                .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->

                    if (firebaseFirestoreException != null) {
                        Log.e("FIXSTOREORDER", "stores responds listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<OrderRespondedStoresItem>()
                    var store: Map<*,*>

                    querySnapshot!!.forEach{

                        store = it.get("store") as Map<*, *>

                        items.add( OrderRespondedStoresItem( store["name"].toString(), store["rating"].toString().toDouble(), it.getDouble("price")!!, it.id ) )

                    }

                    onListen(items)

        }


    }

    fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit ) : ListenerRegistration {

        return firestoreInstence.collection("users")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if ( firebaseFirestoreException != null ){
                        Log.e("FIRESTORE", "User listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()

                    querySnapshot!!.documents.forEach {

                        if ( it.id != FirebaseAuth.getInstance().currentUser?.uid ){
                            items.add( PersonItem(it.toObject(User::class.java)!!, it.id, context ) )
                        }

                    }
                    onListen(items)

                }
    }



    fun removeListener( registration: ListenerRegistration) = registration.remove()



    fun getOrCreateChatChannel( otherUserId: String, orderId: String, onComplete:(channelId: String) -> Unit ){

        currentUserDocRef.collection( "engagedChatChannels" ).document( orderId ).get().addOnSuccessListener {

            // if chat channel already exists which mean we already chatting with other user
            if ( it.exists() ){
                onComplete( it["channelId"] as String ) // get the field "channelId" from DocumentSnapshot
                return@addOnSuccessListener
            }

            // otherwise if chat channel doesn't exists create new chat channel

            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

            val newChannel = chatChannelsCollectionReference.document()

            newChannel.set( mapOf( "userIds" to mutableListOf( currentUserId, otherUserId )  , "orderId" to orderId ) )

            // save chat channel id in both users who chat together

            currentUserDocRef
                    .collection( "engagedChatChannels" )
                    .document( orderId )
                    .set( mapOf( "channelId" to newChannel.id, "repairShopId" to otherUserId ) ) // the newChannel.id is the id of channel document inside Firestore

            firestoreInstence.collection("repairshops").document( otherUserId )
                    .collection( "engagedChatChannels" )
                    .document( orderId )
                    .set( mapOf( "channelId" to newChannel.id, "customerId" to currentUserId) )

            onComplete( newChannel.id )


        }

    }


    fun addChatMessagesListener(channelId: String, context: Context, onListen: (List<Item>) -> Unit ) : ListenerRegistration {

        return chatChannelsCollectionReference.document( channelId ).collection("messages")
                .orderBy("time")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    if ( firebaseFirestoreException != null ){
                        Log.e("FIRESTORE", "ChatMessageListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()

                    querySnapshot!!.forEach {

                        if ( it["type"] == MessageType.TEXT ){

                            items.add( TextMessageItem( it.toObject(TextMessage::class.java), context ) )

                        }else{

                            items.add( ImageMessageItem( it.toObject(ImageMessage::class.java), context ) )

                        }

                    }

                    onListen(items)


                }

    }

    fun sendMessage( message: Message, channelId: String ){

        chatChannelsCollectionReference.document( channelId )
                .collection( "messages" )
                .add( message )

    }


    /*************** region FCM ***************/

    fun getFCMRegistrationTokens( onComplete: (tokens: MutableList<String>) -> Unit ){

        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject( User::class.java )!!

            onComplete(user.registrationTokens)

        }

    }

    fun setFCMRegistrationTokens( registrationTokens: MutableList<String> ){

        currentUserDocRef.update( mapOf( "registrationTokens" to registrationTokens ) )

    }


    /*************** end region FCM ***************/




}