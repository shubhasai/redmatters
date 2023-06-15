package com.shubhasai.redmatters

data class EmergencyAlert(
    val date:String ="",
    val time:String = "",
    val latitude:String = "",
    val longitude:String = "",
    val pid:String = "",
    val text:String = "Help Needed",
)
