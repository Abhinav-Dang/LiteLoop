package com.example.liteloop.util

import android.util.Log
import com.example.liteloop.BuildConfig

object LLog {
    private const val TAG = "LiteLoop"

    fun d(subTag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[$subTag] $message")
        }
    }

    fun e(subTag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "[$subTag] $message", throwable)
        }
    }

    fun w(subTag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "[$subTag] $message")
        }
    }
}
