package com.devwarex.chatapp

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devwarex.chatapp.models.MessageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(): ViewModel() {

    val text = MutableStateFlow<String>("")
    fun setText(s: String){
        text.value = s
    }

    fun onClick(){
        text.value = ""
    }
    val messages = mutableStateListOf(
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user1","Hey!","https://developer.android.com/images/brand/Android_Robot.png"),
        MessageModel("user2","Hii..FINAL","https://developer.android.com/images/brand/Android_Robot.png")
    )

}