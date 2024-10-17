package com.harekrishna.otpClasses

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.repos.RepoContainer
import kotlinx.coroutines.flow.first

class MyApplication : Application() {

    lateinit var container : RepoContainer

    init {
        instance = this

    }

    companion object {
        private lateinit var instance: MyApplication
        lateinit var userName : String
        lateinit var userPhone : String

        fun applicationContext(): Context {
            return instance.applicationContext
        }

        fun checkInternetConnection(): Boolean {
            val connectivityManager =
                instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }

        suspend fun getUserData(){
            val userData = AttendanceDataStore.getUserData().first()
            userName = userData.first ?: "Rajiva Prabhu Ji"
            userPhone = userData.second ?: "+919807726801"
        }


    }

    override fun onCreate() {
        super.onCreate()
        container = RepoContainer(this)
    }
}