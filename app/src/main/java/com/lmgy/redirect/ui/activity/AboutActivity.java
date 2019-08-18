package com.lmgy.redirect.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lmgy.redirect.R;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    private WebView mWvAbout;

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "addJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.nav_about));

        mWvAbout.getSettings().setJavaScriptEnabled(true);
        mWvAbout.setBackgroundColor(0);
        mWvAbout.addJavascriptInterface(this, "JavascriptInterface");
        mWvAbout.loadUrl("file:///android_asset/about_html/index.html");

        mWvAbout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        mWvAbout.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    view.loadUrl("javascript:changeColor('#000000')");
                    view.loadUrl("javascript:changeVersionInfo('" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "')");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWvAbout != null) {
            mWvAbout.removeAllViews();
            mWvAbout.setWebViewClient(null);
            mWvAbout.setTag(null);
            mWvAbout.clearHistory();
            mWvAbout.destroy();
            mWvAbout = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mWvAbout = (WebView) findViewById(R.id.wv_about);
    }
}
