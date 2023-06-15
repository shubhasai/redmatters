package com.shubhasai.redmatters

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shubhasai.redmatters.databinding.ActivityRegisterBinding
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterBinding
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1") // Your Appwrite Endpoint
            .setProject("6489b94aebd49e04665a")
        val account = Account(client)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        binding.btnGotologin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val user = try {
                    account.create(
                        userId = ID.unique(),
                        email = binding.etemail.text.toString(),
                        password = binding.etPass.text.toString()
                    )
                } catch (e: AppwriteException) {
                    // Handle the exception if the IO call fails
                    null
                }

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // IO call was successful, do something in the main scope
                        // with the received `user` object
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registered Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        println(user.id+user.email)
                        saveuserdata(user.id,user.email)

                    } else {
                        // IO call failed, handle the error or show an appropriate message
                    }
                }
            }
        }
        setContentView(binding.root)
    }
    fun saveuserdata(id:String,email:String){
        println("called")
        val userdetail = userdetails(userid = id, email = email)
        GlobalScope.launch(Dispatchers.IO) {
            val client = this@RegisterActivity?.let {
                Client(it)
                    .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
                    .setProject("6489b94aebd49e04665a")
                    .setSelfSigned(true)
            } // Your project ID
            val databases = client?.let { Databases(it) }
            val response = try{
                databases?.createDocument(
                    databaseId = "6489bab012b10cbebe23",
                    collectionId = "648a09afde0d4bfbde7b",
                    documentId = id,
                    data = userdetail
                ).apply {
                    withContext(Dispatchers.Main){
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }catch (e:AppwriteException){
                println(e.message.toString())
            }

        }


    }
}