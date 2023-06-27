package com.mikirinkode.firebasechatapp.base.presenter

interface BasePresenter<V> {
    fun attachView(view: V)
    fun detachView()
}