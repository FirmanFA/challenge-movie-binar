package com.binar.challenge5.ui.auth

import androidx.lifecycle.*
import com.binar.challenge5.data.local.model.User
import com.binar.challenge5.repository.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository): ViewModel() {

    private val _user: MutableLiveData<User?>  = MutableLiveData()
    val user: LiveData<User?> = _user

    fun getUser(email: String){
        viewModelScope.launch {
            val newUser = repository.getUser(email)
            _user.postValue(newUser)
        }
    }

    fun updateUser(user: User) = repository.updateUser(user)

    suspend fun updateAvatarPath(id:Int, avatarPath: String) = repository.updateAvatarPath(id,avatarPath)

    fun login(email: String, password: String) = repository.login(email, password)

    fun register(user: User) = repository.register(user)

    fun checkIfEmailExist(email: String) = repository.checkEmailIfExist(email)



    //login preference
    fun setEmailPreference(email: String){
       viewModelScope.launch {
           repository.setEmail(email)
       }
    }

    fun setNamaPreference(nama: String){
        viewModelScope.launch {
            repository.setNama(nama)
        }
    }

    fun deletePref() = viewModelScope.launch {
        repository.deletePref()
    }

    fun emailPreference() = repository.getEmail()




}



