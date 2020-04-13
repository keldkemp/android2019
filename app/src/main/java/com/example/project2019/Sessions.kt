package com.example.project2019

import android.content.Context
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project2019.classes.makes
import com.example.project2019.adapter.MakesAdapterList
import kotlinx.android.synthetic.main.fragment_sessions.view.*
import kotlinx.android.synthetic.main.makes_list.view.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.FieldPosition


class Sessions : Fragment() {

    private val client = OkHttpClient()

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        activity?.runOnUiThread(action)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    //Отображаем Оценки пользователя
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val View: View = inflater.inflate(R.layout.fragment_sessions, container, false)

        val make_list: List<makes> = setMakes()

        val RecyclerView: RecyclerView = View.sessions_recycle
        RecyclerView.layoutManager = LinearLayoutManager(View.context)
        RecyclerView.setAdapter(MakesAdapterList(View.context ,make_list))
        RecyclerView.addItemDecoration(MakesAdapterList.ItemDecor(5))

        val listener = View.swipe_refresh_makes

        listener.setOnRefreshListener(object: SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {

                val vibrator = View.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val canVibrate: Boolean = vibrator.hasVibrator()
                val milliseconds = 100L

                if (canVibrate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                milliseconds,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        vibrator.vibrate(milliseconds)
                    }
                }

                runOnUiThread {
                    Toast.makeText(View.context, "Начинаю обновлять оценки", Toast.LENGTH_SHORT).show()
                }

                updatesessions(View.context)
                listener.isRefreshing = false
            }
        })

        return View
    }

    fun setMakes(): List<makes> {
        val makes_list: MutableList<makes> = ArrayList<makes>()

        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))

        if (raw.moveToFirst()) {

            //Если пользователь заходит в приложение 1й раз
            if (raw.getInt(4) == 0) {

                val Etis = Main2Activity.USER
                val EtisJson = JSONObject(Etis)
                val users = EtisJson.getJSONArray("user")
                val name = users.getJSONObject(0).get("name")
                db.execSQL("UPDATE users SET name = ? WHERE username = ?", arrayOf(name, Main2Activity.USERNAME))
                val id = raw.getInt(0)
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
                db.execSQL("UPDATE users SET is_first = 1 WHERE username = ?", arrayOf(Main2Activity.USERNAME))
            }

            val makes = db.rawQuery("SELECT * FROM makes WHERE user_id = ?", arrayOf(raw.getString(0)))

            //Заполняем массив оценками и помещаем в адаптер
            if (makes.moveToFirst()) {
                do {
                    makes_list.add(makes(discipline = makes.getString(1), make = makes.getString(2),
                        date = makes.getString(3), teacher = makes.getString(6), trem = makes.getString(4)))
                } while (makes.moveToNext())
            }
        }

        db.close()


        return makes_list
    }

    //Срабатывает по кнопке, обновление оценок
    fun updatesessions(context: Context) {

        val username: String
        val name: String
        val id: Int

        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        username = raw.getString(2)
        name = raw.getString(1)
        id = raw.getInt(0)

        connect("https://crmproject2019.herokuapp.com/api/etis/updatesession", username, name, id, context)

        db.close()
    }

    //Обновляем оценоки пользователя (вызывается в методе connect)
    private fun updatesessionsbd(Etis: String, id: Int, context: Context) {
        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

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
            Toast.makeText(context, "Оценки успешно обновлены", Toast.LENGTH_SHORT).show()
        }

        val fragment_sessions = Sessions()

        val fragmentManager: FragmentManager =activity!!.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(fragment_sessions)
        fragmentTransaction.replace(R.id.fr_sessions, fragment_sessions)

        fragmentTransaction.commit()
    }

    //Соединение с сайтом (вызывается в методе updatesessions)
    private fun connect(url: String, username: String, name: String, id: Int, context: Context) {
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
                    val Etis = response.body()?.string().toString()
                    updatesessionsbd(Etis, id, context)
                }
                else{
                    runOnUiThread {
                        Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                runOnUiThread {
                    Toast.makeText(context, "Сервис недоступен, попробуйте позже", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
