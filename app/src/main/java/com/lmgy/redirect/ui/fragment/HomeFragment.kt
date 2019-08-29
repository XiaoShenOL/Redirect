package com.lmgy.redirect.ui.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.kyleduo.switchbutton.SwitchButton
import com.lmgy.redirect.R
import com.lmgy.redirect.base.BaseFragment
import com.lmgy.redirect.bean.HostData
import com.lmgy.redirect.net.LocalVpnService
import com.lmgy.redirect.utils.SPUtils


class HomeFragment : BaseFragment() {

    private var waitingForVPNStart: Boolean = false

    private lateinit var mContext: Context
    private lateinit var mBtnVpn: SwitchButton

    companion object {
        const val VPN_REQUEST_CODE: Int = 0x0F
        const val DNS_REQUEST_CODE: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    private fun initView(view: View) {
        mBtnVpn = view.findViewById(R.id.btn_vpn)
    }

    private fun initData() {
        mBtnVpn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkHost() == -1) {
                    showDialog()
                } else {
                    startVPN()
                }
            } else {
                shutdownVPN()
            }
        }

        LocalBroadcastManager.getInstance(mContext).registerReceiver(vpnStateReceiver,
                IntentFilter(LocalVpnService.BROADCAST_VPN_STATE))
    }

    override fun onResume() {
        super.onResume()
        setButton(!waitingForVPNStart && !LocalVpnService.isRunning())
    }

    private fun startVPN() {
        waitingForVPNStart = false
        val vpnIntent = LocalVpnService.prepare(mContext)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
        } else {
            onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun setButton(enable: Boolean) {
        mBtnVpn.isChecked = !enable
    }

    private val vpnStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocalVpnService.BROADCAST_VPN_STATE == intent.action && intent.getBooleanExtra("running", false)) {
                waitingForVPNStart = false
            }
        }
    }

    private fun checkHost(): Int {
        val list = SPUtils.getDataList(mContext, "hostList", HostData::class.java)
        return if (list.size == 0) {
            -1
        } else {
            1
        }
    }

    override fun checkStatus() {
        menu?.findItem(R.id.nav_home)?.isChecked = true
        toolbar?.title = getString(R.string.nav_home)
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(mContext)
        builder.setCancelable(false)
                .setTitle(getString(R.string.dialog_title))
                .setMessage(getString(R.string.dialog_message))
                .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                    setButton(true)
                    startActivity(Intent(mContext, RulesFragment::class.java))
                }
                .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> setButton(true) }
                .show()
    }

    private fun shutdownVPN() {
        if (LocalVpnService.isRunning()) {
            mContext.startService(Intent(mContext, LocalVpnService::class.java).setAction(LocalVpnService.ACTION_DISCONNECT))
        }
        setButton(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            waitingForVPNStart = true
            mContext.startService(Intent(mContext, LocalVpnService::class.java).setAction(LocalVpnService.ACTION_CONNECT))
            setButton(false)
        } else if (requestCode == DNS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Snackbar.make(view!!, data!!.getStringExtra("result")!!, Snackbar.LENGTH_SHORT).show()
        }
    }
}
