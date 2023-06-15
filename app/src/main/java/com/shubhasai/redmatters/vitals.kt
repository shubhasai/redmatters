package com.shubhasai.redmatters

data class vitals(
    var height:String = "",
    var weight:String= "",
    var bloodgroup:String="",
    var birthmark:String="",
    var diseases:ArrayList<disease> = ArrayList(),
) {

}
data class disease(
    var nameofdisease:String = ""
)
