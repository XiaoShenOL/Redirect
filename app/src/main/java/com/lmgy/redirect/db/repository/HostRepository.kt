package com.lmgy.redirect.db.repository

import android.content.Context
import com.lmgy.redirect.db.AppDataBase
import com.lmgy.redirect.db.data.DnsData
import com.lmgy.redirect.db.data.HostData
import io.reactivex.Maybe

/**
 * @author lmgy
 * @date 2019/8/31
 */
object HostRepository {

    fun getAllHosts(context: Context): Maybe<MutableList<HostData>> {
        return AppDataBase.getInstance(context).hostDao().getAllHosts()
    }

    fun getDns(context: Context): Maybe<MutableList<DnsData>> {
        return AppDataBase.getInstance(context).dnsDao().getAllDns();
    }

}