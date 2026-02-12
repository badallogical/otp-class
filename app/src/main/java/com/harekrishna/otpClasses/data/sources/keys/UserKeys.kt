package com.harekrishna.otpClasses.data.sources.keys

import androidx.datastore.preferences.core.stringPreferencesKey

object UserKeys{
    val NAME_KEY = stringPreferencesKey("user_name")
    val PHONE_KEY = stringPreferencesKey("user_phone")
}