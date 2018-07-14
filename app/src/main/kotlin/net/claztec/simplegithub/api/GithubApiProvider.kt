package net.claztec.simplegithub.api

import android.content.Context
import net.claztec.simplegithub.data.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


fun provideAuthApi(): AuthApi = Retrofit.Builder()
        .baseUrl("https://github.com")
        .client(provideOkHttpClient(provideLoggingInterceptor(), null))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)


fun provideGithubApi(context: Context): GithubApi = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(provideOkHttpClient(provideLoggingInterceptor(), providerAuthInterceptor(provideAuthTokenProvider(context))))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GithubApi::class.java)


private fun providerAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
    val token = provider.token ?: throw IllegalStateException("authtoken cannot be null")
    return AuthInterceptor(token)
}

private fun provideAuthTokenProvider(context: Context): AuthTokenProvider = AuthTokenProvider(context.applicationContext)


private fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }


private fun provideOkHttpClient(interceptor: HttpLoggingInterceptor,
                                authInterceptor: AuthInterceptor?): OkHttpClient = OkHttpClient.Builder()
        .run {
            if (null != authInterceptor) {
                addInterceptor(authInterceptor)
            }
            addInterceptor(interceptor)
            build()
        }

internal class AuthInterceptor(private val token: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
        val newRequest = request().newBuilder().run {
            addHeader("Authorization", "token $token")
            build()
        }
        proceed(newRequest)
    }
}
