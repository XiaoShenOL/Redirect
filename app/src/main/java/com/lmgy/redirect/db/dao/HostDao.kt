package com.lmgy.redirect.db.dao

import androidx.room.*
import com.lmgy.redirect.db.data.HostData

/**
 * @author lmgy
 * @date 2019/8/30
 */
@Dao
interface HostDao {
    @Insert
    fun insertHost(hostData: HostData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateHost(hostData: HostData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAll(hostData: MutableList<HostData>)

    @Delete
    fun deleteHost(hostData: HostData)

    @Delete
    fun deleteAll(hostData: MutableList<HostData>)

    @Query("SELECT * FROM HostData")
    fun getAllHosts(): MutableList<HostData>


}