package com.cookandroid.weplog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_item.view.*


class HistoryAdapter(): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){


    private var historyList =  mutableListOf<History>()

    fun setListData(data:MutableList<History>){
        historyList = data
    }


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

        holder.historystarttime.text = historyList[position].startTime
        holder.historyendtime.text = historyList[position].endTime
        holder.historyruntime.text = historyList[position].time
        holder.historydate.text = String.format("%s월 %s일", historyList[position].month, historyList[position].day)
        holder.historykm.text = historyList[position].distance

    }

    override fun getItemCount(): Int {
        return historyList.size
    }



}