package com.devwarex.chatapp.utility

object PhoneUtil {

    fun filterPhoneNumber(phone: String): String {
        var fp = ""
        if (phone.isNotBlank()){
            fp = if (phone[0] == '0'){
                phone.substring(1)
            }else{
                phone
            }
        }
        return removeSpace(fp)
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