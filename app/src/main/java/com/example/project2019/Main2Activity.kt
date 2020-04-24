package com.example.project2019

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.*
import java.io.IOException


class Main2Activity : AppCompatActivity() {

    val client = OkHttpClient()

    companion object {
        const val USER = "user"
        var USERNAME = ""
        var mainActivity2 = false
        var ARG = ""
        var Etis: String? = null
        var FLAG: String? = null
        var MENU: android.view.Menu? = null
        var FLAG_MENU: String? = null
        var CONTEXT: Context? = null
    }

    //Выход из УЗ
    fun exit(view: View) {
        val settings = getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("User", null)
        editor.apply()
        finish()

        Log.d("Exit", "User is Logout")
    }

    override fun onStart() {
        super.onStart()
        mainActivity2 = true
    }

    //Сворачивать приложение при нажатии кнопки "Назад"
    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finishAffinity()
    }

    //Основная функция по созданию Активити_2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        CONTEXT = applicationContext

        if (ARG != "true") {
            ARG = intent.getStringExtra(USER)
        }


        //Отслеживаеие нажатия кнопок навигации (нижнее меню)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(naviListener)

        supportFragmentManager.beginTransaction().replace(R.id.fr_sessions, Sessions()).commit()

        Log.d("Main2Activity", "Main2Activity is create")

    }

    fun connect(url: String, name: String) {
        val formBody = FormBody.Builder()
            .add("name", name)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.code() == 200) {
                    Log.d("MAKES", "OK")
                    Etis = response.body()?.string().toString()
                    FLAG_MENU = "true"
                    runOnUiThread {
                        MENU!!.findItem(R.id.menu_session).setVisible(true)
                    }
                }
                else {
                    Log.d("MAKES", "AGAIN CONNECT")
                    connect(url, name)
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
            }
        })
    }

    private val naviListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {

                var FragmentSelect: Fragment = Sessions()

                when (item.getItemId()) {
                    R.id.navigation_session -> {
                        FragmentSelect = Sessions()
                        this@Main2Activity.title = "Оценки"
                    }
                    R.id.navigation_timetable -> {
                        FragmentSelect = timetable()
                        this@Main2Activity.title = "Расписание"
                    }
                    R.id.navigation_menu -> {
                        FragmentSelect = Menu()
                        this@Main2Activity.title = "Меню"
                    }
                }
                    supportFragmentManager.beginTransaction().replace(R.id.fr_sessions, FragmentSelect).commit()

                return true
            }
        }
}
