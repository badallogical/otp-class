package com.harekrishna.otpClasses.domain.repository

interface ConfigRepository {
    suspend fun fetchConfig(): Boolean
    fun getWelcomeMessage(): String
    fun getThanksMessage(): String
    fun getCongregationWelcomeMessage(): String
    fun getCongregationThanksMessage(): String
}
