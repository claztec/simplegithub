package net.claztec.simplegithub.ui.repo

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import net.claztec.simplegithub.api.GithubApi

class RepositoryViewModelFactory(val api: GithubApi) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RepositoryViewModel(api) as T
    }

}