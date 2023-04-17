package com.mikirinkode.firebasechatapp.base

interface BasePresenter<V> {
    fun attachView(view: V)
    fun detachView()
}