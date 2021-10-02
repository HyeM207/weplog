package com.cookandroid.weplog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView


class ExpandableAdapter(
    private val areaList: List<VisitArea>
) : RecyclerView.Adapter<ExpandableAdapter.MyViewHolder>() {


    class MyViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(visitarea: VisitArea) {
            val txtName = itemView.findViewById<TextView>(R.id.txt_name)
            val imgMore = itemView.findViewById<ImageButton>(R.id.img_more)
            val layoutExpand = itemView.findViewById<LinearLayout>(R.id.layout_expand)

            txtName.text = visitarea.areaname

            imgMore.setOnClickListener {
                // 1
                val show = toggleLayout(!visitarea.isExpanded, it, layoutExpand)
                visitarea.isExpanded = show
            }
        }

        private fun toggleLayout(isExpanded: Boolean, view: View, layoutExpand: LinearLayout): Boolean {
            // 2
            ToggleAnimation.toggleArrow(view, isExpanded)
            if (isExpanded) {
                ToggleAnimation.expand(layoutExpand)
            } else {
                ToggleAnimation.collapse(layoutExpand)
            }
            return isExpanded
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(areaList[position])
    }

    override fun getItemCount(): Int {
        return areaList.size
    }

}