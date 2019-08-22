package com.lmgy.redirect.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.kyleduo.switchbutton.SwitchButton;
import com.lmgy.redirect.BuildConfig;
import com.lmgy.redirect.R;
import com.lmgy.redirect.bean.HostData;
import com.lmgy.redirect.net.LocalVpnService;
import com.lmgy.redirect.utils.SPUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int VPN_REQUEST_CODE = 0x0F;
    private static final int DNS_REQUEST_CODE = 0;

    private Toolbar mToolbar;
    private SwitchButton mBtnVpn;
    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;

    private boolean waitingForVPNStart;
    private BroadcastReceiver vpnStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocalVpnService.BROADCAST_VPN_STATE.equals(intent.getAction()) && intent.getBooleanExtra("running", false)) {
                waitingForVPNStart = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavView.setNavigationItemSelectedListener(this);
        ((TextView) mNavView.getHeaderView(0).findViewById(R.id.tv_nav_version)).setText(getString(R.string.nav_version) + " " + BuildConfig.VERSION_NAME);

        mBtnVpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (checkHost() == -1) {
                        showDialog();
                    } else {
                        startVPN();
                    }
                } else {
                    shutdownVPN();
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(vpnStateReceiver,
                new IntentFilter(LocalVpnService.BROADCAST_VPN_STATE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setButton(!waitingForVPNStart && !LocalVpnService.isRunning());
    }


    private void startVPN() {
        waitingForVPNStart = false;
        Intent vpnIntent = LocalVpnService.prepare(this);
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    private int checkHost() {
        List<HostData> list = SPUtils.getDataList(this, "hostList", HostData.class);
        if (list.size() == 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private void shutdownVPN() {
        if (LocalVpnService.isRunning()) {
            startService(new Intent(this, LocalVpnService.class).setAction(LocalVpnService.ACTION_DISCONNECT));
        }
        setButton(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            waitingForVPNStart = true;
            startService(new Intent(this, LocalVpnService.class).setAction(LocalVpnService.ACTION_CONNECT));
            setButton(false);
        } else if (requestCode == DNS_REQUEST_CODE && resultCode == RESULT_OK) {
            Snackbar.make(mDrawerLayout, data.getStringExtra("result"), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void setButton(boolean enable) {
        mBtnVpn.setChecked(!enable);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setButton(true);
                        startActivity(new Intent(getApplicationContext(), HostSettingActivity.class));
                    }
                })
                .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setButton(true);
                    }
                })
                .show();
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_rules) {
            startActivity(new Intent(getApplicationContext(), HostSettingActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        } else if (id == R.id.nav_github) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/lmgy/Redirect"))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.nav_dns) {
            startActivityForResult(new Intent(getApplicationContext(), DnsActivity.class), 0);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mBtnVpn = (SwitchButton) findViewById(R.id.btn_vpn);
        mNavView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }

}
