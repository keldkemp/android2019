package com.example.project2019

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.util.Log
import android.widget.Button
import okhttp3.*
import java.io.IOException
import okhttp3.FormBody
import kotlinx.android.synthetic.main.activity_main.*
import com.example.project2019.Main2Activity.Companion.USERNAME
import com.example.project2019.Main2Activity.Companion.ARG
import kotlinx.android.synthetic.main.activity_main.view.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERSISTANT_STORAGE_NAME = "User_aut"
    }

    private val client = OkHttpClient()

    //Основная функция создания Активити
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Тащим из файла имя пользователя
        val settings = getSharedPreferences(PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
        val is_aut = settings.getString("User", null)

        //Если пользователь авторизован, то переходим сразу на Активити_2
        if (is_aut != null) {
            val secondwindow = Intent(this@MainActivity, Main2Activity::class.java)
            secondwindow.putExtra(USERNAME, is_aut)
            USERNAME = is_aut
            ARG = "true"
            startActivity(secondwindow)
        }

        //Создание БД
        val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT ,name TEXT, username TEXT, password TEXT, is_first INTEGER DEFAULT 0, is_aut INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE IF NOT EXISTS makes (id INTEGER PRIMARY KEY AUTOINCREMENT ,discipline TEXT, make TEXT, date TEXT, trem TEXT, user_id INTEGER, teacher TEXT ,FOREIGN KEY (user_id) REFERENCES users(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS makesInTrem (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "discipline TEXT, tema TEXT, type_of_work TEXT, type_of_control TEXT, make TEXT, " +
                "passing_score TEXT, max_score TEXT, date TEXT, teacher TEXT, trem TEXT, user_id INTEGER, " +
                "FOREIGN KEY (user_id) REFERENCES users(id))")
        db.close()

        Log.d("Create_DB_Activity_1", "Create DB, Create 1 activity")

    }

    //Срабатывает по кнопке, авторизация
    fun login(view: View) {
        val btm = view.login

        var username = username.text.toString()
        val password = password.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Заполните оба поля!", Toast.LENGTH_SHORT).show()
            }
            return
        }
        username = username.substring(0,1).toUpperCase() + username.substring(1)
        btm.isClickable = false

        runOnUiThread {
            Toast.makeText(this@MainActivity, "Выполняется вход, пожалуйста, подождите!", Toast.LENGTH_SHORT).show()
        }

        val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

        val query = db.rawQuery("select * from users where username = ?;", arrayOf(username))
        if (query.moveToFirst()) {}
        else {
            db.execSQL("INSERT INTO users (username) VALUES (?)", arrayOf(username))
        }

        db.close()

        Log.d("Login", "Create Or Update USERS")

        connect("https://crmproject2019.herokuapp.com/api/etis/adduser", username, password, btm)
    }

    //Вызывается в методе login, перебрасывает на Активити_2 - если все Ок
    private fun connect(url: String, username: String, password: String, btm: Button) {
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
                if (response.code() == 200) {
                    val settings = getSharedPreferences(PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString("User", username)
                    editor.apply()

                    val user = response.body()?.string()
                    USERNAME = username
                    val secondwindow = Intent(this@MainActivity, Main2Activity::class.java)
                    secondwindow.putExtra(Main2Activity.USER, user)
                    startActivity(secondwindow)
                }
                else{
                    btm.isClickable = true
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Неверно введен пароль/логин", Toast.LENGTH_SHORT).show()
                    }
                }
                }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                btm.isClickable = true
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Сервис недоступен, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}
