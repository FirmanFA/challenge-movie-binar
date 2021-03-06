package com.binar.challenge5.repository
import androidx.lifecycle.asLiveData
import com.binar.challenge5.data.api.ApiService
import com.binar.challenge5.datastore.UserDataStoreManager

class HomeRepository(private val apiService: ApiService, private val userPref: UserDataStoreManager) {


    suspend fun getAiringMovies() = apiService.getAiringMovie()
    suspend fun getDiscoverMovies() = apiService.getDiscoverMovie()
    suspend fun getUpcomingMovies() = apiService.getUpcomingMovie()
    suspend fun getTopRatedMovies() = apiService.getTopRatedMovie()
    fun getEmail() = userPref.getEmail.asLiveData()
    fun getNama() = userPref.getNama.asLiveData()
    suspend fun deletePref() = userPref.deletePref()


}