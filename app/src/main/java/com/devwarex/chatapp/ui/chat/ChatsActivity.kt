package com.devwarex.chatapp.ui.chat

import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.ChatRelations
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.signUp.ErrorsState
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.utility.BroadCastUtility
import com.google.android.gms.ads.*
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ChatsActivity : ComponentActivity() {
    private val viewModel:  ChatsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)

        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChatsScreen()
                }
            }
        }
        viewModel.chatId.observe(this,this::toConversation)
        lifecycleScope.launchWhenCreated {
            launch { viewModel.emailMessage.collect { updateUiOnEmailError(it) } }
            launch {
                viewModel.isAdded.collect { if (it){
                    Toast.makeText(this@ChatsActivity, "User added",Toast.LENGTH_LONG).show()
                    viewModel.hideDialog()
                } else {
                    Toast.makeText(this@ChatsActivity, "User already exist",Toast.LENGTH_LONG).show()
                    viewModel.hideDialog()
                } }
            }
        }

       /* var adRequest = AdRequest.Builder().build()

        RewardedAd.load(this,"ca-app-pub-2512609943608786/9707272930", adRequest,
            object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mRewardedAd = null
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                mRewardedAd = rewardedAd
            }
        })
        setFull()
        if (mRewardedAd != null) {
            mRewardedAd?.show(this, OnUserEarnedRewardListener() {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    var rewardAmount = rewardItem.amount
                    var rewardType = rewardItem.getType()
                    Log.d(TAG, "User earned the reward. $rewardAmount , $rewardType")
                }
            })
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
        }

    }

    private fun setFull(){
        mRewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad was shown.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                // Called when ad fails to show.
                Log.d(TAG, "Ad failed to show.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "Ad was dismissed.")
                mRewardedAd = null
            }
        }*/
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("TAG", "onVerificationCompleted:$credential")
           // signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w("TAG", "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("TAG", "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            //storedVerificationId = verificationId
            //resendToken = token
        }
    }

    private fun aut(){
        var auth = Firebase.auth

        val options = PhoneAuthOptions.newBuilder(auth)
            //.setPhoneNumber("+201016748500")       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            //.build()
        options.setPhoneNumber("")
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    private fun updateUiOnEmailError(state: ErrorsState){
        when(state){
            ErrorsState.INVALID_EMAIL -> {
                Toast.makeText(this,getString(R.string.invalid_email_message),Toast.LENGTH_LONG).show()
            }
            ErrorsState.EMAIL_NOT_FOUND -> {
                Toast.makeText(this,getString(R.string.email_not_found_message),Toast.LENGTH_LONG).show()
            }
            ErrorsState.NONE -> { }
            else -> viewModel.hideDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearChatId()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    private fun toConversation(chatId: String){
        if(chatId.isEmpty()) return
        val conversationIntent = Intent(this,ConversationActivity::class.java)
            .apply {
                putExtra(BroadCastUtility.CHAT_ID,chatId)
            }
        startActivity(conversationIntent)
    }
}

@Composable
fun AdVMob(){
    AndroidView(factory = {
        AdView(it).apply {
            adSize = AdSize.BANNER
            adUnitId = "ca-app-pub-3940256099942544/6300978111"
            loadAd(AdRequest.Builder().build())
        }
    })
}

@Composable
fun ChatsScreen(modifier: Modifier = Modifier){
    val viewModel: ChatsViewModel = hiltViewModel()
    val (chats,isLoading,showDialog) = viewModel.uiState.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(
                title ={ Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = { if (!showDialog) viewModel.showDialog() }) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add chat" )
        }},
        floatingActionButtonPosition = FabPosition.End
    ) {


        if (showDialog){
            AddUserDialog()
        }else{
            LazyColumn(modifier = modifier){
                items(chats){
                    ChatCard(chat = it)
                }
            }
        }

    }
}

@Composable
fun AddUserDialog(){
    val viewModel = hiltViewModel<ChatsViewModel>()
    val email = viewModel.email.collectAsState().value
    CustomDialogScreen() {
        Card(
            elevation = 12.dp,
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp, end = 16.dp),
            shape = MaterialTheme.shapes.large.copy(
                all = CornerSize(12.dp)
            )
        ) {
            Column() {
                Row() {
                    IconButton(
                        onClick = { viewModel.hideDialog() },
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "close", tint = MaterialTheme.colors.onSurface )
                    }
                    Text(
                        text = stringResource(id = R.string.add_user_title),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.setEmail(it) },
                    modifier = Modifier
                        .padding(all = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    label = { Text(text = stringResource(id = R.string.email_title)) },
                    placeholder = { Text(text = stringResource(id = R.string.enter_email_title))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Button(onClick = { viewModel.addUser()},
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp)) {
                    Text(text = stringResource(id = R.string.add_title))
                }
            }
        }
    }
}

@Composable
fun CustomDialogScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    Layout(
        modifier = modifier.fillMaxSize(),
        content = content
    ){ measurables, constraints ->

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight){
            placeables.forEach { placeable ->
                placeable.placeRelative(x = constraints.maxWidth/constraints.maxHeight, y = constraints.maxHeight/constraints.maxWidth)
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatCard(chat: ChatRelations){
    val viewModel: ChatsViewModel = hiltViewModel()
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .padding(all = 2.dp)
            .fillMaxWidth(),
        onClick = { viewModel.onChatClick(chat.chat.id) }
    ) {
        Row(Modifier.padding(all = 16.dp)) {
            Image(
                painter = if (chat.user?.img.isNullOrEmpty()) painterResource(id = R.drawable.user) else rememberImagePainter(data = chat.user?.img),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column{
                Text(
                    text = chat.user?.name ?: "Name",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                    maxLines = 1
                )
                Row() {
                    if (chat.chat.lastMessage == "IMAGE") {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_photo),
                            contentDescription ="photo label",
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp).size(16.dp)
                        )
                    }
                    Text(
                        text = if (chat.chat.lastMessage == "IMAGE") stringResource(id = R.string.photo_name) else chat.chat.lastMessage,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp).align(Alignment.CenterVertically),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}