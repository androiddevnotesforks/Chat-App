package com.devwarex.chatapp.utility

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

class DateUtility {

    companion object {
        @SuppressLint("SimpleDateFormat")
        fun getDate(time: Long): String{
            val format = SimpleDateFormat("dd MMM HH:mm")
            return format.format(time)
        }
    }
}