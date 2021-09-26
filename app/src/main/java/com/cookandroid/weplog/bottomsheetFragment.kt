package com.cookandroid.weplog

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.mypage.view.*
import kotlinx.android.synthetic.main.record_choice.*
import java.util.*
import kotlin.collections.ArrayList


class bottomsheetFragment(context: Context) : BottomSheetDialogFragment() {

    lateinit var dataPassListener : onDataPassListener
    private val mContext: Context = context
    lateinit var listAdapter: ListAdapter
    private var recordchoicelist : ListView?= null
    private lateinit var database: DatabaseReference
    var list_item = ArrayList<MonthListViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as onDataPassListener //형변환
    }

    interface onDataPassListener {
        fun onDataPass(data : String?)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val pass : Button by lazy { view.findViewById(R.id.pass) }
//
//        // Activity의 OnCreate에서 했던 작업을 프래그먼트는 여기에서 수행행
//        pass.setOnClickListener {
//            dataPassListener.onDataPass("goodBye")
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.record_choice, container, false)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.SomeStyle)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.SomeStyle)
        recordchoicelist = view.findViewById(R.id.record_choicelist)





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

        list_item = ArrayList<MonthListViewModel>()
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
            recordchoicelist!!.adapter = listAdapter
            recordchoicelist!!.setOnItemClickListener { parent, view, position, id ->
                val clickedDate_m = list_item[position].month
                val clickedDate_y = list_item[position].year
                //Toast.makeText(activity, "$clickedDate_m", Toast.LENGTH_SHORT).show()
                //val intent = Intent(context, RecordActivity::class.java)
                //intent.putExtra("Month", clickedDate_m)
                dataPassListener.onDataPass(clickedDate_m)
                val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
                fragmentManager.beginTransaction().remove(this@bottomsheetFragment).commit()
                fragmentManager.popBackStack()

                //(activity as RecordActivity).destroyFragment()
                //requireContext().startActivity(intent)

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