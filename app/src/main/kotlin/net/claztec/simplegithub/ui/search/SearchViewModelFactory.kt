package net.claztec.simplegithub.ui.search

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.claztec.simplegithub.api.GithubApi
import net.claztec.simplegithub.data.SearchHistoryDao

class SearchViewModelFactory(val api: GithubApi, val searchHistoryDao: SearchHistoryDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchViewModel(api, searchHistoryDao) as T
    }

}