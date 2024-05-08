package com.app.myrecipes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.myrecipes.R;
import com.app.myrecipes.callbacks.CallbackSetting;
import com.app.myrecipes.config.AppConfig;
import com.app.myrecipes.databases.prefs.SharedPref;
import com.app.myrecipes.models.Setting;
import com.app.myrecipes.rests.ApiInterface;
import com.app.myrecipes.rests.RestAdapter;
import com.app.myrecipes.utils.Constant;
import com.app.myrecipes.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPrivacyPolicy extends AppCompatActivity {

    private SwipeRefreshLayout swipe_refresh;
    private ShimmerFrameLayout lyt_shimmer;
    SharedPref sharedPref;
    Setting post;

    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_privacy_policy);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);

        swipe_refresh = findViewById(R.id.swipe_refresh);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        txt=findViewById(R.id.ttg);

        txt.setOnClickListener(v -> {
            Intent intent = new Intent(this, Selection.class);
            startActivity(intent);
        });

        requestAction();

        swipe_refresh.setOnRefreshListener(this::requestAction);

        setupToolbar();

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.title_setting_privacy));
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(this::requestDetailsPostApi, Constant.DELAY_TIME);
    }

    private void requestDetailsPostApi() {
        ApiInterface api = RestAdapter.createAPI(sharedPref.getApiUrl());
        Call<CallbackSetting> callbackCall = api.getSettings(AppConfig.REST_API_KEY);
        callbackCall.enqueue(new Callback<CallbackSetting>() {
            @Override
            public void onResponse(Call<CallbackSetting> call, Response<CallbackSetting> response) {
                CallbackSetting resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post = resp.post;
                    displayPostData();
                    swipeProgress(false);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackSetting> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        RelativeLayout lyt_main_content = findViewById(R.id.lyt_main_content);

        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_main_content.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_main_content.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (findViewById(R.id.failed_retry)).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        });
    }

    public void displayPostData() {
        WebView webView = findViewById(R.id.webview_privacy_policy);
        Tools.displayPostDescription(this, webView, post.privacy_policy);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
