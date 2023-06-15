package com.shubhasai.redmatters

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.messaging.FirebaseMessaging
import com.shubhasai.redmatters.databinding.ActivityMainBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val REQUEST_FINE_LOCATION_PERMISSION = 1
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION = 2
    private val REQUEST_COURSE_LOCATION_PERMISSION = 3
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navMenu.setOnItemSelectedListener {
            Log.d("Item",it.itemId.toString())
            NavigationUI.onNavDestinationSelected(it,findNavController(binding.navHostFragment.id))
            findNavController(R.id.nav_host_fragment).popBackStack(it.itemId, inclusive = false)
            true
        }
        permission()
        updateTokenId()

    }
    fun updateTokenId(){
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { tokenResult ->
                val token = tokenResult
                GlobalScope.launch(Dispatchers.IO) {
                    val client = Client(this@MainActivity)
                        .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                        .setProject("6489b94aebd49e04665a")
                        .setSelfSigned(true) //
                    val hashmap:HashMap<Any,Any> = HashMap()// Your project ID
                    hashmap.put("fcmToken",token)
                    println(token)
                    println(Userinfo.userid)
                    val databases = client?.let { Databases(it) }
                    val response = try {
                        databases?.updateDocument(
                            databaseId = "6489bab012b10cbebe23",
                            collectionId = "648a09afde0d4bfbde7b",
                            documentId = Userinfo.userid,
                            data = hashmap
                        )
                    }catch (e:AppwriteException){
                        println(e.message)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Error retrieving FCM token", e)
            }
    }
    fun permission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission for fine location is already granted
            // Do something with the location here
        } else {
            // Request permission for fine location
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSION,
            )
        }

        // Check if permission for background location is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission for background location
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION_PERMISSION
            )
        }

        // Check if permission for course location is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission for course location is already granted
            // Do something with the location here
        } else {
            // Request permission for course location
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_COURSE_LOCATION_PERMISSION
            )
        }
    }
}