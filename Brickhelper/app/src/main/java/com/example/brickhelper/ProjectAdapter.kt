package com.example.brickhelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ProjectAdapter(context: Context, projects_list: ArrayList<String>) : BaseAdapter() {

    private val mContext: Context = context
    private val length = projects_list.size
    private val list = projects_list

    override fun getCount(): Int {
        return length
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val row = layoutInflater.inflate(R.layout.single_project, parent, false)

        val nameTextView = row.findViewById<TextView>(R.id.projectName)
        nameTextView.text = list[position]
        return row
    }
}