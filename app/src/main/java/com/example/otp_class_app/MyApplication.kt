package com.example.otp_class_app

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.otp_class_app.data.local.repos.RepoContainer

class MyApplication : Application() {

    lateinit var container : RepoContainer

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: MyApplication

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
    }

    override fun onCreate() {
        super.onCreate()
        container = RepoContainer(this)
    }
}