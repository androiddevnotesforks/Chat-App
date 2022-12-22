package com.devwarex.chatapp.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.devwarex.chatapp.ui.MainActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.util.Paths
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    private lateinit var galleryIntent: Intent
    private lateinit var pickPictureIntentLauncher: ActivityResultLauncher<Intent?>
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme{
              Surface(
                  modifier = Modifier.fillMaxSize(),
                  color = MaterialTheme.colors.background
              ){
                  ProfileScreen()
              }
            }
        }

        galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*"  }
        pickPictureLauncher()

        lifecycleScope.launchWhenCreated {
            launch { viewModel.insert.collect{
                if (it){
                   pickPhoto()
                }
            } }
            launch {
                viewModel.isSignedOut.collect{
                    if (it){
                        GoogleSignIn.getClient(this@ProfileActivity,getGSO()).signOut().addOnCompleteListener {  }
                        startActivity(Intent(this@ProfileActivity,MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun getGSO():GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Paths.FIREBASE_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

    private fun pickPhoto(){
        pickPictureIntentLauncher.launch(galleryIntent)
    }

    private fun pickPictureLauncher() {
        pickPictureIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            viewModel.removeInsertPhoto()
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val uri = result.data!!.data
                    try {
                        var imageStream = uri?.let {
                            contentResolver.openInputStream(
                                it
                            )
                        }
                        var bitmap = BitmapFactory.decodeStream(imageStream)
                        viewModel.setBitmap(bitmap)
                        bitmap = null
                        imageStream = null
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}