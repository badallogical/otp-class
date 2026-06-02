package com.harekrishna.otpClasses.data.sources.keys

import androidx.datastore.preferences.core.stringPreferencesKey

object MessageKeys {
    val WELCOME_MESSAGE_KEY = stringPreferencesKey("welcome_message")
    val THANKS_MESSAGE_KEY = stringPreferencesKey("thanks_message")
    val CONGREGATION_WELCOME_KEY = stringPreferencesKey("congregation_welcome")
    val CONGREGATION_THANKS_KEY = stringPreferencesKey("congregation_thanks")
}