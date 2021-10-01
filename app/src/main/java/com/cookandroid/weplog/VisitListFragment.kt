package com.cookandroid.weplog

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VisitListFragment(context: Context):BottomSheetDialogFragment() {

    private var recordchoicelist : ListView?= null

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


        val items= mutableListOf<ListViewItem>()
        items.add(ListViewItem(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fiber_manual_record_24), "주소"))

        val adapter=ListViewAdapter(items)

        recordchoicelist!!.adapter = adapter


        return view
    }

}