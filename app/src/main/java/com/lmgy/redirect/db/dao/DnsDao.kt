package com.lmgy.redirect.db.dao

import androidx.room.*
import com.lmgy.redirect.db.data.DnsData
import io.reactivex.Completable
import io.reactivex.Maybe

/**
 * @author lmgy
 * @date 2019/9/1
 */
@Dao
interface DnsDao {

    @Insert
    fun insertDns(dnsData: DnsData): Completable

    @Delete
    fun deleteDns(dnsData: DnsData): Completable

    @Query("SELECT * FROM DnsData")
    fun getAllDns(): Maybe<MutableList<DnsData>>

    @Update
    fun updateDns(dnsData: DnsData): Completable

}