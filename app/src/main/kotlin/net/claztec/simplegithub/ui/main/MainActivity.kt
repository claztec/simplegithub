package net.claztec.simplegithub.ui.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import net.claztec.simplegithub.R
import net.claztec.simplegithub.api.model.GithubRepo
import net.claztec.simplegithub.data.provideSearchHistoryDao
import net.claztec.simplegithub.extensions.plusAssign
import net.claztec.simplegithub.rx.AutoClearedDisposable
import net.claztec.simplegithub.ui.repo.RepositoryActivity
import net.claztec.simplegithub.ui.search.SearchActivity
import net.claztec.simplegithub.ui.search.SearchAdapter
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {

    internal val adapter by lazy {
        SearchAdapter().apply {
            setItemClickListener(this@MainActivity)
        }
    }

    internal val searchHistoryDao by lazy {
        provideSearchHistoryDao(this)
    }

    internal val disposables = AutoClearedDisposable(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycle += disposables
        lifecycle += object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun fetch() {
                fetchSearchHistory()
            }
        }

        btnActivityMainSearch.setOnClickListener {
            startActivity(intentFor<SearchActivity>())
        }

        with(rvActivityMainList) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun fetchSearchHistory(): Disposable = searchHistoryDao.getHistory()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                with(adapter) {
                    setItems(items)
                    notifyDataSetChanged()
                }

                if (items.isEmpty()) {
                    showMessage(getString(R.string.no_recent_repositories))
                } else {
                    hideMessage()
                }
            }) {
                showMessage(it.message)
            }

    companion object {

        private val TAG = "MainActivity"
    }

    override fun onItemClick(repository: GithubRepo) {
        startActivity<RepositoryActivity>(RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
                RepositoryActivity.KEY_REPO_NAME to repository.name)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.menu_activity_main_clear_all == item.itemId) {
            clearAll()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearAll() {
        disposables += Completable
                .fromCallable{ searchHistoryDao.clearAll() }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    private fun showMessage(message: String?) {
        with(tvActivityMainMessage) {
            text = message ?: "Unexpedtied error:"
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage() {
        with(tvActivityMainMessage) {
            text = ""
            visibility = View.GONE
        }
    }
}
