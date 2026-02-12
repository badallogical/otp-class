package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.harekrishna.otpClasses.data.sources.dataStore
import com.harekrishna.otpClasses.data.sources.keys.UserKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context : Context
) {

    suspend fun saveUserData(name: String, phone: String) {
        context.dataStore.edit {
            it[UserKeys.NAME_KEY] = name
            it[UserKeys.PHONE_KEY] = phone
        }
    }

    fun getUserData(): Flow<Pair<String?, String?>> {
        return context.dataStore.data.map {
            it[UserKeys.NAME_KEY] to it[UserKeys.PHONE_KEY]
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit {
            it.remove(UserKeys.NAME_KEY)
            it.remove(UserKeys.PHONE_KEY)
        }
    }

}