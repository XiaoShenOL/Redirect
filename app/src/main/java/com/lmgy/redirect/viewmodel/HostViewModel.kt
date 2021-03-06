package com.lmgy.redirect.viewmodel

import androidx.lifecycle.ViewModel
import com.lmgy.redirect.db.dao.HostDao
import com.lmgy.redirect.db.data.HostData
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * @author lmgy
 * @date 2019/9/1
 */
class HostViewModel(private val dataSource: HostDao) : ViewModel() {

    fun getAll(): Maybe<MutableList<HostData>> = dataSource.getAllHosts()

    fun insert(data: HostData): Completable = dataSource.insertHost(data)

    fun updateAll(data: MutableList<HostData>): Completable = dataSource.updateAll(data)

    fun update(data: HostData): Completable = dataSource.updateHost(data)

    fun delete(data: HostData): Completable = dataSource.deleteHost(data)
}