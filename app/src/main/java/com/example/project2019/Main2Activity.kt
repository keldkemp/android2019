package com.example.project2019

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment


class Main2Activity : AppCompatActivity() {

    companion object {
        const val USER = "user"
        var USERNAME = ""
        var mainActivity2 = false
        var ARG = ""
    }

    //Выход из УЗ
    fun exit(view: View) {
        val settings = getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("User", null)
        editor.apply()
        finish()
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

        if (ARG != "true")
            ARG = intent.getStringExtra(USER)

        //Отслеживаеие нажатия кнопок навигации (нижнее меню)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(naviListener)

        supportFragmentManager.beginTransaction().replace(R.id.fr_sessions, Sessions()).commit()

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
