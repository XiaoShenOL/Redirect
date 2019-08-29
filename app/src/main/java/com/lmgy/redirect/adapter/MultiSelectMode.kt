package com.lmgy.redirect.adapter

/*
 * Created by lmgy on 2019/8/29
 */
interface MultiSelectMode<T> {

    fun getSelectedItemsId(): List<T>

    fun addSelectedItemId(selectedItemId: T)

    fun removeSelectedItemId(selectedItemId: T)

    fun removeAllSelected()

    fun getSelectedItemsCount(): Int

    fun isSomethingSelected(): Boolean
}