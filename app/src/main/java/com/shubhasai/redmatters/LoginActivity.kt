package com.shubhasai.redmatters

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.shubhasai.redmatters.databinding.ActivityLoginBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnGotoregister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
        val client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1") // Your API Endpoint
            .setProject("6489b94aebd49e04665a")
            .setSelfSigned(true)
        val account = Account(client)
        // For Retrieving Session
        GlobalScope.launch(Dispatchers.IO) {
            val r = try{
                account.getSession("current").apply {
                    Userinfo.userid = this.userId
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
            catch (e:AppwriteException){

            }

        }
        //For Login
        binding.btnLogin.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val response = try{
                    account.createEmailSession(
                        email = binding.etemail.text.toString(),
                        password = binding.etPass.text.toString()
                    ).apply {
                        println(this.toString())
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
                catch (e:AppwriteException){
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            e.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }
}