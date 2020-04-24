package com.example.project2019

import android.content.Context
import android.os.*
import android.util.Log
import android.view.*
import android.view.Menu
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project2019.adapter.MakesAdapterList
import com.example.project2019.classes.makes
import kotlinx.android.synthetic.main.fragment_sessions.view.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class Sessions : Fragment() {

    private val client = OkHttpClient()
    private var FLAG_CHECK_REFRESH: Boolean = false

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        activity?.runOnUiThread(action)
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            Toast.makeText(Main2Activity.CONTEXT, message.obj.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private val refreshHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    //Отображаем Оценки пользователя
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val View: View = inflater.inflate(R.layout.fragment_sessions, container, false)
        val spinner = View.spinner_sessions

        val make_list: List<makes> = setMakes()
        val all_trem: ArrayList<String> = getAllTrem()

        val adapt_spinner: ArrayAdapter<String> = ArrayAdapter(View.context, R.layout.support_simple_spinner_dropdown_item, all_trem)

        val RecyclerView: RecyclerView = View.sessions_recycle
        RecyclerView.layoutManager = LinearLayoutManager(View.context)
        RecyclerView.setAdapter(MakesAdapterList(View.context ,make_list))
        RecyclerView.addItemDecoration(MakesAdapterList.ItemDecor(5))

        adapt_spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapt_spinner
        spinner.setSelection(all_trem.size - 1)

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

                updatesessions(View.context, listener)
                //listener.isRefreshing = false
            }
        })

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val trem = spinner.selectedItem.toString()

                val change_list: List<makes> = MakesAdapterList(View.context, make_list).createListShow(trem)
                RecyclerView.setAdapter(MakesAdapterList(View.context, change_list))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        return View
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Main2Activity.MENU = menu
        inflater.inflate(R.menu.menu_sessions, menu)
        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        if (raw.getInt(5) == 0 && Main2Activity.FLAG_MENU == null)
            menu.findItem(R.id.menu_session).setVisible(false)
        else
            FLAG_CHECK_REFRESH = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var FragmentSelect: Fragment = makes_in_trem()
        when (item.itemId) {
            R.id.menu_session -> {
                FragmentSelect = makes_in_trem()
            }
        }
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.fr_sessions, FragmentSelect).commit()

        return super.onOptionsItemSelected(item)
    }

    private fun getAllTrem(): ArrayList<String> {
        val all_trem: ArrayList<String> = ArrayList()
        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        val trem = db.rawQuery("SELECT DISTINCT trem FROM makes WHERE user_id = ?", arrayOf(raw.getString(0)))

        if (trem.moveToFirst()) {
            do {
                all_trem.add(trem.getString(0))
            }
            while (trem.moveToNext())

            all_trem.add("Оценки за все время")
        }
        return all_trem
    }

    fun setMakes(): List<makes> {
        val makes_list: MutableList<makes> = ArrayList<makes>()

        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))

        if (raw.moveToFirst()) {

            if (raw.getInt(5) == 0 && raw.getInt(4) == 1 && Main2Activity.FLAG_MENU == null)
                Main2Activity().connect("https://crmproject2019.herokuapp.com/api/etis/getmakesintrem", raw.getString(1).toString())


            //Если пользователь заходит в приложение 1й раз
            if (raw.getInt(4) == 0) {
                val Etis = Main2Activity.ARG
                Main2Activity.FLAG = "true"
                val EtisJson = JSONObject(Etis)

                val users = EtisJson.getJSONArray("user")
                val name = users.getJSONObject(0).get("name")

                Main2Activity().connect("https://crmproject2019.herokuapp.com/api/etis/getmakesintrem", name.toString())

                val session_id: String = EtisJson.getJSONObject("cookie").get("session_id").toString()

                db.execSQL("UPDATE users SET name = ? WHERE username = ?", arrayOf(name, Main2Activity.USERNAME))
                val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                val editor = settings.edit()
                editor.putString("session_id", session_id)
                editor.apply()

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
    fun updatesessions(context: Context, listener: SwipeRefreshLayout) {

        val name: String
        val id: Int

        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
        val session_id = settings.getString("session_id", null) ?: ""

        name = raw.getString(1)
        id = raw.getInt(0)

        connect("https://crmproject2019.herokuapp.com/api/etis/updatesession", name, id, context, session_id, listener)
        connectMakes("https://crmproject2019.herokuapp.com/api/etis/updatemakesintrem", name, id, context, session_id, listener)

        db.close()
    }

    private fun updatecookie(context: Context) {
        val username: String = Main2Activity.USERNAME
        val password: String

        val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
        password = settings.getString("password", null).toString()

        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url("https://crmproject2019.herokuapp.com/api/etis/updatecookie")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.code() == 200) {
                    val Etis = response.body()?.string().toString()
                    val EtisJson = JSONObject(Etis)

                    val session_id: String = EtisJson.getJSONObject("cookie").get("session_id").toString()

                    val settings = context.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                    val editor = settings.edit()

                    editor.putString("session_id", session_id)
                    editor.apply()
                }
                else{
                    runOnUiThread {
                        Toast.makeText(context, "Введен старый пароль! Пожалуйста, перезайдите в приложение", Toast.LENGTH_SHORT).show()
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

    //Обновляем оценки пользователя (вызывается в методе connect)
    private fun updatesessionsbd(Etis: String, id: Int, context: Context) {
        val db = context.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

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
        val msg = mHandler.obtainMessage(1, "Оценки успешно обновлены")
        msg.sendToTarget()
        Log.d("sessions", "sessions - OK")
    }

    //Обновляем оценки в Триместре пользователя (вызывается в методе connectMakes)
    private fun updatemakesbd(Etis: String, id: Int, context: Context) {
        val db = context.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

        val EtisJson = JSONObject(Etis)
        val makes = EtisJson.getJSONArray("make")
        val trem = makes.getJSONObject(0).get("trem")

        db.execSQL("DELETE FROM makesInTrem WHERE user_id= ? and trem= ?", arrayOf(id, trem))

        for (objct in 0 until makes.length()) {
            val obj = makes.getJSONObject(objct)
            val discipline = obj.get("discipline").toString()
            val tema = obj.get("tema").toString()
            val type_of_work = obj.get("type_of_work").toString()
            val type_of_control = obj.get("type_of_control").toString()
            val make = obj.get("make").toString()
            val passing_score = obj.get("passing_score").toString()
            val max_score = obj.get("max_score").toString()
            val date = obj.get("date").toString()
            val teacher = obj.get("teacher").toString()
            val trem = obj.get("trem").toString()

            db.execSQL(
                "INSERT INTO makesInTrem (discipline, tema, type_of_work, type_of_control, " +
                        "make, passing_score, max_score, date, teacher, trem, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    discipline,
                    tema,
                    type_of_work,
                    type_of_control,
                    make,
                    passing_score,
                    max_score,
                    date,
                    teacher,
                    trem,
                    id
                )
            )
        }
        db.close()
        val msg = mHandler.obtainMessage(1, "Оценки успешно обновлены")
        msg.sendToTarget()
        Log.d("makesInTrem", "MakesInTrem - OK")
    }

    //Соединение с сайтом (вызывается в методе updatesessions)
    private fun connect(url: String, name: String, id: Int, context: Context, cookie: String, listener: SwipeRefreshLayout) {
        val formBody = FormBody.Builder()
            .add("cookie", cookie)
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
                    activity!!.finish()
                    val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString("User", null)
                    editor.apply()

                    val msg = mHandler.obtainMessage(1, "Пожалуйста, авторизайтесь заново")
                    msg.sendToTarget()

                    refreshHandler.post(object: Runnable {
                        override fun run() {
                            listener.isRefreshing = false
                        }
                    })
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                val msg = mHandler.obtainMessage(1, "Сервис недоступен, попробуйте позже")
                msg.sendToTarget()

                refreshHandler.post(object: Runnable {
                    override fun run() {
                        listener.isRefreshing = false
                    }
                })
            }
        })
    }

    //Соединение с сайтом (вызывается в методе updatesessions)
    private fun connectMakes(url: String, name: String, id: Int, context: Context, cookie: String, listener: SwipeRefreshLayout) {
        val formBody = FormBody.Builder()
            .add("cookie", cookie)
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
                    updatemakesbd(Etis, id, context)

                    refreshHandler.post(object: Runnable {
                        override fun run() {
                            listener.isRefreshing = false
                        }
                    })
                }
                else{
                    activity!!.finish()
                    val settings = activity!!.baseContext.getSharedPreferences(MainActivity.PERSISTANT_STORAGE_NAME, Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString("User", null)
                    editor.apply()

                    val msg = mHandler.obtainMessage(1, "Пожалуйста, авторизайтесь заново")
                    msg.sendToTarget()

                    refreshHandler.post(object: Runnable {
                        override fun run() {
                            listener.isRefreshing = false
                        }
                    })
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("Requests", "Service FAIL. ALARM")
                val msg = mHandler.obtainMessage(1, "Сервис недоступен, попробуйте позже")
                msg.sendToTarget()

                refreshHandler.post(object: Runnable {
                    override fun run() {
                        listener.isRefreshing = false
                    }
                })
            }
        })
    }
}
