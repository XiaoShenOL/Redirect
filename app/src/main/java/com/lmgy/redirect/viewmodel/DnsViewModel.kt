package com.lmgy.redirect.viewmodel

import androidx.lifecycle.ViewModel
import com.lmgy.redirect.db.dao.DnsDao
import com.lmgy.redirect.db.data.DnsData
import com.lmgy.redirect.db.data.HostData
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * @author lmgy
 * @date 2019/9/1
 */
class DnsViewModel(private val dataSource: DnsDao) : ViewModel() {

    fun getAllDns(): Maybe<MutableList<DnsData>> = dataSource.getAllDns()

    fun insertDns(data: DnsData): Completable = dataSource.insertDns(data)

    fun update(data: DnsData): Completable = dataSource.updateDns(data)

    fun delete(data: DnsData): Completable = dataSource.deleteDns(data)

}