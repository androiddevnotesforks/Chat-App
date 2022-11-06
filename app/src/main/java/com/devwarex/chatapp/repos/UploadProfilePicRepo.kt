package com.devwarex.chatapp.repos

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

class UploadProfilePicRepo @Inject constructor(

) {
    val img = Channel<String>(Channel.UNLIMITED)
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    val uploadProgress = MutableStateFlow(0)
    fun upload(bitmap: Bitmap){
        uploadProgress.value = 1
        val frontImg = storageRef.child("profile_pictures/${user?.uid}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,50, baos)
        val data = baos.toByteArray()
        val task = frontImg.putBytes(data)
        task.addOnCompleteListener{snapshot ->
            if (snapshot.isSuccessful){
                frontImg.downloadUrl.addOnCompleteListener { uri ->
                    if (uri.isSuccessful){
                        CoroutineScope(Dispatchers.Default).launch {
                            img.send(uri.result.toString())
                        }
                    }
                }
            }

        }.addOnFailureListener{ Log.e("PROFILE_STORAGE",it.localizedMessage!!)}
        task.addOnProgressListener { result -> val progress:Double = (100.0 * result.bytesTransferred) / result.totalByteCount
            uploadProgress.value = progress.toInt()
        }
    }
}