package com.devwarex.chatapp.util

import com.google.firebase.auth.FirebaseAuth

object PhoneUtil {

    private val userPhone: String get() = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

    fun filterPhoneNumber(phone: String): String {
        var fp = ""
        if (phone.isNotBlank()){
            fp = if (phone[0] == '0'){
                phone.substring(1)
            }else{
                phone
            }
            if (fp.isNotBlank() && fp[0] != '+'){
                fp = getCode(fp[0])+fp
            }
        }
        return removeSpace(fp)
    }

    private fun getCode(breakPoint: Char): String{
        var code: String = ""
        for (c in userPhone){
            if (c == breakPoint){
                break
            }
            code += c
        }
        return code
    }
    private fun removeSpace(phone: String): String{
        var fp = phone
        var i = 0

        while (i < fp.length){
            val builder = StringBuilder(fp)
            if (fp[i].isWhitespace() || fp[i] == '-') {
                fp = builder.deleteAt(i).toString()
            }
            i++
        }

        return fp
    }
}