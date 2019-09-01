package com.lmgy.redirect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lmgy.redirect.db.dao.HostDao

/**
 * @author lmgy
 * @date 2019/9/1
 */
class ViewModelFactory(private val dataSource: HostDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HostViewModel::class.java)) {
            return HostViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}