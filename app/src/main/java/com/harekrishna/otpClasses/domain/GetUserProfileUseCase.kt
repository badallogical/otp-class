package com.harekrishna.otpClasses.domain

import android.content.Context
import com.harekrishna.otpClasses.data.sources.repos.UserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    fun getUserData(): Flow<Pair<String?, String?>> {
        return userProfileRepository.getUserProfile()
            .map { user ->
                user.name to user.phone
            }
    }

}