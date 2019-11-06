package com.example.project2019

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import org.json.JSONArray
import org.json.JSONObject

class Main2Activity : AppCompatActivity() {

    companion object {
        const val USER = "user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val user = intent.getStringExtra(USER)
        val userJson = JSONObject(user)
        val user1 = userJson["user"]

        textUsername.text = (user)
    }
}
