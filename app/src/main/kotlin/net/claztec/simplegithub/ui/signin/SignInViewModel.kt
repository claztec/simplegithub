package net.claztec.simplegithub.ui.signin

import android.arch.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import net.claztec.simplegithub.api.AuthApi
import net.claztec.simplegithub.data.AuthTokenProvider
import net.claztec.simplegithub.util.SupportOptional
import net.claztec.simplegithub.util.optionalOf

class SignInViewModel(
        val api: AuthApi,
        val authTokenProvider: AuthTokenProvider)
    : ViewModel() {

    val accessToken: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    val message: PublishSubject<String> = PublishSubject.create()

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun loadAccessToken(): Disposable = Single.fromCallable { optionalOf(authTokenProvider.token) }
            .subscribeOn(Schedulers.io())
            .subscribe(Consumer<SupportOptional<String>> {
                accessToken.onNext(it)
            })

    fun requestAccessToken(clientId: String, clientSecret: String, code: String): Disposable = api.getAccessToken(clientId, clientSecret, code)
            .map { it.accessToken }
            .doOnSubscribe { isLoading.onNext(true) }
            .doOnTerminate { isLoading.onNext(false) }
            .subscribe({ token ->
                authTokenProvider.updateToken(token)
                accessToken.onNext(optionalOf(token))
            }) {
                message.onNext(it.message ?: "Unexpected error")
            }
}