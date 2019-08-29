package com.lmgy.redirect.ui.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.lmgy.redirect.R;
import com.lmgy.redirect.bean.HostData;
import com.lmgy.redirect.utils.SPUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChangeSettingActivity";
    private AppCompatEditText mIpAddress;
    private AppCompatEditText mHostname;
    private AppCompatEditText mRemark;
    private Button mBtnSave;

    private int mId;
    private HostData hostData;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_setting);
        initView();

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.action_edit));

        hostData = (HostData) getIntent().getSerializableExtra("hostData");
        mId = getIntent().getIntExtra("id", -1);

        if (hostData != null) {
            mIpAddress.setText(hostData.getIpAddress());
            mHostname.setText(hostData.getHostName());
            mRemark.setText(hostData.getRemark());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }

    private boolean isIP(String address) {
        if (address.length() < 7 || address.length() > 15) {
            return false;
        }
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(address);
        boolean ipAddress = mat.find();
        if (ipAddress) {
            String ips[] = address.split("\\.");
            if (ips.length == 4) {
                try {
                    for (String ip : ips) {
                        if (Integer.parseInt(ip) < 0 || Integer.parseInt(ip) > 255) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        return ipAddress;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_delete:
                List<HostData> dataList = SPUtils.getDataList(this, "hostList", HostData.class);
                if (hostData != null){
                    for(HostData it: dataList){
                        if(it.getIpAddress().equals(hostData.getIpAddress()) && it.getHostName().equals(hostData.getHostName())){
                            dataList.remove(it);
                            break;
                        }
                    }
                    SPUtils.setDataList(this, "hostList", dataList);
                    Intent i = new Intent();
                    i.putExtra("result", getString(R.string.delete_successful));
                    setResult(3, i);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mIpAddress = (AppCompatEditText) findViewById(R.id.ipAddress);
        mHostname = (AppCompatEditText) findViewById(R.id.hostname);
        mRemark = (AppCompatEditText) findViewById(R.id.remark);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mBtnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if (isIP(mIpAddress.getText().toString())) {
                    if (!mHostname.getText().toString().isEmpty()) {
                        hostData = new HostData(true, mIpAddress.getText().toString(), mHostname.getText().toString(), mRemark.getText().toString());
                        saveOrUpdate(hostData);
                    } else {
                        Snackbar.make(mCoordinatorLayout, getString(R.string.input_correct_hostname), Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(mCoordinatorLayout, getString(R.string.input_correct_ip), Snackbar.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void saveOrUpdate(final HostData savedHostData) {
        final List<HostData> hostDataList = SPUtils.getDataList(this, "hostList", HostData.class);
        if (mId != -1) {//覆盖
            HostData hostData = hostDataList.get(mId);
            hostData.setIpAddress(savedHostData.getIpAddress());
            hostData.setHostName(savedHostData.getHostName());
            hostData.setType(savedHostData.getType());
            hostData.setRemark(savedHostData.getRemark());
        } else {//新建
            hostDataList.add(savedHostData);
        }
        SPUtils.setDataList(this, "hostList", hostDataList);
        Intent i = new Intent();
        i.putExtra("result", getString(R.string.save_successful));
        setResult(3, i);
        finish();
    }
}
