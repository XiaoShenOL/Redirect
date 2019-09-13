package com.lmgy.redirect.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lmgy.redirect.db.dao.DnsDao
import com.lmgy.redirect.db.dao.HostDao
import com.lmgy.redirect.db.data.DnsData
import com.lmgy.redirect.db.data.HostData


/**
 * @author lmgy
 * @date 2019/8/31
 */
@Database(entities = [HostData::class, DnsData::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {

    abstract fun hostDao(): HostDao
    abstract fun dnsDao(): DnsDao

    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: buildDataBase(context).also {
                    instance = it
                }
        }

        private fun buildDataBase(context: Context) =
            Room.databaseBuilder(context, AppDataBase::class.java, "redirectDatabase.db")
                    .build()
    }
}