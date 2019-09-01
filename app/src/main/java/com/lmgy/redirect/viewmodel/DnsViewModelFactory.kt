package com.lmgy.redirect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lmgy.redirect.db.dao.DnsDao
import com.lmgy.redirect.db.dao.HostDao

/**
 * @author lmgy
 * @date 2019/9/1
 */
class DnsViewModelFactory(private val dataSource: DnsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DnsViewModel::class.java)) {
            return DnsViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}