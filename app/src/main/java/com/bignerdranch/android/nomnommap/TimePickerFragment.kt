package com.bignerdranch.android.nomnommap

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.Calendar

class TimePickerFragment : DialogFragment() {

    private val args: TimePickerFragmentArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeListener = TimePickerDialog.OnTimeSetListener {
                _: TimePicker, hour: Int, minute: Int ->
            val calendar = Calendar.getInstance()
            calendar.time = args.mealDate
            val resultDate = Calendar.getInstance()
            resultDate.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            resultDate.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            resultDate.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            resultDate.set(Calendar.HOUR_OF_DAY, hour)
            resultDate.set(Calendar.MINUTE, minute)
            setFragmentResult(REQUEST_KEY_TIME,
                bundleOf(BUNDLE_KEY_TIME to resultDate.time))
        }

        val calendar = Calendar.getInstance()
        calendar.time = args.mealDate
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            false
        )
    }

    companion object {
        const val REQUEST_KEY_TIME = "REQUEST_KEY_TIME"
        const val BUNDLE_KEY_TIME = "BUNDLE_KEY_TIME"
    }
}