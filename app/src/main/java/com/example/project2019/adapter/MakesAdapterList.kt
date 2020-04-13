package com.example.project2019.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project2019.Main2Activity
import com.example.project2019.R
import com.example.project2019.classes.makes
import com.google.android.material.behavior.SwipeDismissBehavior

class MakesAdapterList(private val context: Context, private val makes: List<makes>): RecyclerView.Adapter<MakesAdapterList.ViewHolder>() {

    override fun getItemCount(): Int {
        return makes.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.makes_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MakesAdapterList.ViewHolder, position: Int) {
        holder.discipline.text = makes[position].getDiscipline()
        holder.discipline.setTextColor(Color.BLACK)

        holder.make.text = makes[position].getMake()
        holder.make.setTextColor(Color.parseColor("#8db600"))

        holder.date.text = makes[position].getDate()
        holder.date.setTextColor(Color.BLACK)

        holder.teacher.text = String.format(context.getString(R.string.default_teacher), makes[position].getTeacher())
        holder.teacher.visibility = TextView.GONE
        holder.teacher.setTextColor(Color.BLACK)

        holder.trem.text = makes[position].getTrem()
        holder.trem.visibility = TextView.GONE
        holder.trem.setTextColor(Color.BLACK)

        holder.layout.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View) {
                if  (holder.teacher.visibility == TextView.GONE) {
                    holder.teacher.visibility = TextView.VISIBLE
                    holder.trem.visibility = TextView.VISIBLE
                }
                else {
                    holder.teacher.visibility = TextView.GONE
                    holder.trem.visibility = TextView.GONE
                }
            }
        })
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var discipline: TextView
        var make: TextView
        var date: TextView
        var layout: LinearLayout
        var teacher: TextView
        var trem: TextView

        init {
            discipline = itemView.findViewById(R.id.makes_list_discipline)
            make = itemView.findViewById(R.id.makes_list_make)
            date = itemView.findViewById(R.id.makes_list_date)
            layout = itemView.findViewById(R.id.makes_layout)
            teacher = itemView.findViewById(R.id.makes_list_teacher)
            trem = itemView.findViewById(R.id.makes_list_trem)
        }
    }

    class ItemDecor(private val space: Int): RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.bottom = space
        }
    }
}