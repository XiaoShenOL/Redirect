package com.lmgy.redirect.db.repository

import com.lmgy.redirect.db.dao.HostDao
import com.lmgy.redirect.db.data.HostData
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * @author lmgy
 * @date 2019/8/31
 */
class HostRepository private constructor(private val hostDao: HostDao) {

    fun getAllHosts() = hostDao.getAllHosts()

    fun updateHost(hostData: HostData) = hostDao.updateHost(hostData)

    fun updateAll(hostData: MutableList<HostData>) = hostDao.updateAll(hostData)

    fun insertHost(hostData: HostData) = hostDao.insertHost(hostData)

    fun deleteHost(hostData: HostData) = hostDao.deleteHost(hostData)

    fun deleteAll(hostData: MutableList<HostData>) = hostDao.deleteAll(hostData)


    companion object {
        @Volatile
        private var instance: HostRepository? = null

        fun getInstance(hostDao: HostDao): HostRepository =
                instance ?: synchronized(this) {
                    instance ?: HostRepository(hostDao).also {
                        instance = it
                    }
                }
    }
}