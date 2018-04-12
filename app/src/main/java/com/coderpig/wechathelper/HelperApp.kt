package com.coderpig.wechathelper

import android.app.Application
import com.orhanobut.hawk.Hawk
import kotlin.properties.Delegates

/**
 * 描述：
 *
 * @author CoderPig on 2018/04/12 11:43.
 */
class HelperApp : Application() {
    companion object {
        var instance by Delegates.notNull<HelperApp>()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(this).build()
    }
}