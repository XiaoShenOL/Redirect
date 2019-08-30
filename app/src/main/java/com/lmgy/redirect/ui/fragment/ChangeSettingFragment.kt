package com.lmgy.redirect.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.lmgy.redirect.R
import com.lmgy.redirect.base.BaseFragment
import com.lmgy.redirect.bean.HostData
import com.lmgy.redirect.event.MessageEvent
import com.lmgy.redirect.utils.SPUtils
import org.greenrobot.eventbus.EventBus
import java.util.regex.Pattern

/**
 * @author lmgy
 * @date 2019/8/30
 */
class ChangeSettingFragment : BaseFragment(), View.OnClickListener, Toolbar.OnMenuItemClickListener {


    private lateinit var mIpAddress: AppCompatEditText
    private lateinit var mHostname: AppCompatEditText
    private lateinit var mRemark: AppCompatEditText
    private lateinit var mBtnSave: Button
    private var mId: Int = -1
    private var hostData: HostData? = null
    private lateinit var mContext: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hostData = it.getSerializable("hostData") as? HostData
            mId = it.getInt("id", -1)
        }
        mContext = this.context ?: requireContext()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_change_setting, container, false)
        initView(view)
        return view
    }


    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_delete -> {
                val dataList = SPUtils.getDataList(mContext, "hostList", HostData::class.java)
                if (hostData != null) {
                    for (it in dataList) {
                        if (it.ipAddress == hostData?.ipAddress && it.hostName == hostData?.hostName) {
                            dataList.remove(it)
                            break
                        }
                    }
                    SPUtils.setDataList(mContext, "hostList", dataList)
                    EventBus.getDefault().post(MessageEvent(2, getString(R.string.delete_successful)))
                    activity?.onBackPressed()
                }
            }
        }
        return true
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_save -> if (isIP(mIpAddress.text.toString())) {
                if (mHostname.text.toString().isNotEmpty()) {
                    hostData = HostData(true, mIpAddress.text.toString(), mHostname.text.toString(), mRemark.text.toString())
                    saveOrUpdate(hostData!!)
                } else {
                    Snackbar.make(view!!, getString(R.string.input_correct_hostname), Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(view!!, getString(R.string.input_correct_ip), Snackbar.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }


    private fun initView(view: View) {
        mIpAddress = view.findViewById(R.id.ipAddress)
        mHostname = view.findViewById(R.id.hostname)
        mRemark = view.findViewById(R.id.remark)
        mBtnSave = view.findViewById(R.id.btn_save)
        mBtnSave.setOnClickListener(this)
    }

    override fun checkStatus() {
        menu?.findItem(R.id.nav_rules)?.isChecked = true
        toolbar?.inflateMenu(R.menu.menu_select)
        toolbar?.setTitle(R.string.action_edit)
        toolbar?.setOnMenuItemClickListener(this)
    }

    override fun initData() {
        mIpAddress.setText(hostData?.ipAddress)
        mHostname.setText(hostData?.hostName)
        mRemark.setText(hostData?.remark)
    }

    private fun isIP(address: String): Boolean {
        if (address.length < 7 || address.length > 15) {
            return false
        }
        val rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
        val pat = Pattern.compile(rexp)
        val mat = pat.matcher(address)
        val ipAddress = mat.find()
        if (ipAddress) {
            val ips = address.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (ips.size == 4) {
                try {
                    for (ip in ips) {
                        if (Integer.parseInt(ip) < 0 || Integer.parseInt(ip) > 255) {
                            return false
                        }
                    }
                } catch (e: Exception) {
                    return false
                }
                return true
            } else {
                return false
            }
        }
        return ipAddress
    }

    private fun saveOrUpdate(savedHostData: HostData) {
        val hostDataList = SPUtils.getDataList(activity, "hostList", HostData::class.java)
        e("asd", "" + mId + " - " + hostDataList.size)
        if (mId != -1) {//覆盖
            val hostData = hostDataList[mId]
            hostData.ipAddress = savedHostData.ipAddress
            hostData.hostName = savedHostData.hostName
            hostData.type = savedHostData.type
            hostData.remark = savedHostData.remark
        } else {//新建
            hostDataList.add(savedHostData)
        }
        SPUtils.setDataList(mContext, "hostList", hostDataList)
        EventBus.getDefault().post(MessageEvent(2, getString(R.string.save_successful)))
        activity?.onBackPressed()
    }

}
