package net.claztec.simplegithub.data

import android.content.Context
import android.preference.PreferenceManager

/*
프로퍼티를 저장한다.
 */
class AuthTokenProvider(private val context: Context) {

    val token: String?
        get() = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_AUTH_TOKEN, null)

    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply()
    }

    companion object {

        private val KEY_AUTH_TOKEN = "auth_token"
    }
}
