package com.example.project2019

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_menu.view.*


class Menu : Fragment() {

    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val View = inflater.inflate(R.layout.fragment_menu, container, false)
        val TextView = View.menu_student_name

        if (name == "") {
            val db = activity!!.baseContext.openOrCreateDatabase("Etis.db", Context.MODE_PRIVATE, null)

            val raw = db.rawQuery("select * from users where username = ?", arrayOf(Main2Activity.USERNAME))
            raw.moveToFirst()

            name = raw.getString(1)

            db.close()
        }

        TextView.text = String.format(View.context.getString(R.string.student, name))
        TextView.setTextColor(Color.BLACK)
        TextView.setBackgroundColor(Color.WHITE)
        TextView.setTextSize(20.toFloat())

        return View
    }

}
