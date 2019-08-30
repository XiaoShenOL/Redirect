package com.lmgy.redirect.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import com.lmgy.redirect.R
import com.lmgy.redirect.base.BaseFragment

/**
 * @author lmgy
 * @date 2019/8/29
 */
class AboutFragment : BaseFragment() {

    private lateinit var mActivity: FragmentActivity
    private lateinit var mWvAbout: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this.activity ?: requireActivity()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        mWvAbout = view.findViewById(R.id.wv_about)
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    override fun initData() {
        mWvAbout.settings.javaScriptEnabled = true
        mWvAbout.setBackgroundColor(0)
        mWvAbout.addJavascriptInterface(this, "JavascriptInterface")
        mWvAbout.loadUrl("file:///android_asset/about_html/index.html")

        mWvAbout.setOnLongClickListener { true }

        mWvAbout.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                try {
                    view.loadUrl("javascript:changeColor('#000000')")
                    view.loadUrl("javascript:changeVersionInfo('" + mActivity.packageManager?.getPackageInfo(mActivity.packageName!!, 0)?.versionName + "')")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun checkStatus() {
        menu?.findItem(R.id.nav_about)?.isChecked = true
        toolbar?.setTitle(R.string.nav_about)
    }

    override fun onDestroy() {
        super.onDestroy()
        mWvAbout.removeAllViews()
        mWvAbout.webViewClient = null
        mWvAbout.tag = null
        mWvAbout.clearHistory()
        mWvAbout.destroy()
    }

}
