package com.cookandroid.weplog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_item.view.*


class HistoryAdapter(): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){

    var titles = arrayOf("9월 25일", "9월 20일", "9월 19일", "9월 15일", "9월 10일")
    var details = arrayOf("Item one", "Item two", "Item three", "Item four", "Itme five")

    var images = intArrayOf(R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground)


    class HistoryViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        public var historydate: TextView = itemview.history_date
        public var historystarttime: TextView = itemview.history_starttime
        public var historyendtime: TextView = itemview.history_endtime
        public var historykm: TextView = itemview.history_km
        public var historyruntime: TextView = itemview.history_runtime
    }

    override fun onCreateViewHolder(viewgroup: ViewGroup, position: Int): HistoryViewHolder {
        var v: View = LayoutInflater.from(viewgroup.context).inflate(R.layout.history_item, viewgroup, false)

        return HistoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.historydate.setText(titles.get(position))
        holder.historykm.setText(details.get(position))
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}