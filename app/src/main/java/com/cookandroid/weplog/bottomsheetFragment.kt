package com.cookandroid.weplog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class bottomsheetFragment(context: Context) : BottomSheetDialogFragment() {

    private val mContext: Context = context
    lateinit var listAdapter: ListAdapter
    private var list : ListView?= null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.record_choice, container, false)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.SomeStyle)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.SomeStyle)
        list = view.findViewById(R.id.list)


//        var list_item = ArrayList<MonthListViewModel>()
//        list_item.add(MonthListViewModel("2021", "9"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        list_item.add(MonthListViewModel("2021", "8"))
//        listAdapter = ListAdapter(requireContext(), list_item)
//        list!!.adapter = listAdapter

//        list!!.setOnTouchListener { v, event ->
//            var action = event.action
//            when (action) {
//                MotionEvent.ACTION_DOWN ->                         // Disallow NestedScrollView to intercept touch events.
//                    v.parent.requestDisallowInterceptTouchEvent(true)
//                MotionEvent.ACTION_UP ->                         // Allow NestedScrollView to intercept touch events.
//                    v.parent.requestDisallowInterceptTouchEvent(false)
//            }
//            // Handle ListView touch events.
//            v.onTouchEvent(event)
//            true
//        }
        date_db()

        return view
    }

    fun date_db(){
        var list_item = ArrayList<MonthListViewModel>()
        val user = Firebase.auth.currentUser
        database = Firebase.database.reference
        var mCalendar = Calendar.getInstance()
        var currentyear = (mCalendar.get(Calendar.YEAR)).toString()
        var currentMonth = (mCalendar.get(Calendar.MONTH) + 1).toString()
        var currentday = (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()
        var joinyear : String ?= null
        var joinmonth : String ?= null
        var joinday : String ?= null

        database.child("users").child(user!!.uid).child("joindate").get().addOnSuccessListener {
            println("joindate : " + it.value)
            var joindate = it.value.toString()
            joinyear = joindate.substring(0, joindate.indexOf("/"))
            joinmonth = joindate.substring(5, joindate.lastIndexOf("/"))
            joinday = (joindate.substring(joindate.lastIndexOf("/")+1))
            println( joinyear + joinmonth + joinday)
            if (currentyear.toString() == joinyear.toString()){
                println("if문 : " + currentyear.toString() + joinyear)
                if ( currentMonth.toString() == joinmonth.toString() ){
                    list_item.add(MonthListViewModel(currentyear, currentMonth))
                } else {
                    for (i in joinmonth!!.toInt()..currentMonth.toInt()) {
                        println("i :: " + i)
                        list_item.add(MonthListViewModel(currentyear, i.toString()))
                    }
                }
            } else {
                for(i in joinyear!!.toInt()..currentyear.toInt()){
                    if ( i == currentyear.toInt()) {
                        for ( m in 1..currentMonth.toInt()){
                            list_item.add(MonthListViewModel(i.toString(), m.toString()))
                        }
                    } else if ( i == joinyear!!.toInt()){
                        for ( m in joinmonth!!.toInt()..12){
                            list_item.add(MonthListViewModel(i.toString(), m.toString()))
                        }
                    } else {
                        for ( m in 1..12){
                            list_item.add(MonthListViewModel(i.toString(), m.toString()))
                        }
                    }
                }
            }
            listAdapter = ListAdapter(requireContext(), list_item)
            list!!.adapter = listAdapter
            list!!.setOnItemClickListener { parent, view, position, id ->
                val clickedDate = list_item[position]
            }

        }
    }

}

data class MonthListViewModel(
    val year : String,
    val month : String
)

class ListAdapter (val context: Context, val array: ArrayList<MonthListViewModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return array.size
    }

    override fun getItem(position: Int): Any {
        return array[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.record_choice_item, null)
        var year = view.findViewById<TextView>(R.id.rec_year)
        var month = view.findViewById<TextView>(R.id.rec_month)
        val listitem = array[position]
        year!!.text = listitem.year
        month!!.text = listitem.month
        return view
    }

}