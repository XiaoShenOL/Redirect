package com.lmgy.redirect.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.lmgy.redirect.base.BaseFragment
import com.lmgy.redirect.db.data.HostData
import com.lmgy.redirect.event.MessageEvent
import com.lmgy.redirect.viewmodel.HostViewModel
import com.lmgy.redirect.viewmodel.HostViewModelFactory
import com.lmgy.redirect.viewmodel.Injection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
    private lateinit var mContext: Context
    private lateinit var hostViewModelFactory: HostViewModelFactory
    private lateinit var viewModel: HostViewModel

    private var hostList: MutableList<HostData> = mutableListOf()
    private var mId: Int = -1
    private var hostData: HostData? = null
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val args = ChangeSettingFragmentArgs.fromBundle(it)
            hostData = args.hostData
            mId = args.id
        }
        mContext = this.context ?: requireContext()
        hostViewModelFactory = Injection.provideHostViewModelFactory(mContext)
        viewModel = ViewModelProvider(this, hostViewModelFactory).get(HostViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(com.lmgy.redirect.R.layout.fragment_change_setting, container, false)
        initView(view)
        return view
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            com.lmgy.redirect.R.id.action_delete -> {
                if (hostData != null) {
                    disposable.add(viewModel.delete(hostData!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                EventBus.getDefault().post(MessageEvent(2, getString(com.lmgy.redirect.R.string.delete_successful)))
                                activity?.onBackPressed()
                            })
                }
            }
        }
        return true
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            com.lmgy.redirect.R.id.btn_save -> if (isIP(mIpAddress.text.toString())) {
                if (mHostname.text.toString().isNotEmpty()) {
                    hostData = HostData(true, mIpAddress.text.toString(), mHostname.text.toString(), mRemark.text.toString())
                    saveOrUpdate(hostData!!)
                } else {
                    Snackbar.make(view!!, getString(com.lmgy.redirect.R.string.input_correct_hostname), Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(view!!, getString(com.lmgy.redirect.R.string.input_correct_ip), Snackbar.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
    }

    private fun initView(view: View) {
        mIpAddress = view.findViewById(com.lmgy.redirect.R.id.ipAddress)
        mHostname = view.findViewById(com.lmgy.redirect.R.id.hostname)
        mRemark = view.findViewById(com.lmgy.redirect.R.id.remark)
        mBtnSave = view.findViewById(com.lmgy.redirect.R.id.btn_save)
        mBtnSave.setOnClickListener(this)
    }

    override fun checkStatus() {
        menu?.findItem(com.lmgy.redirect.R.id.nav_rules)?.isChecked = true
        toolbar?.inflateMenu(com.lmgy.redirect.R.menu.menu_select)
        toolbar?.setTitle(com.lmgy.redirect.R.string.action_edit)
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
        if (mId != -1) {
            //覆盖
            disposable.add(viewModel.update(hostData!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        EventBus.getDefault().post(MessageEvent(2, getString(com.lmgy.redirect.R.string.save_successful)))
                        activity?.onBackPressed()
                    })
        } else {
            //新建
            hostList.add(savedHostData)
            disposable.add(viewModel.insert(savedHostData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        EventBus.getDefault().post(MessageEvent(2, getString(com.lmgy.redirect.R.string.save_successful)))
                        activity?.onBackPressed()
                    })
        }
    }
}
