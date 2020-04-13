package com.example.project2019

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.project2019.Main2Activity.Companion.USERNAME
import kotlinx.android.synthetic.main.fragment_timetable.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Executable
import java.net.URLEncoder


class Main2Activity : AppCompatActivity() {

    private val client = OkHttpClient()

    companion object {
        const val USER = "user"
        var USERNAME = ""
        var mainActivity2 = false
    }

    //Выход из УЗ
    fun exit(view: View) {
        val settings = getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putString("User", null)
        editor.apply()
        finishAffinity()
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

    //Смена пароля на сайте (вызывается в методе changepassword)
    private fun changepassworddb(username: String, old_password: String, new_password: String) {
        //TODO: Вызов АПИ

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("old_password", old_password)
            .add("new_password", new_password)
            .build()
        val request = Request.Builder()
            .url("https://crmproject2019.herokuapp.com/api/etis/updatepassword")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.code() == 200) {
                    val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
                    db.execSQL("UPDATE users SET password = ? WHERE username = ?", arrayOf(new_password, username))
                    db.close()

                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "Пароль изменен", Toast.LENGTH_SHORT).show()
                    }
                }
                else if (response.code() == 403){
                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "Введен неверно старый пароль", Toast.LENGTH_SHORT).show()
                    }
                }
                else if (response.code() == 402){
                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "В данный момент пароль изменить нельзя", Toast.LENGTH_SHORT).show()
                    }
                }
                else if (response.code() == 401) {
                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "Вы не заполнили одно из полей", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "Неизвестная ошибка", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                runOnUiThread {
                    Toast.makeText(this@Main2Activity, "Сервис недоступен, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //Срабатывает по кнопке, смена пароля
    fun changepassword(view: View) {

        val username: String = USERNAME
        var old_password: String
        var new_password: String

        val layout = LayoutInflater.from(this)
        val alert_dialog_view = layout.inflate(R.layout.alert_dialog_update_password, null)

        val mDialogBuilder = AlertDialog.Builder(this)

        mDialogBuilder.setView(alert_dialog_view)

        val old_password_view: EditText = alert_dialog_view.findViewById(R.id.old_password)
        val new_password_view: EditText = alert_dialog_view.findViewById(R.id.new_password)

        mDialogBuilder
            .setCancelable(false)
            .setPositiveButton("Сменить пароль"
            ) { dialog, _ ->
            //Вызываем функцию по смене пароля
                old_password = old_password_view.text.toString()
                new_password = new_password_view.text.toString()

                changepassworddb(username, old_password, new_password)

                dialog.cancel()
            }
            .setNegativeButton("Отмена",
                object:DialogInterface.OnClickListener {
                    override fun onClick(dialog:DialogInterface, id:Int) {
                        dialog.cancel()
                    }
                })
        //Создаем AlertDialog:
        val alertDialog = mDialogBuilder.create()

        //и отображаем его:
        alertDialog.show()

    }

    //Срабатывает по кнопке, обновление оценок
    fun updatesessions(view: View) {

        val username: String
        val name: String
        val id: Int

        val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(USERNAME))
        raw.moveToFirst()

        username = raw.getString(2)
        name = raw.getString(1)
        id = raw.getInt(0)

        connect("https://crmproject2019.herokuapp.com/api/etis/updatesession", username, name, id)

        db.close()
    }

    //Обновляем оценоки пользователя (вызывается в методе connect)
    private fun updatesessionsbd(Etis: String, id: Int) {
        val db = baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

        db.execSQL("DELETE FROM makes WHERE user_id= ?", arrayOf(id))

        val EtisJson = JSONObject(Etis)
        val makes = EtisJson.getJSONArray("makes")

        for (objct in 0 until makes.length()) {
            val obj = makes.getJSONObject(objct)
            val discipline = obj.get("discipline").toString()
            val make = obj.get("make").toString()
            val date = obj.get("date").toString()
            val teacher = obj.get("teacher").toString()
            val trem = obj.get("trem").toString()

            db.execSQL(
                "INSERT INTO makes (discipline, make, date, teacher, trem, user_id) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(discipline, make, date, teacher, trem, id)
            )
        }
        db.close()

        runOnUiThread {
            Toast.makeText(this@Main2Activity, "Оценки успешно обновлены", Toast.LENGTH_SHORT).show()
        }
    }

    //Соединение с сайтом (вызывается в методе updatesessions)
    private fun connect(url: String, username: String, name: String, id: Int) {
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("name", name)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.code() == 200) {
                    val settings = getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString("User", username)
                    editor.apply()
                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "Пожалуйста, подождите", Toast.LENGTH_SHORT).show()
                    }
                    val Etis = response.body()?.string().toString()
                    updatesessionsbd(Etis, id)
                }
                else{
                    runOnUiThread {
                        Toast.makeText(this@Main2Activity, "Неизвестная ошибка", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                runOnUiThread {
                    Toast.makeText(this@Main2Activity, "Сервис недоступен, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //Основная функция по созданию Активити_2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val fragment_sessions = Sessions()
        val fragment_timetable = timetable()
        val fragment_menu = Menu()

        //Отслеживаеие нажатия кнопок навигации (нижнее меню)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener(
            object : BottomNavigationView.OnNavigationItemSelectedListener {
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    val layout: LinearLayout = findViewById(R.id.linear_fragment)
                    when (item.getItemId()) {
                        R.id.navigation_session -> {
                            layout.visibility = LinearLayout.VISIBLE

                            val fragmentManager: FragmentManager = supportFragmentManager
                            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.remove(fragment_sessions)
                            fragmentTransaction.replace(R.id.fr_sessions, fragment_sessions)

                            this@Main2Activity.title = "Оценки"

                            fragmentTransaction.commit()
                        }
                        R.id.navigation_timetable -> {
                            layout.visibility = LinearLayout.VISIBLE

                            val fragmentManager: FragmentManager = supportFragmentManager
                            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.fr_sessions, fragment_timetable)

                            this@Main2Activity.title = "Расписание"

                            fragmentTransaction.commit()
                        }
                        R.id.navigation_menu -> {
                            layout.visibility = LinearLayout.VISIBLE

                            val fragmentManager: FragmentManager = supportFragmentManager
                            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.fr_sessions, fragment_menu)

                            this@Main2Activity.title = "Меню"

                            fragmentTransaction.commit()

                        }
                    }
                    return true
                }
            })

    }
}
