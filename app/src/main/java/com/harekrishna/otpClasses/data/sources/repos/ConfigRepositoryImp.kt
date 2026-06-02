package com.harekrishna.otpClasses.data.sources.repos

import com.harekrishna.otpClasses.data.remote.RemoteConfigDataSource

interface ConfigRepository {
    suspend fun fetchConfig(): Boolean
    fun getWelcomeMessage(): String
    fun getThanksMessage(): String
    fun getCongregationWelcomeMessage(): String
    fun getCongregationThanksMessage(): String
}

class ConfigRepositoryImpl(
    private val dataSource: RemoteConfigDataSource
) : ConfigRepository {

    override suspend fun fetchConfig(): Boolean {
        return dataSource.fetchAndActivate()
    }

    override fun getWelcomeMessage(): String {
        return dataSource.getWelcomeMessage()
    }

    override fun getThanksMessage(): String {
        return dataSource.getThanksMessage()
    }

    override fun getCongregationWelcomeMessage(): String {
        return dataSource.getCongregationWelcomeMessage()
    }

    override fun getCongregationThanksMessage(): String {
        return dataSource.getCongregationThanksMessage()
    }

}