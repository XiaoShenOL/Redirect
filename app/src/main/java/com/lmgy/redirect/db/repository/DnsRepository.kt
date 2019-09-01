package com.lmgy.redirect.db.repository

import com.lmgy.redirect.db.dao.DnsDao
import com.lmgy.redirect.db.data.DnsData

/**
 * @author lmgy
 * @date 2019/9/1
 */
class DnsRepository (private val dnsDao: DnsDao){

    fun getAllDns() = dnsDao.getAllDns()

    fun insertDns(dnsData: DnsData) = dnsDao.insertDns(dnsData)

    fun deleteDns(dnsData: DnsData) = dnsDao.deleteDns(dnsData)

}