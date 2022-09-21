package com.devwarex.chatapp.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateUtility {

    companion object {
        @SuppressLint("SimpleDateFormat")
        fun getDate(time: Long): String{
            val format = SimpleDateFormat("dd MMM HH:mm")
            return format.format(time)
        }
        @SuppressLint("SimpleDateFormat")
        fun getChatDate(time: Long): String{
            val currentTime = Calendar.getInstance().timeInMillis
            return when{
                currentTime - time < 60000 -> { "n" }
                currentTime - time in 60..3599999 ->{
                    return getMin(current = currentTime, time = time).toString()
                }
                currentTime -time > 3600000 -> getHours(time = time)
                else -> {
                    val format = SimpleDateFormat("dd MMM HH:mm")
                    format.format(time)
                }
            }
        }

        private fun getMin(current: Long,time: Long): Int{
            return ((current - time)/60000).toInt()
        }

        @SuppressLint("SimpleDateFormat")
        private fun getHours(time: Long): String{
            val format = SimpleDateFormat("dd MMM HH:mm")
            val hourFormat = SimpleDateFormat("HH:mm")
            val currentCalender = Calendar.getInstance()
            val calender = Calendar.getInstance()
            calender.timeInMillis = time
            return if (currentCalender[Calendar.MONTH] == calender[Calendar.MONTH]){
                when(currentCalender[Calendar.DAY_OF_MONTH] - calender[Calendar.DAY_OF_MONTH]){
                    0 -> { hourFormat.format(time) }
                    1 -> { "y" }
                    else -> { format.format(time) }
                }

            }else{
                format.format(time)
            }
        }
    }
}