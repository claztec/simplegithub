package net.claztec.simplegithub.ui.repo

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import net.claztec.simplegithub.api.GithubApi
import net.claztec.simplegithub.api.model.GithubRepo
import net.claztec.simplegithub.util.SupportOptional
import net.claztec.simplegithub.util.optionalOf

class RepositoryViewModel(val api: GithubApi) : ViewModel() {

    val repository: BehaviorSubject<SupportOptional<GithubRepo>> = BehaviorSubject.create()

    val message: BehaviorSubject<String> = BehaviorSubject.create()

    val isContentVisible: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.create()

    fun requestRepositoryInfo(login: String, repoName: String): Disposable {
        val repoObservable = if (!repository.hasValue()) {
            api.getRepository(login, repoName)
        } else {
            Observable.empty()
        }
        return repoObservable
                .doOnSubscribe { isLoading.onNext(true) }
                .doOnTerminate { isLoading.onNext(false) }
                .subscribeOn(Schedulers.io())
                .subscribe({ repo ->
                    repository.onNext(optionalOf(repo))
                    isContentVisible.onNext(true)
                }) {
                    message.onNext(it.message ?: "Unexpected error")
                }
    }

}