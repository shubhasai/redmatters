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
import com.google.firebase.ktx.Firebase
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class SendemergencyActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private  lateinit var locationRequest: LocationRequest
    private  val pERMISSION_CODE = 100
    var userid:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sendemergency)
        val client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
            .setProject("6489b94aebd49e04665a")
            .setSelfSigned(true)
        val account = Account(client)
        // For Retrieving Session
        GlobalScope.launch(Dispatchers.IO) {
            val r = try{
                account.getSession("current").apply {
                    userid = this.userId
                    startActivity(Intent(this@SendemergencyActivity, MainActivity::class.java))
                    finish()
                }
            }
            catch (e: AppwriteException){

            }

        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
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
                        sendalert(location)
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
                Toast.makeText(this@SendemergencyActivity,lastLocation.latitude.toString(), Toast.LENGTH_SHORT).show()
                sendalert(lastLocation)

            }
        }
    }
    fun sendalert(location:Location){
        val currentDate = Date()

// Format the date and time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm:ss")

        val formattedDate = dateFormat.format(currentDate)
        val formattedTime = timeFormat.format(currentDate)
        val alert = EmergencyAlert(
            time = formattedTime,
            date = formattedDate,
            latitude = location.latitude.toString(),
            longitude = location.longitude.toString(),
            pid = userid,
            text = "Help Needed"

        )
        GlobalScope.launch(Dispatchers.IO) {
            val client = this@SendemergencyActivity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
                    .setSelfSigned(true)
            } // Your project ID
            val databases = client?.let { Databases(it) }
            val response = databases?.createDocument(
                databaseId = "6489bab012b10cbebe23",
                collectionId = "648a239ba491c0936c62",
                documentId = ID.unique(),
                data = alert
            )
        }
    }
}