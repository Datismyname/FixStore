package com.hadistudios.fixstore.model

import java.util.*

interface Message {

    val time: Date
    val senderId: String
    val reciptientId: String
    val senderName: String
    val type: String

}

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

