package com.example.project2019

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.activity_main2.view.*
import org.json.JSONArray
import org.json.JSONObject

class Main2Activity : AppCompatActivity() {

    companion object {
        const val USER = "user"
    }

    fun exit(view: View) {
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val user = intent.getStringExtra(USER)
        val userJson = JSONObject(user)
        val users = userJson.getJSONArray("user")
        val times = userJson.getJSONArray("time")
        for (i in 0 until users.length()){
            val obj = users.getJSONObject(i)
            val username = obj.get("username").toString()
            textUsername.text = (username)
        }
        for (i in 0 until times.length()) {
            val obj = times.getJSONObject(i)
            var time_start = obj.get("time_of_arrival").toString()
            time_start = time_start.replace("Z", "")
            time_start = time_start.replace("T", " ")
            var time_end = obj.get("time_of_leaving").toString()
            time_end = time_end.replace("Z", "")
            time_end = time_end.replace("T", " ")
            val message = time_start + " - " + time_end
            if (i == 0) {
                textView2.text = (message)
                textView3.background = null
                textView3.text = ""
                textView4.background = null
                textView4.text = ""
                textView5.background = null
                textView5.text = ""
                textView6.background = null
                textView6.text = ""
            }
            if (i == 1) {
                textView3.text = (message)
                textView3.setBackgroundColor(Color.parseColor("#9370DB"))
            }
            if (i == 2) {
                textView4.text = (message)
                textView4.setBackgroundColor(Color.parseColor("#FFEFD5"))
            }
            if (i == 3) {
                textView5.text = (message)
                textView5.setBackgroundColor(Color.parseColor("#3CB371"))
            }
            if (i == 4) {
                textView6.text = (message)
                textView6.setBackgroundColor(Color.parseColor("#FFA07A"))
            }
        }
    }
}
