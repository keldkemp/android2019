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
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.IOException
import javax.xml.transform.Templates
import okhttp3.FormBody
import okhttp3.RequestBody
import java.nio.charset.Charset
import android.widget.EditText
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.database.sqlite.SQLiteDatabase
import com.example.project2019.Main2Activity.Companion.USERNAME
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.R.id.edit
import android.R.id.edit


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
            startActivity(secondwindow)
        }

        //Создание БД
        val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT ,name TEXT, username TEXT, password TEXT, is_first INTEGER DEFAULT 0, is_aut INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE IF NOT EXISTS makes (id INTEGER PRIMARY KEY AUTOINCREMENT ,discipline TEXT, make TEXT, date TEXT, trem TEXT, user_id INTEGER, teacher TEXT ,FOREIGN KEY (user_id) REFERENCES users(id))")
        db.close()

        Log.d("Create_DB_Activity_1", "Create DB, Create 1 activity")

    }

    //Срабатывает по кнопке, авторизация
    fun login(view: View) {
        val username = username.text.toString()
        val password = password.text.toString()
        val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

        val query = db.rawQuery("select * from users where username = ?;", arrayOf(username))
        if (query.moveToFirst())
            db.execSQL("UPDATE users SET password = ? WHERE username = ?", arrayOf(password, username))
        else
            db.execSQL("INSERT INTO users (username, password) VALUES (?, ?)", arrayOf(username, password))

        db.close()

        Log.d("Login", "Create Or Update USERS")

        connect("https://crmproject2019.herokuapp.com/api/etis/adduser", username, password)
    }

    //Вызывается в методе login, перебрасывает на Активити_2 - если все Ок
    private fun connect(url: String, username: String, password: String) {
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
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Неверно введен пароль/логин", Toast.LENGTH_SHORT).show()
                    }
                }
                }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Сервис недоступен, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}
