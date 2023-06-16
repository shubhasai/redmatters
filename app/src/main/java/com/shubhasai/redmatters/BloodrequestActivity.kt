package com.shubhasai.redmatters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

class BloodrequestActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private  lateinit var locationRequest: LocationRequest
    private  val pERMISSION_CODE = 100
    var userid:String = ""
    var bloodgroup:String = " "
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bloodrequest)
        val client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
            .setProject("6489b94aebd49e04665a")
            .setSelfSigned(true)
        val account = Account(client)
        // For Retrieving Session
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        GlobalScope.launch(Dispatchers.IO) {
            val r = try{
                account.getSession("current").apply {
                    userid = this.userId
                    getLastLocation()
                }
            }
            catch (e: AppwriteException){

            }

        }
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        //check for the permissions
        if (checkPermissions()){
            //check if location service is enabled
            if (isLocationEnabled()){
                //lets get the location
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        //if location is null we will get new user location
                        //add new location function here
                        getNewLocation()

                    } else {
                        readUserDetails(location)
                    }


                }
            }else Toast.makeText(this,"Please enable the Location Services", Toast.LENGTH_SHORT).show()
        }else RequestPermission()
    }

    //Function to check the user permissions
    private fun checkPermissions() :Boolean{
        return ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    //Function to check if location service of the device is enabled
    private fun isLocationEnabled(): Boolean {
        var locationManager : LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)

    }

    //Function that will allow us to get user permissions
    private fun RequestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),pERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==pERMISSION_CODE){
            if (grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.d("Debug:","You have the permission")
            }
        }
    }

    //Function to get new user location
    @SuppressLint("MissingPermission")
    private fun getNewLocation(){

        locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval=60*1000
        locationRequest.interval = 5*1000
        locationRequest.numUpdates= 2
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }
    //create locationCallback variable
    private val locationCallback = object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation : Location? = p0.lastLocation
            if (lastLocation != null) {
                readUserDetails(lastLocation)

            }
        }
    }
    fun sendalert1(location: Location,bloodgroup:String){
        val currentDate = Date()

// Format the date and time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm:ss")

        val formattedDate = dateFormat.format(currentDate)
        val formattedTime = timeFormat.format(currentDate)
        val alert = BloodData(
            bloodGroup = bloodgroup,
            time = formattedTime,
            date = formattedDate,
            latitude = location.latitude.toString(),
            longitude = location.longitude.toString(),
            pid = userid,
            text = "Help Needed"

        )
        GlobalScope.launch(Dispatchers.IO) {
            val client = this@BloodrequestActivity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
                    .setSelfSigned(true)
            } // Your project ID
            val databases = client?.let { Databases(it) }
            val response = try {
                databases?.createDocument(
                    databaseId = "6489bab012b10cbebe23",
                    collectionId = "648accbe2cf8917934a6",
                    documentId = ID.unique(),
                    data = alert
                ).apply {
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@BloodrequestActivity,"Alert Has Been Sent",Toast.LENGTH_SHORT).show()
                    }
                }
            }catch (e:AppwriteException){
                println("error in send"+e.message)
            }
        }
    }
    fun readUserDetails(location: Location){
        GlobalScope.launch(Dispatchers.IO){
            val client = this@BloodrequestActivity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
            }
            val databases = client?.let { Databases(it) }
            val response = try{
                databases?.getDocument(
                    databaseId = "6489bab012b10cbebe23",
                    collectionId = "648a09afde0d4bfbde7b",
                    documentId = Userinfo.userid,
                )?.apply {
                    val userMap = data
                    val userDetails = userdetails(
                        userid = userMap["userid"] as String,
                        name = userMap["name"] as String,
                        email = userMap["email"] as String,
                        phone = userMap["phone"] as String,
                        gender = userMap["gender"] as String,
                        dob = userMap["dob"] as String,
                        emergencyNumber = userMap["emergencyNumber"] as String,
                        redcoins = (userMap["redcoins"] as Double).toInt(),
                        state = userMap["state"] as String,
                        district = userMap["district"] as String,
                        locality = userMap["locality"] as String,
                        pincode = userMap["pincode"] as String,
                        height = userMap["height"] as String,
                        weight = userMap["weight"] as String,
                        bloodgroup = userMap["bloodgroup"] as String,
                        birthmark = userMap["birthmark"] as String
                    )
                    withContext(Dispatchers.Main){
                        bloodgroup = userDetails.bloodgroup
                        sendalert1(location, userDetails.bloodgroup)
                    }
                }
            }catch (e:AppwriteException){
                println("error"+e.message)
            }
        }

    }
}