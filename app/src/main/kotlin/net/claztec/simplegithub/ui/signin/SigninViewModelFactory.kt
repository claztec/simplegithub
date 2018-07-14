package net.claztec.simplegithub.ui.signin

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.claztec.simplegithub.api.AuthApi
import net.claztec.simplegithub.data.AuthTokenProvider

class SigninViewModelFactory(
        val api: AuthApi,
        val authTokenProvider: AuthTokenProvider)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SignInViewModel(api, authTokenProvider) as T
    }
}