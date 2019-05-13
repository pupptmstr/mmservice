package com.pupptmstr.freefall.matchmakingservice

import com.google.gson.annotations.SerializedName
import java.awt.TrayIcon

data class RespondModel(
    @SerializedName("messageType")
    val messageType: Int,
    @SerializedName("messageText")
    val messageText: String
)