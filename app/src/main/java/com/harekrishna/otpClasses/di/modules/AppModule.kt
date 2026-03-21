package com.harekrishna.otpClasses.di.modules

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.harekrishna.otpClasses.data.remote.RemoteConfigDataSource
import com.harekrishna.otpClasses.data.sources.repos.ConfigRepository
import com.harekrishna.otpClasses.data.sources.repos.ConfigRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }

        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(
            mapOf(
                "welcome_message" to "",
                "thanks_message" to ""
            )
        )

        return remoteConfig
    }

    @Provides
    fun provideRepository(
        dataSource: RemoteConfigDataSource
    ): ConfigRepository = ConfigRepositoryImpl(dataSource)

}