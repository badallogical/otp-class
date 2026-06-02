package com.harekrishna.otpClasses.domain

import android.util.Log
import com.harekrishna.otpClasses.data.sources.repos.ConfigRepository
import com.harekrishna.otpClasses.data.sources.repos.MessagePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.MessageType
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PrepareWhatsappMessageUseCase @Inject constructor(
    private val configRepository: ConfigRepository
) {

    suspend operator fun invoke(
        phoneNumber: String,
        type: MessageType
    ) : String {

        val rawMessage = when (type) {
            MessageType.WELCOME -> configRepository.getWelcomeMessage()
            MessageType.THANKS -> configRepository.getThanksMessage()
            MessageType.CONGREGATION_WELCOME -> configRepository.getCongregationWelcomeMessage()
            MessageType.CONGREGATION_THANKS -> configRepository.getCongregationThanksMessage()
        }


        Log.d("PrepareWhatsappMessageUseCase", "rawMessage: $rawMessage")
        // Safety check (VERY IMPORTANT)
        val message = rawMessage.takeIf { it.isNotBlank() }?.replace("\\n", "\n")
            ?: return "Message not available. Please try again."

        val greeting = "Hello 💐💐💐"

        val footer = "Regards,\nISKCON Youth Forum".trimIndent()

        val finalMessage = """
            |$greeting
            
            |$message
            
            |$footer""".trimMargin()

        return finalMessage
    }
}

object PhoneFormatter {
    fun format(phone: String): String {
        return if (phone.startsWith("+91")) {
            phone
        } else {
            "+91${phone.filter { it.isDigit() }}"
        }
    }
}

object DefaultMessages {

    val welcome = """
        |Thank you for registering in ISKCON Youth Forum (IYF) program. 🎉✌

        |It is a *life-changing* step to *discover yourself* and *unleash your true potential*. 💯🌟

        |📢 *We invite you to the Sunday Program*:

        |🕒 *Timing*: *4:30 PM*, this *Sunday*
        |🎉 *Event*: Seminar 🧑‍💻🗣, Kirtan 🎤, Music 🎸, Q&A session, Mentorship and Delicious Snacks 🍛🍰

        |*Entry is Free*
        |
        |🏛️ *Venue*: IYF Seminar Hall, ISKCON Temple, Lucknow
        |
        |📍*Location of Seminar Hall*: https://maps.app.goo.gl/ao6jTfJpatKEnLrf8?g_st=aw
        |""".trimMargin()



    val thanks = """
        |Thank you for attending our ISKCON Youth Forum (IYF) Program! 🌟

        |We're glad you joined, and we hope it was a fruitful experience for your spiritual journey. 🌱

        |📢 *We warmly invite you to our next Sunday Program*:

        |🕒 *Timing*: 4:30 PM, this Sunday

        |🎉 *Highlights*: Engaging Seminar 🧑‍💻🗣️, Soul-stirring Kirtan 🎤, Live Music 🎸, and Delicious Snacks 🍛🍰.

        |🏛️ *Venue*: IYF Seminar Hall, ISKCON Temple, Lucknow""".trimMargin()
}

