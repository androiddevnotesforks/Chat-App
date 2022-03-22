package com.devwarex.chatapp.ui.conversation



class MessageUtility {

    companion object{

        fun isTextMassageValid(text: String): Boolean = text.isNotBlank() && text.isNotEmpty()
        fun filterText(text: String): String{
            var t = text
            if (text.isBlank()){
                return text
            }
            if (text.isNotBlank() && text.length < 3){
                return text
            }
            /*val builder = StringBuilder(text)
            while (t[0].isWhitespace()){
                t = builder.deleteCharAt(0).toString()
            }
            while (t[text.lastIndex].isWhitespace()){
                t = builder.deleteCharAt(t.lastIndex).toString()
            }
*/
            return t
        }
    }
}