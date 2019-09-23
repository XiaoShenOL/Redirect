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
import com.lmgy.livedatabus.LiveDataBus
import com.lmgy.redirect.R
import com.lmgy.redirect.base.BaseFragment
import com.lmgy.redirect.db.data.HostData
import com.lmgy.redirect.event.HostDataEvent
import com.lmgy.redirect.event.MessageEvent
import com.lmgy.redirect.viewmodel.HostViewModel
import com.lmgy.redirect.viewmodel.HostViewModelFactory
import com.lmgy.redirect.viewmodel.Injection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern


/**
 * @author lmgy
 * @date 2019/8/30
 */
class ChangeSettingFragment : BaseFragment(), View.OnClickListener, Toolbar.OnMenuItemClickListener {

    private var mIpAddress: AppCompatEditText? = null
    private var mHostname: AppCompatEditText? = null
    private var mRemark: AppCompatEditText? = null
    private var mBtnSave: Button? = null
    private var mContext: Context? = null
    private var hostViewModelFactory: HostViewModelFactory? = null
    private var viewModel: HostViewModel? = null
    private var mView: View? = null

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mContext = this.context ?: requireContext()
        hostViewModelFactory = Injection.provideHostViewModelFactory(requireNotNull(mContext))
        viewModel = ViewModelProvider(this, requireNotNull(hostViewModelFactory)).get(HostViewModel::class.java)

        mView = inflater.inflate(R.layout.fragment_change_setting, container, false)
        initView(requireNotNull(mView))
        return mView
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_delete -> {
                if (hostData != null) {
                    disposable.add(requireNotNull(viewModel).delete(requireNotNull(hostData))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                LiveDataBus.with(HostDataEvent::class.java).post(HostDataEvent(1, getString(R.string.delete_successful), requireNotNull(hostData)))
                                activity?.onBackPressed()
                            })
                }
            }
        }
        return true
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_save -> if (isIP(mIpAddress?.text.toString())) {
                if (mHostname?.text.toString().isNotEmpty()) {
                    hostData = HostData(true, mIpAddress?.text.toString(), mHostname?.text.toString(), mRemark?.text.toString())
                    saveOrUpdate(hostData)
                } else {
                    Snackbar.make(requireNotNull(mView), getString(com.lmgy.redirect.R.string.input_correct_hostname), Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(requireNotNull(mView), getString(com.lmgy.redirect.R.string.input_correct_ip), Snackbar.LENGTH_SHORT).show()
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
        mBtnSave?.setOnClickListener(this)
    }

    override fun checkStatus() {
        menu?.findItem(R.id.nav_rules)?.isChecked = true
        toolbar?.inflateMenu(R.menu.menu_select)
        toolbar?.setTitle(com.lmgy.redirect.R.string.action_edit)
        toolbar?.setOnMenuItemClickListener(this)
    }

    override fun initData() {
        mIpAddress?.setText(hostData?.ipAddress)
        mHostname?.setText(hostData?.hostName)
        mRemark?.setText(hostData?.remark)
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

    override fun onDestroyView() {
        super.onDestroyView()
        mIpAddress = null
        mHostname = null
        mRemark = null
        mBtnSave = null
        mContext = null
        hostViewModelFactory = null
        viewModel = null
        mView = null
    }

    private fun saveOrUpdate(savedHostData: HostData?) {
        if (mId != -1) {
            //覆盖
            disposable.add(requireNotNull(viewModel).update(requireNotNull(hostData))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        LiveDataBus.with(MessageEvent::class.java).post(MessageEvent(2, getString(R.string.save_successful)))
                        activity?.onBackPressed()
                    })
        } else {
            //新建
            hostList.add(requireNotNull(savedHostData))
            disposable.add(requireNotNull(viewModel).insert(requireNotNull(savedHostData))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        LiveDataBus.with(MessageEvent::class.java).post(MessageEvent(2, getString(R.string.save_successful)))
                        activity?.onBackPressed()
                    })
        }
    }
}
