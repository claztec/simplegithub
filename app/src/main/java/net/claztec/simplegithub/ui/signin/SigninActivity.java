package net.claztec.simplegithub.ui.signin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.claztec.simplegithub.BuildConfig;
import net.claztec.simplegithub.R;
import net.claztec.simplegithub.api.AuthApi;
import net.claztec.simplegithub.api.GithubApi;
import net.claztec.simplegithub.api.GithubApiProvider;
import net.claztec.simplegithub.api.model.GithubAccessToken;
import net.claztec.simplegithub.data.AuthTokenProvider;
import net.claztec.simplegithub.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigninActivity extends AppCompatActivity {
    private static final String TAG = "SigninActivity";

    Button btnStart;

    ProgressBar progressBar;

    AuthApi api;

    AuthTokenProvider authTokenProvider;

    Call<GithubAccessToken> accessTokenCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        btnStart = findViewById(R.id.btnActivitySignInStart);
        progressBar = findViewById(R.id.pbActivitySignIn);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri authUri = new Uri.Builder().scheme("https").authority("github.com")
                        .appendPath("login")
                        .appendPath("oauth")
                        .appendPath("authorize")
                        .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
                        .build();

                CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                intent.launchUrl(SigninActivity.this, authUri);
            }
        });

        api = GithubApiProvider.provideAuthApi();
        authTokenProvider = new AuthTokenProvider(this);

        Log.d(TAG, "token: " + authTokenProvider.getToken());

        if (null != authTokenProvider.getToken()) {
            launchMainActivity();
        }


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.d(TAG, "온 뉴 인텐트에서: ");

        showProgress();
        Uri uri = intent.getData();
        if (null == uri) {
            throw new IllegalArgumentException("No data exist");
        }

        String code = uri.getQueryParameter("code");
        if (null == code) {
            throw new IllegalStateException("No code exist");
        }

        getAccessToken(code);
    }

    private void getAccessToken(@NonNull String code) {
        showProgress();

        accessTokenCall = api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code);

        accessTokenCall.enqueue(new Callback<GithubAccessToken>() {
            @Override
            public void onResponse(Call<GithubAccessToken> call, Response<GithubAccessToken> response) {
                hideProgress();

                GithubAccessToken token = response.body();
                Log.d(TAG, token.accessToken);
                if (response.isSuccessful() && null != token) {
                    authTokenProvider.updateToken(token.accessToken);
                    launchMainActivity();
                } else {
                    showError(new IllegalStateException("Not Successfull: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<GithubAccessToken> call, Throwable t) {
                hideProgress();
                showError(t);
            }
        });
    }

    private void showError(Throwable throwable) {
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();

    }

    private void hideProgress() {
        btnStart.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void showProgress() {
        btnStart.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void launchMainActivity() {
        Log.d(TAG, "런치 메인 액티비티");
        startActivity(new Intent(SigninActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

}
