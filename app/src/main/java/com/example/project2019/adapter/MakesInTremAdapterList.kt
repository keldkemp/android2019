package com.example.project2019.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project2019.R
import com.example.project2019.classes.MakesInTrem
import com.example.project2019.makes_in_trem

class MakesInTremAdapterList(private val context: Context, private var makes: List<MakesInTrem>): RecyclerView.Adapter<MakesInTremAdapterList.ViewHolder>() {

    private var listShow: ArrayList<MakesInTrem> = ArrayList()
    private var flag = 1

    override fun getItemCount(): Int {
        return makes.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.makes_in_trem, parent, false)
        return ViewHolder(itemView)
    }

    fun createListShow(trem: String, discipline: String): List<MakesInTrem> {
        var listShow = ArrayList<MakesInTrem>()
        var i = 0

        while (i < makes.size) {
            if (discipline == "Все предметы") {
                if (makes.get(i).getTrem() == trem) {
                    listShow.add(makes.get(i))
                }
            }
            else {
                    if (makes.get(i).getTrem() == trem && makes.get(i).getDiscipline() == discipline) {
                        listShow.add(makes.get(i))
                    }
            }
            i++
        }

        return listShow
    }

    override fun onBindViewHolder(holder: MakesInTremAdapterList.ViewHolder, position: Int) {
        holder.discipline.text = makes[position].getDiscipline()
        holder.discipline.setTextColor(Color.BLACK)
        if (makes_in_trem.flag_discipline != "Все предметы")
            holder.discipline.visibility = TextView.GONE

        holder.tema.text = makes[position].getTema()
        holder.tema.setTextColor(Color.BLACK)

        holder.passing_score.text = String.format(context.getString(R.string.passing_score), makes[position].getPassingScore())
        holder.passing_score.setTextColor(Color.BLACK)


        holder.make.text = String.format(context.getString(R.string.make), makes[position].getMake())
        holder.make.setTextColor(Color.BLACK)


        holder.max_score.text = String.format(context.getString(R.string.max_score), makes[position].getMaxScore())
        holder.max_score.setTextColor(Color.BLACK)

        holder.date.text = makes[position].getDate()
        holder.date.setTextColor(Color.BLACK)

        holder.teacher.text = String.format(context.getString(R.string.default_teacher), makes[position].getTeacher())
        holder.teacher.setTextColor(Color.BLACK)
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var discipline: TextView
        var tema: TextView
        var passing_score: TextView
        var make: TextView
        var max_score: TextView
        var date: TextView
        var layout: LinearLayout
        var teacher: TextView

        init {
            discipline = itemView.findViewById(R.id.makes_list_trem_discipline)
            passing_score = itemView.findViewById(R.id.makes_list_trem_passing_score)
            tema = itemView.findViewById(R.id.makes_list_trem_tema)
            make = itemView.findViewById(R.id.makes_list_trem_make)
            max_score = itemView.findViewById(R.id.makes_list_trem_max_score)
            date = itemView.findViewById(R.id.makes_list_trem_date)
            layout = itemView.findViewById(R.id.makes_trem_layout)
            teacher = itemView.findViewById(R.id.makes_list_trem_teacher)
        }
    }

    class ItemDecor(private val space: Int): RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = space
        }
    }
}