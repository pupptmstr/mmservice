package com.pupptmstr.freefall.matchmakingservice

import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val userName: String,
    @SerializedName("level")
    val level: Int,
    @SerializedName("clanName")
    val clanName: String,
    @SerializedName("kill")
    val kill: Int,
    @SerializedName("death")
    val death: Int,
    @SerializedName("clearDamage")
    val clearDamage: Int,
    @SerializedName("clearDefence")
    val clearDefence: Int,
    @SerializedName("additionalDamage")
    val additionalDamage: Int,
    @SerializedName("additionalDefence")
    val additionalDefence: Int,
    @SerializedName("fullDamage")
    val fullDamage: Int,
    @SerializedName("fullDefence")
    val fullDefence: Int,
    @SerializedName("health")
    val health: Int,
    @SerializedName("onBodyHelmetId")
    val onBodyHelmetId: Int,
    @SerializedName("onBodySwordId")
    val onBodySwordId: Int,
    @SerializedName("onBodyShoeId")
    val onBodyShoeId: Int,
    @SerializedName("onBodyArmourId")
    val onBodyArmourId: Int,
    @SerializedName("firstSlotHelmetId")
    val firstSlotHelmetId: Int,
    @SerializedName("firstSlotSwordId")
    val firstSlotSwordId: Int,
    @SerializedName("firstSlotShoeId")
    val firstSlotShoeId: Int,
    @SerializedName("firstSlotArmourId")
    val firstSlotArmourId: Int,
    @SerializedName("secondSlotHelmetId")
    val secondSlotHelmetId: Int,
    @SerializedName("secondSlotSwordId")
    val secondSlotSwordId: Int,
    @SerializedName("secondSlotShoeId")
    val secondSlotShoeId: Int,
    @SerializedName("secondSlotArmourId")
    val secondSlotArmourId: Int,
    @SerializedName("date")
    val  date: String,
    @SerializedName("token")
    val token: String
)