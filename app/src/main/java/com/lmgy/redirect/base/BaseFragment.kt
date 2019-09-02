package com.lmgy.redirect.base


import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.lmgy.redirect.R

/*
 * Created by lmgy on 2019/8/29
 */
abstract class BaseFragment : Fragment() {
    protected var toolbar: Toolbar? = null
    protected var menu: Menu? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menu = (activity?.findViewById(R.id.nav_view) as NavigationView).menu
        toolbar = activity?.findViewById(R.id.toolbar)
        toolbar?.menu?.clear()
        checkStatus()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()

        toolbar = null
        menu = null
    }

    abstract fun initData()
    abstract fun checkStatus()
}