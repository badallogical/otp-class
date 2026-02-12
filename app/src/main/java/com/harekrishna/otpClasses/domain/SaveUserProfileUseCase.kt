package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userPrefRepo : UserPreferencesRepository
){
    suspend operator fun invoke(name : String, phone : String){
        userPrefRepo.saveUserData(name, phone)
    }
}