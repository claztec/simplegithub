package net.claztec.simplegithub.ui.main

import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import net.claztec.simplegithub.api.model.GithubRepo
import net.claztec.simplegithub.data.SearchHistoryDao
import net.claztec.simplegithub.extensions.runOnIoScheduler
import net.claztec.simplegithub.util.SupportOptional
import net.claztec.simplegithub.util.emptyOptional
import net.claztec.simplegithub.util.optionalOf

class MainViewModel(val searchHistoryDao: SearchHistoryDao) : ViewModel() {

    val searchHistory: Flowable<SupportOptional<List<GithubRepo>>>
        get() = searchHistoryDao.getHistory()
                .map { optionalOf(it) }
                .doOnNext{ optional ->
                    if (optional.value.isEmpty()) {
                        message.onNext(optionalOf("No recent repositories."))
                    } else {
                        message.onNext(emptyOptional())
                    }

                }
                .doOnError{
                    message.onNext(optionalOf(it.message ?: "Unexpected error"))
                }
                .onErrorReturn { emptyOptional() }

    val message: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    fun clearSearchHistory(): Disposable = runOnIoScheduler { searchHistoryDao.clearAll() }

}