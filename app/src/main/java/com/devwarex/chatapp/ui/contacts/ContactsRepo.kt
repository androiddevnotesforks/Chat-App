package com.devwarex.chatapp.ui.contacts

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Contact
import com.devwarex.chatapp.db.User
import com.devwarex.chatapp.repos.CreateChatRepo
import com.devwarex.chatapp.repos.SearchUserRepo
import com.devwarex.chatapp.util.DateUtility
import com.devwarex.chatapp.util.PhoneUtil
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

class ContactsRepo @Inject constructor(
    private val db: AppDao,
    private val searchRepo: SearchUserRepo,
    private val chatRepo: CreateChatRepo
) {

    private val job = CoroutineScope(Dispatchers.IO)
    private val userPhone: String = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
    val chatId: Flow<String?> get() = chatRepo.chatId.receiveAsFlow()

    fun getContacts(): Flow<List<Contact>>  = db.getContacts()

    init {
        job.launch {
            launch {
                searchRepo.user.receiveAsFlow().collectLatest { if (it != null){
                    chatRepo.create(it)
                    db.insertUser(
                        User(
                            uid = it.uid,
                            name = it.name,
                            img = it.img,
                            email = it.email,
                            phone = it.phone,
                            joinedAt = it.timestamp?.time ?: 0L
                        )
                    )
                }}
            }
            launch {
                searchRepo.isContactFound.receiveAsFlow().collectLatest {
                    db.updateContactExist(it)
                }
            }
        }
    }


    fun findIfUserExistByPhone(phone: String){
        searchRepo.findUserByPhone(phone)
    }

    fun updateContacts(contacts: Queue<Contact>){
        job.launch {
            while (contacts.isNotEmpty()){
                val contact = contacts.poll() ?: continue
                if (contact.phone.isNotBlank()){
                    if (contact.phone == userPhone){ continue }
                    db.insertContact(
                        Contact(
                            name = contact.name,
                            phone = PhoneUtil.filterPhoneNumber(contact.phone),
                            isFound = false
                        )
                    )
                }
            }
        }
    }

    fun searchContacts(contacts: List<Contact>){
        Log.e("search","started ${contacts.size}")
        job.launch {
            if(job.isActive) {
                val queue: Queue<Contact> = LinkedList(contacts)
                while (queue.isNotEmpty()) {
                    queue.poll()?.let { searchRepo.findUserByContact(it) }
                }
            }
        }
    }

    fun cancelJobs(){
        job.cancel()
    }
}