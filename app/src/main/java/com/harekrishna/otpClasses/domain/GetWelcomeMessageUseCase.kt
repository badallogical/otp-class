package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.ConfigRepository
import javax.inject.Inject

class GetWelcomeMessageUseCase @Inject constructor(
    private val repository: ConfigRepository
) {
    operator fun invoke(): String {
        return repository.getWelcomeMessage()
    }
}