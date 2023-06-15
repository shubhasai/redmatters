package com.shubhasai.redmatters

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.shubhasai.redmatters.databinding.FragmentEmergencyBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmergencyFragment : Fragment() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private  lateinit var locationRequest: LocationRequest
    private  val pERMISSION_CODE = 100
    private lateinit var binding:FragmentEmergencyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fusedLocationProviderClient = activity?.let {
            LocationServices.getFusedLocationProviderClient(
                it
            )
        }!!
        binding = FragmentEmergencyBinding.inflate(layoutInflater)
        binding.floatingActionButton2.setOnClickListener {
            // Check if location permission is granted
            getLastLocation()

        }
        return binding.root
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
                        getemergencylist(location)
                        Toast.makeText(activity,"Your Location: "+location.longitude.toString()+","+location.latitude.toString(), Toast.LENGTH_SHORT).show()
                    }


                }
            }else Toast.makeText(activity,"Please enable the Location Services", Toast.LENGTH_SHORT).show()
        }else RequestPermission()
    }

    //Function to check the user permissions
    private fun checkPermissions() :Boolean{
        return activity?.let { ActivityCompat.checkSelfPermission(it,android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    //Function to check if location service of the device is enabled
    private fun isLocationEnabled(): Boolean {
        var locationManager : LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)

    }

    //Function that will allow us to get user permissions
    private fun RequestPermission(){
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),pERMISSION_CODE) }
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
                getemergencylist(lastLocation)
                Toast.makeText(activity,lastLocation.latitude.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }
    fun getemergencylist(loca:Location){
        var emergencylist:ArrayList<EmergencyAlert> = ArrayList()
        GlobalScope.launch(Dispatchers.IO) {
            val client = activity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
            } // Your project ID

            val databases = client?.let { Databases(it) }

            val response = try{
                databases?.listDocuments(
                    databaseId = "6489bab012b10cbebe23",
                    collectionId = "648a239ba491c0936c62",
                )?.apply {
                    for ( doc in this.documents){
                        val eMap = doc.data
                        val emergencyDetails = EmergencyAlert(
                            date = eMap["userid"] as String,
                            time = eMap["userid"] as String,
                            latitude = eMap["userid"] as String,
                            longitude = eMap["userid"] as String,
                            pid = eMap["userid"] as String,
                            text = eMap["userid"] as String,
                        )
                        emergencylist.add(emergencyDetails)
                    }
                    withContext(Dispatchers.Main){
                        for (emergency in emergencylist){
                            val distance = distance(loca.latitude,loca.longitude,emergency.latitude.toDouble(),emergency.longitude.toDouble())
                            if(emergency.date != null && distance<5){
                                val mapView = binding.ambulancemapview
                                mapView.onCreate(null)
                                mapView.onResume()
                                mapView.getMapAsync { googleMap ->
                                    // Initialize the Google Map object
                                    val placeLatLng = LatLng(emergency.latitude.toDouble(), emergency.longitude.toDouble())
                                    val userLatLng = LatLng(loca.latitude, loca.longitude)
                                    val circleOptions = CircleOptions()
                                        .center(userLatLng)
                                        .radius(5.0*1000)
                                        .fillColor(com.shubhasai.redmatters.R.color.royal_blue)
                                    val markerOptions = MarkerOptions()
                                        .position(placeLatLng)
                                        .title(emergency.pid.toString())
                                    googleMap.addMarker(markerOptions)
                                    googleMap.addCircle(circleOptions)
                                    googleMap.setMapStyle(activity?.let {
                                        MapStyleOptions.loadRawResourceStyle(
                                            it,com.shubhasai.redmatters.R.raw.map_style)
                                    })
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15f))
                                }
                            }
                        }

                    }
                }
            } catch (e: AppwriteException){

            }
        }
    }
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of the earth in km
        val dLat = deg2rad(lat2 - lat1)
        val dLon = deg2rad(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val d = R * c // Distance in km
        return d
    }

    fun deg2rad(deg: Double): Double {
        return deg * (Math.PI /180)
    }
}