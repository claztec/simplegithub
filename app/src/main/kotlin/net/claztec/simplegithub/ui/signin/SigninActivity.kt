package net.claztec.simplegithub.ui.signin

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

//    internal var accessTokenCall: Call<GithubAccessToken>? = null
    internal val disposables = AutoClearedDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

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

        Log.d(TAG, "token: " + authTokenProvider.token!!)

        if (null != authTokenProvider.token) {
            launchMainActivity()
        }


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
        showProgress()

        disposables += api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
                .map { it.accessToken }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe{ showProgress() }
                .doOnTerminate { hideProgress() }
                .subscribe({ token ->
                    authTokenProvider.updateToken(token)
                    launchMainActivity()
                }) {
                    showError(it)
                }

    }

    override fun onStop() {
        super.onStop()
    }

    private fun showError(throwable: Throwable) {
        longToast(throwable.message ?: "No message available")
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
