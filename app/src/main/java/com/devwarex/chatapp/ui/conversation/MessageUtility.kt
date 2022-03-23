package com.devwarex.chatapp.ui.conversation



class MessageUtility {

    companion object{

        fun isTextMassageValid(text: String): Boolean = text.isNotBlank() && text.isNotEmpty()
        fun filterText(text: String): String{
            var t = text
            if (t.isBlank()){
                return t
            }
            if (t.isNotBlank() && t.length < 3){
                return t
            }
            val builder = StringBuilder(t)
            while (t[0].isWhitespace()){
                t = builder.deleteCharAt(0).toString()
            }
            while (t[t.lastIndex].isWhitespace()){
                t = builder.deleteCharAt(t.lastIndex).toString()
            }
            return t
        }
    }
}