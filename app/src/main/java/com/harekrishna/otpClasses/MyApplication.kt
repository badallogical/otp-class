package com.harekrishna.otpClasses

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import dagger.hilt.android.HiltAndroidApp

fun Context.sendWhatsappMesssage(phone: String, message: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://wa.me/$phone?text=${Uri.encode(message)}".toUri()
        setPackage("com.whatsapp")
    }

    try {
        startActivity(intent)
    }catch (e : ActivityNotFoundException){
        Toast.makeText(this, "Whatsapp not installed", Toast.LENGTH_SHORT).show()
    }
}


@HiltAndroidApp
class MyApplication : Application() {


    init {
        instance = this
    }

    companion object {
        private lateinit var instance: MyApplication



        fun applicationContext(): Context {
            return instance.applicationContext
        }

        fun String.toCamelCase(): String {
            return this.lowercase()
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
        }

    }
}