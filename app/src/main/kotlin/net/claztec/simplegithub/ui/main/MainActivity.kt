package net.claztec.simplegithub.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import net.claztec.simplegithub.R
import net.claztec.simplegithub.api.model.GithubRepo
import net.claztec.simplegithub.data.provideSearchHistoryDao
import net.claztec.simplegithub.extensions.plusAssign
import net.claztec.simplegithub.rx.AutoActivatedDisposable
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

    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposable = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy { MainViewModelFactory(provideSearchHistoryDao(this)) }

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]

        lifecycle += disposables

        lifecycle += viewDisposable

        // 액티비티 활성화 상태일때만 이라는데... Lifecycle.Event.ON_START 일때 인듯
        lifecycle += AutoActivatedDisposable(this) {
            viewModel.searchHistory
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { items ->
                        with(adapter) {
                            if (items.isEmpty) {
                                clearItems()
                            } else {
                                setItems(items.value)
                            }
                            notifyDataSetChanged()
                        }
                    }
        }

        btnActivityMainSearch.setOnClickListener {
            startActivity(intentFor<SearchActivity>())
        }

        with(rvActivityMainList) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        viewDisposable += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message ->
                    if (message.isEmpty) {
                        hideMessage()
                    } else {
                        showMessage(message.value)
                    }
                }
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
            disposables += viewModel.clearSearchHistory()
            return true
        }
        return super.onOptionsItemSelected(item)
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
