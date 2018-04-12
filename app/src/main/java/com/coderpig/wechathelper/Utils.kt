package com.coderpig.wechathelper

import android.widget.Toast

/**
 * 描述：
 *
 * @author CoderPig on 2018/04/12 12:14.
 */
fun shortToast(msg: String) = Toast.makeText(HelperApp.instance, msg, Toast.LENGTH_SHORT).show()

fun longToast(msg: String) = Toast.makeText(HelperApp.instance, msg, Toast.LENGTH_LONG).show()