package com.example.project2019

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.os.AsyncTask
import android.telecom.Call
import okhttp3.*
import java.io.File
import java.io.IOException
import javax.xml.transform.Templates
import okhttp3.FormBody
import okhttp3.RequestBody
import java.nio.charset.Charset
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun login(view: View) {
        val username = username.text.toString()
        val password = password.text.toString()

        connect("https://crmproject2019.herokuapp.com/api/", username, password)
    }

    fun connect(url: String, username: String, password: String) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                val user =  response.body()?.string()
                val secondwindow = Intent (this@MainActivity, Main2Activity::class.java)
                secondwindow.putExtra(Main2Activity.USER, user)
                startActivity(secondwindow)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                println("fail")
            }
        })
    }

}
