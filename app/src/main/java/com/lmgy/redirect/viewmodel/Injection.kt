package com.lmgy.redirect.viewmodel

import android.content.Context
import com.lmgy.redirect.db.AppDataBase
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

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideUserDataSource(context)
        return ViewModelFactory(dataSource)
    }
}