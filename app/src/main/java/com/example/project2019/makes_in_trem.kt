package com.example.project2019

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project2019.adapter.MakesInTremAdapterList
import com.example.project2019.classes.MakesInTrem
import kotlinx.android.synthetic.main.fragment_makes_in_trem.view.*
import okhttp3.*
import org.json.JSONObject
import java.lang.NumberFormatException


class makes_in_trem : Fragment() {

    private val client = OkHttpClient()

    companion object {
        var flag_discipline = "Все предметы"
    }

    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            Toast.makeText(Main2Activity.CONTEXT, message.obj.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val View: View = inflater.inflate(R.layout.fragment_makes_in_trem, container, false)

        val spinner = View.spinner_makes
        val spinner_discipline = View.spinner_discipline

        val RecyclerView: RecyclerView = View.makes_in_trem_recycle
        val make_list: List<MakesInTrem> = setMakes()
        val all_trem: ArrayList<String> = getAllTrem()
        var all_discipline_in_trem: ArrayList<String> = getAllDisciplineInCurrentTrem(all_trem[all_trem.size-1])

        val change_list: List<MakesInTrem> = MakesInTremAdapterList(View.context, make_list).createListShow(all_trem[all_trem.size-1], all_discipline_in_trem[all_discipline_in_trem.size-1])

        val adapt: ArrayAdapter<String> = ArrayAdapter(View.context, R.layout.support_simple_spinner_dropdown_item, all_trem)
        var adapt_discipline: ArrayAdapter<String> = ArrayAdapter(View.context, R.layout.support_simple_spinner_dropdown_item, all_discipline_in_trem)

        adapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapt
        spinner.setSelection(all_trem.size - 1)

        adapt_discipline.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_discipline.adapter = adapt_discipline
        spinner_discipline.setSelection(all_discipline_in_trem.size - 1)

        RecyclerView.layoutManager = LinearLayoutManager(View.context)
        RecyclerView.setAdapter(MakesInTremAdapterList(View.context, change_list))
        RecyclerView.addItemDecoration(MakesInTremAdapterList.ItemDecor(5))

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val trem = spinner.selectedItem.toString()
                all_discipline_in_trem = getAllDisciplineInCurrentTrem(trem)
                adapt_discipline = ArrayAdapter(View.context, R.layout.support_simple_spinner_dropdown_item, all_discipline_in_trem)
                spinner_discipline.adapter = adapt_discipline
                spinner_discipline.setSelection(all_discipline_in_trem.size - 1)
                val discipline = spinner_discipline.selectedItem.toString()

                val change_list: List<MakesInTrem> = MakesInTremAdapterList(View.context, make_list).createListShow(trem, discipline)
                RecyclerView.setAdapter(MakesInTremAdapterList(View.context, change_list))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        spinner_discipline.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val trem = spinner.selectedItem.toString()
                val discipline = spinner_discipline.selectedItem.toString()
                flag_discipline = discipline

                val change_list: List<MakesInTrem> = MakesInTremAdapterList(View.context, make_list).createListShow(trem, discipline)
                RecyclerView.setAdapter(MakesInTremAdapterList(View.context, change_list))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }


        return View
    }

    private fun getAllDisciplineInCurrentTrem(CurentTrem: String): ArrayList<String> {
        val all_discipline: ArrayList<String> = ArrayList()
        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        val disciplines = db.rawQuery("SELECT DISTINCT discipline FROM makesInTrem WHERE user_id = ? and trem = ?", arrayOf(raw.getString(0), CurentTrem))

        if (disciplines.moveToFirst()) {
            do {
                all_discipline.add(disciplines.getString(0))
            } while (disciplines.moveToNext())
            all_discipline.add("Все предметы")
        }

        return  all_discipline
    }

    private fun getAllTrem(): ArrayList<String> {
        val all_trem: ArrayList<String> = ArrayList()
        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        val trem = db.rawQuery("SELECT DISTINCT trem FROM makesInTrem WHERE user_id = ?", arrayOf(raw.getString(0)))

        if (trem.moveToFirst()) {
            do {
                all_trem.add(trem.getString(0))
            }
            while (trem.moveToNext())
        }
        return all_trem
    }

    private fun setMakes(): List<MakesInTrem> {
        val makes_list: MutableList<MakesInTrem> = ArrayList<MakesInTrem>()
        val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)
        val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
        raw.moveToFirst()

        if (Main2Activity.FLAG == "true" || raw.getInt(5) == 0) {
            Main2Activity.ARG = "ok"
            Main2Activity.FLAG = "false"

            val id = raw.getInt(0)

            val EtisJson = JSONObject((Main2Activity.Etis).toString())

            val makes = EtisJson.getJSONArray("makes")

            for (objct in 0 until makes.length()) {
                val obj = makes.getJSONObject(objct)
                val discipline = obj.get("discipline").toString()
                val tema = obj.get("tema").toString()
                val type_of_work = obj.get("type_of_work")
                val type_of_control = obj.get("type_of_control")
                val make = obj.get("make").toString()
                val passing_score = obj.get("passing_score")
                val max_score = obj.get("max_score")
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
            Log.d("MAKES", "DB - OK")
            db.execSQL("UPDATE users SET is_aut = 1 WHERE username = ?", arrayOf(Main2Activity.USERNAME))
        }

        val makes =
            db.rawQuery("SELECT * FROM makesInTrem WHERE user_id = ?", arrayOf(raw.getString(0)))

        var count: Double = 0.0
        var i: Int = 0

        //Заполняем массив оценками и помещаем в адаптер
        if (makes.moveToFirst()) {
            do {
                makes_list.add(
                    MakesInTrem(
                        discipline = makes.getString(1), tema = makes.getString(2),
                        type_of_work = makes.getString(3), type_of_control = makes.getString(4),
                        make = makes.getString(5), passing_score = makes.getString(6),
                        max_score = makes.getString(7), date = makes.getString(8),
                        teacher = makes.getString(9), trem = makes.getString(10)
                    )
                )
            } while (makes.moveToNext())
        }
        db.close()

        if (makes.moveToFirst()) {
            do {

                if (i != 0 && makes.getString(1) != makes_list[i-1].getDiscipline()) {
                    makes_list[i-1].setAllScore(count)
                    count = 0.0
                    makes_list[i-1].setIsAllScore(true)
                }

                try {
                    if (makes.getString(7) != "0")
                        count += makes.getString(5).toDouble()
                } catch (e: NumberFormatException) {}


                if (i == makes_list.size - 1) {
                    makes_list[i].setAllScore(count)
                    makes_list[i].setIsAllScore(true)
                    break
                }
                i += 1

            } while (makes.moveToNext())
        }

        return makes_list
    }

}
