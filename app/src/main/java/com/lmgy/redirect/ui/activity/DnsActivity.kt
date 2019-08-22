package com.lmgy.redirect.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.lmgy.redirect.R
import com.lmgy.redirect.bean.DnsBean
import com.lmgy.redirect.net.LocalVpnService
import com.lmgy.redirect.utils.SPUtils
import kotlinx.android.synthetic.main.activity_dns.*


class DnsActivity : AppCompatActivity() {

    private var isEmpty = true
    private var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dns)

        initData()

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = getString(com.lmgy.redirect.R.string.nav_dns)

        val ipv4Array = resources.getStringArray(R.array.dns_ipv4_address)
        val ipv6Array = resources.getStringArray(R.array.dns_ipv6_address)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        pos: Int, id: Long) {
                position = pos
                ipv4.setText(ipv4Array[pos])
                ipv6.setText(ipv6Array[pos])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btn_save.setOnClickListener {
            val tempIpv4 = ipv4.text.toString().trim()
            val tempIpv6 = ipv6.text.toString().trim()

            if (("" == tempIpv4) or ("" == tempIpv6)){
                Snackbar.make(coordinatorLayout, getString(R.string.empty), Snackbar.LENGTH_SHORT).show()
            } else {
                val data: List<DnsBean> = listOf(DnsBean(position, tempIpv4, tempIpv6))
                SPUtils.setDataList(this, "dnsList", data)
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

    private fun initData() {
        val data = getList()
        if (data.isNotEmpty()) {
            isEmpty = false
            spinner.setSelection(data[0].id)
            if (data[0].id != 0) {
                ipv4.setText(data[0].ipv4)
                ipv6.setText(data[0].ipv6)
            } else {
                ipv4.setText("")
                ipv6.setText("")
            }
        }
    }

    private fun getList(): List<DnsBean> = SPUtils.getDataList(this, "dnsList", DnsBean::class.java)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
