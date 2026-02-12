package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.MessagePreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InitializeDefaultMessagesUseCase @Inject constructor(
    private val messageRepo : MessagePreferencesRepository
) {
    suspend operator fun invoke() {
        if (messageRepo.welcomeMessageFlow.first().isBlank()) {
            messageRepo.saveWelcomeMessage(DefaultMessages.welcome)
        }
        if (messageRepo.thanksMessageFlow.first().isBlank()) {
            messageRepo.saveThanksMessage(DefaultMessages.thanks)
        }
    }
}

class ResetMessagesUseCase @Inject constructor(
    private val messageRepo: MessagePreferencesRepository
) {
    suspend operator fun invoke() {
        messageRepo.saveWelcomeMessage(DefaultMessages.welcome)
        messageRepo.saveThanksMessage(DefaultMessages.thanks)
    }
}
