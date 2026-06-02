package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.harekrishna.otpClasses.data.sources.dataStore
import com.harekrishna.otpClasses.data.sources.keys.MessageKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class MessageType {
    WELCOME,
    THANKS,
    CONGREGATION_WELCOME,
    CONGREGATION_THANKS
}

class MessagePreferencesRepository @Inject constructor(
    @param:ApplicationContext val context : Context
) {
    // Function to save a setting in DataStore
    suspend fun saveWelcomeMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[MessageKeys.WELCOME_MESSAGE_KEY] = message.trimIndent().trim()
        }

        // WAIT until DataStore emits the new value
        context.dataStore.data
            .map { it[MessageKeys.WELCOME_MESSAGE_KEY] }
            .first { it == message.trimIndent().trim() }
    }

    suspend fun saveCongregationWelcome(message : String){
        context.dataStore.edit { preferences ->
            preferences[MessageKeys.CONGREGATION_WELCOME_KEY] = message.trimIndent().trim()
        }

        context.dataStore.data
            .map { it[MessageKeys.CONGREGATION_WELCOME_KEY] }
            .first { it == message.trimIndent().trim() }
    }

    suspend fun saveCongregationThanksMessage(message : String){
        context.dataStore.edit { preferences ->
            preferences[MessageKeys.CONGREGATION_THANKS_KEY] = message.trimIndent().trim()
        }

        context.dataStore.data
            .map { it[MessageKeys.CONGREGATION_WELCOME_KEY]}
            .first { it == message.trimIndent().trim() }
    }

    suspend fun saveThanksMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[MessageKeys.THANKS_MESSAGE_KEY] = message.trimIndent().trim()
        }

        context.dataStore.data
            .map { it[MessageKeys.THANKS_MESSAGE_KEY] }
            .first { it == message.trimIndent().trim() }
    }

    val welcomeMessageFlow = context.dataStore.data
        .map { it[MessageKeys.WELCOME_MESSAGE_KEY] ?: "" }

    val thanksMessageFlow = context.dataStore.data
        .map { it[MessageKeys.THANKS_MESSAGE_KEY] ?: "" }

    val congregationWelcomeMessageFlow = context.dataStore.data
        .map { it[MessageKeys.CONGREGATION_WELCOME_KEY] ?: "" }

    val congregationThankMessageFlow = context.dataStore.data
        .map { it[MessageKeys.CONGREGATION_WELCOME_KEY] ?: "" }

}