package com.harekrishna.otpClasses.di.modules

import com.harekrishna.otpClasses.data.sources.repos.RegistrationRepositoryImpl
import com.harekrishna.otpClasses.domain.repository.RegistrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRegistrationRepository(
        impl: RegistrationRepositoryImpl
    ): RegistrationRepository
}