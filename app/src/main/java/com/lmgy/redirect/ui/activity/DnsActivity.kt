package com.lmgy.redirect.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.lmgy.redirect.R
import com.lmgy.redirect.bean.DnsBean
import com.lmgy.redirect.net.LocalVpnService
import com.lmgy.redirect.utils.SPUtils
import kotlinx.android.synthetic.main.activity_dns.*


class DnsActivity : AppCompatActivity() {

    private var position = -1

    // UI
    private lateinit var spinner: Spinner
    private lateinit var btnSave: Button
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var data: List<DnsBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dns)
        initView()
        initData()

        val ipv4Array = resources.getStringArray(R.array.dns_ipv4_address)
        val ipv6Array = resources.getStringArray(R.array.dns_ipv6_address)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
                position = pos
                if (pos == 0) {
                    if (data.isNotEmpty() && data[0].id == 0) {
                        ipv4.setText(data[0].ipv4)
                        ipv6.setText(data[0].ipv6)
                        return
                    }
                    ipv4.setText("")
                    ipv6.setText("")
                } else {
                    ipv4.setText(ipv4Array[pos])
                    ipv6.setText(ipv6Array[pos])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnSave.setOnClickListener {
            val tempIpv4 = (ipv4.text?.toString() ?: "").trim()
            val tempIpv6 = (ipv6.text?.toString() ?: "").trim()

            if (tempIpv4.isEmpty() || tempIpv6.isEmpty()) {
                Snackbar.make(coordinatorLayout, getString(R.string.empty), Snackbar.LENGTH_SHORT).show()
            } else {
                val data: List<DnsBean> = listOf(DnsBean(position, tempIpv4, tempIpv6))
                SPUtils.setDataList(this@DnsActivity, "dnsList", data)
                val intent = Intent()
                if (LocalVpnService.isRunning()) {
                    intent.putExtra("result", getString(R.string.save_successful_restart))
                } else {
                    intent.putExtra("result", getString(R.string.save_successful))
                }
                setResult(-1, intent)
                finish()
            }
        }
    }

    private fun initView() {
        spinner = findViewById(R.id.spinner)
        btnSave = findViewById(R.id.btn_save)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = getString(R.string.nav_dns)
    }

    private fun initData() {
        data = getList()
        if (data.isNotEmpty()) {
            spinner.setSelection(data[0].id)
            ipv4.setText(data[0].ipv4)
            ipv6.setText(data[0].ipv6)
        }

    }

    private fun getList(): List<DnsBean> = SPUtils.getDataList(this@DnsActivity, "dnsList", DnsBean::class.java)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
