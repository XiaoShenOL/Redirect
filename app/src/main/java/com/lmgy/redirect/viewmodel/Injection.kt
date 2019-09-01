package com.lmgy.redirect.viewmodel

import android.content.Context
import com.lmgy.redirect.db.AppDataBase
import com.lmgy.redirect.db.dao.DnsDao
import com.lmgy.redirect.db.dao.HostDao

/**
 * @author lmgy
 * @date 2019/9/1
 */
object Injection {

    private fun provideUserDataSource(context: Context): HostDao {
        val database = AppDataBase.getInstance(context)
        return database.hostDao()
    }

    private fun provideDnsDataSource(context: Context): DnsDao {
        val dataBase = AppDataBase.getInstance(context)
        return dataBase.dnsDao()
    }

    fun provideHostViewModelFactory(context: Context): HostViewModelFactory {
        val dataSource = provideUserDataSource(context)
        return HostViewModelFactory(dataSource)
    }

    fun provideDnsViewModelFactory(context: Context): DnsViewModelFactory {
        val dataSource = provideDnsDataSource(context)
        return DnsViewModelFactory(dataSource)
    }
}