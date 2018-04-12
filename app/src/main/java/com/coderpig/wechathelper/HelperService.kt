package com.coderpig.wechathelper

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.orhanobut.hawk.Hawk


/**
 * 描述：无障碍服务类
 *
 * @author CoderPig on 2018/04/12 13:47.
 */
class HelperService : AccessibilityService() {

    private val TAG = "HelperService"
    private val handler = Handler()
    private var nameList = mutableListOf<String>()

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()
        Log.d(TAG, event.toString())
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (Hawk.get(Constant.RED_PACKET, false)) {
                    when (className) {
                        "com.tencent.mm.ui.LauncherUI" -> openRedPacket()
                        "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI" -> clickRedPacket()
                        "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI" -> performBackClick()
                    }
                    //com.tencent.mm:id/ad8
                }
                if (Hawk.get(Constant.ADD_FRIENDS, false) && Hawk.get(Constant.GROUP_NAME, "") != "") {
                    when (className) {
                        "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI" -> addFriends()
                        "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI" -> verifyFriend()
                        "com.tencent.mm.plugin.profile.ui.ContactInfoUI" -> contactInfo()
                        "com.tencent.mm.ui.LauncherUI" -> openGroup()
                        "com.tencent.mm.ui.contact.ChatroomContactUI" -> {
                            if (nameList.size > 0) searchGroup() else performBackClick()
                        }
                        "com.tencent.mm.ui.chatting.ChattingUI" -> openGroupSetting()
                        "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI" -> {
                            if (nameList.size > 0) addToGroup() else performBackClick()
                        }
                        "com.tencent.mm.ui.base.i" -> dialogClick()
                    }
                }
                if (Hawk.get(Constant.FRIEND_SQUARE)) {
                    if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI") {
                        autoZan()
                    }
                }

            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                if (event.parcelableData != null && event.parcelableData is Notification) {
                    val notification = event.parcelableData as Notification
                    val content = notification.tickerText.toString()
                    if (content.contains("[微信红包]")) {
                        val pendingIntent = notification.contentIntent
                        try {
                            pendingIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }

                    }
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if(className == "android.widget.TextView") {
                    if(getRunningActivityName() == "com.tencent.mm.ui.LauncherUI") {
                        openRedPacket()
                    }
                }
            }
        }
    }

    //添加好友
    private fun addFriends() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val list = nodeInfo.findAccessibilityNodeInfosByText("接受")
            if (list != null && list.size > 0) {
                list[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                val nameText: List<AccessibilityNodeInfo>? = list[0].parent.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b8s")
                nameList.add(nameText?.get(0)?.text.toString())
            } else {
                performBackClick()
            }
        }
    }

    //完成验证
    private fun verifyFriend() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val finishNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hh")[0]
            finishNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    //好友详细资料页
    private fun contactInfo() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nameNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/q0")[0]
            Log.i(TAG, nameNode.toString())
            if (nameList.contains(nameNode.text.toString().trim())) performBackClick()
        }
    }

    //打开群聊
    private fun openGroup() {
        if (nameList.size > 0) {
            val nodeInfo = rootInActiveWindow
            if (nodeInfo != null) {
                val tabNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c9f")
                for (tabNode in tabNodes) {
                    if (tabNode.text.toString() == "通讯录") {
                        tabNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        handler.postDelayed({
                            val newNodeInfo = rootInActiveWindow
                            if (newNodeInfo != null) {
                                val tagNodes = newNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jk")
                                for (tagNode in tagNodes) {
                                    if (tagNode.text.toString() == "群聊") {
                                        tagNode.parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        break
                                    }
                                }
                            }
                        }, 500L)
                    }
                }
            }
        }
    }

    //搜索群聊
    private fun searchGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a9t")
            for (info in nodes) {
                if (info.text.toString() == Hawk.get(Constant.GROUP_NAME)) {
                    info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    break
                }
            }
        }
    }

    //打开群聊设置
    private fun openGroupSetting() {
        if (nameList.size > 0) {
            val nodeInfo = rootInActiveWindow
            if (nodeInfo != null) {
                nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hi")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    //添加到群聊里
    private fun addToGroup() {
        if (nameList.size > 0) {
            val nodeInfo = rootInActiveWindow
            if (nodeInfo != null) {
                val listNodes = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/list")
                if (listNodes != null && listNodes.size > 0) {
                    val listNode = listNodes[0]
                    listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    val scrollNodeInfo = rootInActiveWindow
                    if (scrollNodeInfo != null) {
                        handler.postDelayed({
                            val nodes = scrollNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cz1")
                            for (info in nodes) {
                                if (info.contentDescription.toString() == "添加成员") {
                                    info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    break
                                }
                            }
                        }, 1000L)
                        handler.postDelayed({
                            val editNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/arx")
                            if (editNodes != null && editNodes.size > 0) {
                                val editNode = editNodes[0]
                                val arguments = Bundle()
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, nameList[0])
                                editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                                nameList.removeAt(0)
                            }
                        }, 2300L)
                        handler.postDelayed({
                            val cbNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/l7")
                            if (cbNodes != null) {
                                var cbNode: AccessibilityNodeInfo? = null
                                if (cbNodes.size == 1) {
                                    cbNode = cbNodes[0]
                                } else if (cbNodes.size == 2) {
                                    cbNode = cbNodes[1]
                                }
                                if (cbNode != null) {
                                    cbNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    val sureNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hh")[0]
                                    sureNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }
                        }, 3000L)
                    }
                }
            }
        }
    }

    //对话框处理
    private fun dialogClick() {
        val inviteNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/all")[0]
        inviteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        handler.postDelayed({
            val sureNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/all")
            if (sureNodes != null && sureNodes.size > 0) {
                val sureNode = sureNodes[0]
                sureNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }, 1000L)
    }

    //自动点赞
    private fun autoZan() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            while (true) {
                val listNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ddn")
                if (listNodes != null && listNodes.size > 0) {
                    var listNode = listNodes[0]
                    val zanNodes = listNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dao")
                    for (zan in zanNodes) {
                        zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(500)
                        var zsNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d_m")
                        Thread.sleep(500)
                        if (zsNodes != null && zsNodes.size > 0) {
                            if (zsNodes[0].findAccessibilityNodeInfosByText("赞").size > 0) {
                                zsNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                        Thread.sleep(500)
                    }
                    listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                }

            }
        }
    }

    //遍历获得未打开红包
    private fun openRedPacket() {
        val listNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a_c")
        if (listNode != null && listNode.size > 0) {
            val msgNodes = listNode[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ad8")
            if (msgNodes != null && msgNodes.size > 0) {
                val rpNode = msgNodes[msgNodes.size - 1]
                val rpStatusNode = rpNode.findAccessibilityNodeInfosByText("领取红包")
                if (rpStatusNode != null && rpStatusNode.size > 0) {
                    rpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    //打开红包
    private fun clickRedPacket() {
        val nodeInfo = rootInActiveWindow
        val clickNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c31")
        if (clickNode != null && clickNode.size > 0) {
            clickNode[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            performBackClick()
        }
    }



    //遍历控件的方法
    fun recycle(info: AccessibilityNodeInfo) {
        if (info.childCount == 0) {
            Log.i(TAG, "child widget----------------------------" + info.className.toString())
            Log.i(TAG, "showDialog:" + info.canOpenPopup())
            Log.i(TAG, "Text：" + info.text)
            Log.i(TAG, "windowId:" + info.windowId)
            Log.i(TAG, "desc:" + info.contentDescription)
        } else {
            (0 until info.childCount)
                    .filter { info.getChild(it) != null }
                    .forEach { recycle(info.getChild(it)) }
        }
    }

    private fun performBackClick() {
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 300L)
    }

    private fun getRunningActivityName(): String {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningTasks(1)[0].topActivity.className
    }

}