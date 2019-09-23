package com.lmgy.redirect.event

import com.lmgy.redirect.db.data.HostData

/**
 * @author lmgy
 * @date 2019/9/23
 */
data class HostDataEvent (
        val type: Int,
        val message: String,
        val hostData: HostData
)