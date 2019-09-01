package com.lmgy.redirect.db.dao

import androidx.room.*
import com.lmgy.redirect.db.data.HostData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * @author lmgy
 * @date 2019/8/30
 */
@Dao
interface HostDao {

    @Insert
    fun insertHost(hostData: HostData): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateHost(hostData: HostData): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(hostData: MutableList<HostData>): Completable

    @Delete
    fun deleteHost(hostData: HostData): Completable

    @Delete
    fun deleteAll(hostData: MutableList<HostData>): Completable

    @Query("SELECT * FROM HostData")
    fun getAllHosts(): Maybe<MutableList<HostData>>

}