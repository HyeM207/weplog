package com.cookandroid.weplog

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.map.*
import java.util.*

class MapFragment : Fragment() {

    private val PREF : String = "sharedpref"
    lateinit var map_btnstart : Button

    lateinit var alertDialog : AlertDialog
    lateinit var builder : AlertDialog.Builder

    lateinit var map_btnstop : Button
    lateinit var map_btnend : Button

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.map, container, false)


        val prefs : SharedPreferences = requireActivity().getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit() // 데이터 기록을 위한 editor

        map_btnstart = view.findViewById(R.id.map_btnstart)
        map_btnstop = view.findViewById(R.id.map_btnstop)
        map_btnend = view.findViewById(R.id.map_btnend)


//        editor.remove("isStarted")
//        editor.commit()
//        editor.remove("isStoped")
//        editor.commit()

        //Toast.makeText(activity, prefs.getString("isStarted","").toString(), Toast.LENGTH_SHORT).show()


        if (prefs.getString("isStarted","").equals("") ){
            //Toast.makeText(activity,"null", Toast.LENGTH_SHORT).show()
            editor.putString("isStarted", "No")
            editor.putString("isStoped", "No")
            editor.commit() // 필수
        } else if (prefs.getString("isStarted","").equals("Yes")  ){
                //Toast.makeText(activity,"else if 1", Toast.LENGTH_SHORT).show()
                map_btnstart.visibility = View.INVISIBLE
                map_btnstop.visibility = View.VISIBLE
                map_btnend.visibility = View.VISIBLE
            }
            else if (prefs.getString("isStarted","").equals("No") ) {
                //Toast.makeText(activity, "else if 2", Toast.LENGTH_SHORT).show()

                map_btnstart.visibility = View.VISIBLE
                map_btnstop.visibility = View.INVISIBLE
                map_btnend.visibility = View.INVISIBLE
            }



        map_btnstart.setOnClickListener {
            alertDialog()
            map_btnstart.visibility = View.INVISIBLE
            map_btnstop.visibility = View.VISIBLE
            map_btnend.visibility = View.VISIBLE

        }

        map_btnend.setOnClickListener {
            editor.putString("isStarted", "No")
            editor.remove("isStoped")
            editor.commit()

            map_btnstart.visibility = View.VISIBLE
            map_btnstop.visibility = View.INVISIBLE
            map_btnend.visibility = View.INVISIBLE

            map_btnstop.setText("STOP")
            editor.putString("isStoped", "No")
            editor.commit()
        }

        map_btnstop.setOnClickListener {
            //Toast.makeText(activity, prefs.getString("isStoped","").toString(), Toast.LENGTH_SHORT).show()

            if (prefs.getString("isStoped","").equals("") ){
                editor.putString("isStoped", "No")
                editor.commit()
            }
            else if (prefs.getString("isStoped","").equals("Yes")){
                // stop 상태일 때 버튼 누름 -> 시작하려고 함
                //Toast.makeText(activity, prefs.getString("isStoped","").toString()+"1", Toast.LENGTH_SHORT).show()
                map_btnstop.setText("STOP")
                editor.putString("isStoped", "No")
                editor.commit()
            }
            else if (prefs.getString("isStoped","").equals("No")){
                //Toast.makeText(activity, prefs.getString("isStoped","").toString()+"2", Toast.LENGTH_SHORT).show()
                map_btnstop.setText("RESTART")
                editor.putString("isStoped", "Yes")
                editor.commit()
            }

        }

        return view

    }




    fun alertDialog(){

        val prefs : SharedPreferences = requireActivity().getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()

        try{

            var str_buttonOK = "확인"
            var str_buttonNO = "취소"
            var str_buttonNature = "이동"

            builder = AlertDialog.Builder(requireContext())
            builder.setTitle("[START]")
            //builder.setIcon(R.drawable.tk_app_icon) //팝업창 아이콘 지정
            builder.setMessage("쓰레기봉투의 QR을 촬영하시겠습니까?")
            builder.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정

            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(activity, QRcodeScanner::class.java)
                startActivity(intent)
                editor.putString("isStarted", "Yes")
                editor.commit() // 필수
            })
            builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, which ->
                editor.putString("isStarted", "Yes")
                editor.commit() // 필수
            })


            alertDialog = builder.create()

            try {
                alertDialog.show()
            }
            catch (e : Exception){
                e.printStackTrace()
            }
        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }




}