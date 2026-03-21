package com.harekrishna.otpClasses.data.remote

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteConfigDataSource @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetch(0).await()
            remoteConfig.activate().await()
        } catch (e: Exception) {
            false
        }
    }

    fun getWelcomeMessage(): String {
        return remoteConfig.getString("welcome_message")
    }

    fun getThanksMessage(): String {
        return remoteConfig.getString("thanks_message")
    }
}