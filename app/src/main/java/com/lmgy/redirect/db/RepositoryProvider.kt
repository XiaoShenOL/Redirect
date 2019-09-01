package com.lmgy.redirect.db

import android.content.Context
import com.lmgy.redirect.db.repository.HostRepository

/**
 * @author lmgy
 * @date 2019/8/31
 */
object RepositoryProvider {

    fun providerHostRepository(context: Context): HostRepository {
        return HostRepository.getInstance(AppDataBase.getInstance(context).hostDao())
    }

}