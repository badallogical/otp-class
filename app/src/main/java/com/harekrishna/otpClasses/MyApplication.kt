package com.harekrishna.otpClasses

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.harekrishna.otpClasses.data.sources.repos.ConfigRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

fun Context.sendWhatsappMesssage(phone: String, message: String) {
    Log.d("Message Sending", phone + message)

    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://wa.me/${"91$phone"}?text=${Uri.encode(message)}".toUri()
        setPackage("com.whatsapp")
    }

    try {
        startActivity(intent)
    } catch (e : ActivityNotFoundException){
        Toast.makeText(this, "Whatsapp not installed", Toast.LENGTH_SHORT).show()
    }
}


@HiltAndroidApp
class MyApplication : Application() {


    init {
        instance = this
    }

    @Inject
    lateinit var configRepository: ConfigRepository

    override fun onCreate() {
        super.onCreate()

        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                configRepository.fetchConfig()
            } catch (e: Exception) {
                // optional: log error
            }
        }
    }

    companion object {
        private lateinit var instance: MyApplication


        fun String.toCamelCase(): String {
            return this.lowercase()
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
        }

    }
}