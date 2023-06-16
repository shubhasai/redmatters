package com.shubhasai.redmatters
data class userdetails(
    var userid:String = " ",
    var name:String = " ",
    var email:String= " ",
    var phone:String=" ",
    var gender:String=" ",
    var dob:String=" ",
    var emergencyNumber: String=" ",
    var redcoins:Int = 0,
    var state:String = " ",
    var fcmToken:String = " ",
    var district:String = " ",
    var locality:String= " ",
    var pincode:String=" ",
    var height:String = " ",
    var weight:String= " ",
    var bloodgroup:String=" ",
    var birthmark:String=" ",
)
data class address(
    var state:String = "",
    var district:String = "",
    var locality:String= "",
    var pincode:String="",
)