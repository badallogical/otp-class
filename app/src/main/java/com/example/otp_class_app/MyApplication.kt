package com.example.otp_class_app

import android.app.Application
import android.content.Context
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
    }

    override fun onCreate() {
        super.onCreate()
        container = RepoContainer(this)
    }
}