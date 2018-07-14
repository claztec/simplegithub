package net.claztec.simplegithub.ui.signin

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_signin.*
import net.claztec.simplegithub.BuildConfig
import net.claztec.simplegithub.R
import net.claztec.simplegithub.api.provideAuthApi
import net.claztec.simplegithub.data.AuthTokenProvider
import net.claztec.simplegithub.extensions.plusAssign
import net.claztec.simplegithub.rx.AutoClearedDisposable
import net.claztec.simplegithub.ui.main.MainActivity
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask

class SigninActivity : AppCompatActivity() {

    internal val api by lazy { provideAuthApi() }

    internal val authTokenProvider by lazy { AuthTokenProvider(this) }

    internal val disposables = AutoClearedDisposable(this)

    internal val viewDisposables = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    internal val viewModelFactory by lazy { SigninViewModelFactory(provideAuthApi(), AuthTokenProvider(this)) }
//
    lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[SignInViewModel::class.java]

        lifecycle += disposables

        lifecycle += viewDisposables


        btnActivitySignInStart.setOnClickListener {
            val authUri = Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                    .build()

            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SigninActivity, authUri)
        }

        viewDisposables += viewModel.accessToken
                .filter { !it.isEmpty }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ launchMainActivity() }

        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ message -> showError(message) }

        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ isLoading ->
                    if (isLoading) {
                        showProgress()
                    } else {
                        hideProgress()
                    }
                }

        disposables += viewModel.loadAccessToken()

    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(TAG, "온 뉴 인텐트에서: ")

        showProgress()
        val uri = intent.data ?: throw IllegalArgumentException("No data exist")

        val code = uri.getQueryParameter("code") ?: throw IllegalStateException("No code exist")

        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        disposables += viewModel.requestAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
    }

    override fun onStop() {
        super.onStop()
    }

    private fun showError(message: String) {
        longToast(message)
    }

    private fun hideProgress() {
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

    private fun showProgress() {
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun launchMainActivity() {
        Log.d(TAG, "런치 메인 액티비티")
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }

    companion object {
        private val TAG = "SigninActivity"
    }

}
