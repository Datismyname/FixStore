package com.hadistudios.fixstore.model

data class ChatChannel(val userIds: MutableList<String>) {

    constructor() : this(mutableListOf())

}