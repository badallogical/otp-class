package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.ConfigRepository
import javax.inject.Inject

class FetchConfigUseCase @Inject constructor (
    private val repository: ConfigRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.fetchConfig()
    }
}