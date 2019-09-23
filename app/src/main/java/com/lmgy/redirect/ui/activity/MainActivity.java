package com.lmgy.redirect.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.lmgy.livedatabus.LiveDataBus;
import com.lmgy.redirect.BuildConfig;
import com.lmgy.redirect.R;
import com.lmgy.redirect.event.MessageEvent;

/**
 * @author lmgy
 * @date 2019/8/14
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;
    private AppBarConfiguration mAppBarConfiguration;
    private Fragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setSupportActionBar(mToolbar);
        initEvent();

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_rules, R.id.nav_dns,
                R.id.nav_star, R.id.nav_share, R.id.nav_github,
                R.id.nav_about)
                .setDrawerLayout(mDrawerLayout)
                .build();

        fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        ((TextView) mNavView.getHeaderView(0).findViewById(R.id.tv_nav_version)).setText(getString(R.string.nav_version) + " " + BuildConfig.VERSION_NAME);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavView, navController);

        mNavView.setNavigationItemSelectedListener(this);

    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar);
        mNavView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                NavHostFragment.findNavController(fragment).navigate(R.id.nav_home);
                break;
            case R.id.nav_rules:
                NavHostFragment.findNavController(fragment).navigate(R.id.nav_rules);
                break;
            case R.id.nav_dns:
                NavHostFragment.findNavController(fragment).navigate(R.id.nav_dns);
                break;
            case R.id.nav_github:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/lmgy/Redirect"))
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nav_star:
                final String appPackageName = this.getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case R.id.nav_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_text));
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.nav_share)));
                break;
            case R.id.nav_about:
                NavHostFragment.findNavController(fragment).navigate(R.id.nav_about);
                break;
            default:
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void initEvent() {
        LiveDataBus.INSTANCE.with(MessageEvent.class).observe(this, event -> {
            if (event.getType() == 1 || event.getType() == 2) {
                Snackbar.make(mDrawerLayout, event.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

}
