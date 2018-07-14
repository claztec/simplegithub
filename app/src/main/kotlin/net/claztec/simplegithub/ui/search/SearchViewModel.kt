package net.claztec.simplegithub.ui.search

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import net.claztec.simplegithub.api.GithubApi
import net.claztec.simplegithub.api.model.GithubRepo
import net.claztec.simplegithub.data.SearchHistoryDao
import net.claztec.simplegithub.extensions.runOnIoScheduler
import net.claztec.simplegithub.util.SupportOptional
import net.claztec.simplegithub.util.emptyOptional
import net.claztec.simplegithub.util.optionalOf

class SearchViewModel(val api: GithubApi, val searchHistoryDao: SearchHistoryDao) : ViewModel() {

    val searchResult: BehaviorSubject<SupportOptional<List<GithubRepo>>> = BehaviorSubject.createDefault(emptyOptional())

    val lastSearchKeyword: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.createDefault(emptyOptional())

    val message: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun searchRepository(query: String): Disposable
            = api.searchRepository(query)
            .doOnNext{ lastSearchKeyword.onNext(optionalOf(query)) }
            .flatMap {
                if (0 == it.totalCount) {
                    Observable.error(IllegalStateException("No search result"))
                } else {
                    Observable.just(it.items)
                }
            }
            .doOnSubscribe {
                searchResult.onNext(emptyOptional())
                message.onNext(emptyOptional())
                isLoading.onNext(true)
            }
            .doOnTerminate { isLoading.onNext(false) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                items -> searchResult.onNext(optionalOf(items))
            }) {
                message.onNext(optionalOf(it.message ?: "Unexpected error"))
            }

    fun addToSearchHistory(repository: GithubRepo): Disposable
        = runOnIoScheduler { searchHistoryDao.add(repository) }
}