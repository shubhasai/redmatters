package com.shubhasai.redmatters

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shubhasai.redmatters.databinding.FragmentProfileBinding
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)
        readUserDetails()
        binding.btnSaveVitals.setOnClickListener {
            saveuserdata()
        }
        return binding.root
    }
    fun saveuserdata(){
        val bloodGroup = binding.etBloodGroup.text.toString().uppercase()
        val birthmark = binding.Birthmark.text.toString()
        val height = binding.etHeight.text.toString()
        val weight = binding.etWeight.text.toString()
        val emergencyContact = binding.etemergencycontact.text.toString()
        val Contact = binding.etcontact.text.toString()
        val name = binding.etname.text.toString()
        val email = binding.etemail.text.toString()
        val state = binding.etState.text.toString()
        val district = binding.etDistrict.text.toString()
        val locality = binding.etLocality.text.toString()
        val pincode = binding.etPincode.text.toString()
        val gender = binding.etGender.text.toString().lowercase()
        val dob = binding.etDob.text.toString()
        val hashMap:HashMap<String,Any> = HashMap()
        val userdetails = userdetails(name = name,
            email = email,
            phone = Contact,gender = gender, dob = dob, emergencyNumber = emergencyContact,
            state=state,district=district, locality = locality,pincode = pincode,
            userid = Userinfo.userid,height= height, weight = weight, bloodgroup = bloodGroup, birthmark = birthmark)
        GlobalScope.launch(Dispatchers.IO) {
            val client = activity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
                    .setSelfSigned(true)
            } // Your project ID
            val databases = client?.let { Databases(it) }
            val response = databases?.createDocument(
                databaseId = "6489bab012b10cbebe23",
                collectionId = "648a09afde0d4bfbde7b",
                documentId = Userinfo.userid,
                data = userdetails
            )
        }


    }
    fun readUserDetails(){
        GlobalScope.launch(Dispatchers.IO) {
            val client = activity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
            } // Your project ID

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
                        binding.etname.setText(userDetails.name)
                        binding.etemail.setText(userDetails.email)
                        binding.etcontact.setText(userDetails.phone)
                        binding.etemergencycontact.setText(userDetails.emergencyNumber)
                        binding.etState.setText(userDetails.state)
                        binding.etDistrict.setText(userDetails.district)
                        binding.etLocality.setText(userDetails.locality)
                        binding.etPincode.setText(userDetails.pincode)
                        binding.etGender.setText(userDetails.gender)
                        binding.etDob.setText(userDetails.dob)
                        binding.etBloodGroup.setText(userDetails.bloodgroup)
                        binding.Birthmark.setText(userDetails.birthmark)
                        binding.etHeight.setText(userDetails.height)
                        binding.etWeight.setText(userDetails.weight)
                    }
                }
            } catch (e:AppwriteException){

            }
        }

    }
}