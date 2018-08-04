package com.coderpig.wechathelper

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentValues.TAG
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

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()
        Log.d(TAG, event.toString())
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (Hawk.get(Constant.ADD_FRIENDS, false) && Hawk.get(Constant.MEMBER_LIST, mutableListOf<String>()).size > 0) {
                    when (className) {
                        "com.tencent.mm.ui.LauncherUI" -> openGroup()
                        "com.tencent.mm.ui.contact.ChatroomContactUI" -> searchGroup()
                        "com.tencent.mm.ui.chatting.ChattingUI" -> openGroupSetting()
                        "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI" -> openSelectContact()
                        "com.tencent.mm.ui.contact.SelectContactUI" -> addMembers()
                    }
                }
                if (className == "com.tencent.mm.ui.widget.a.c") {
                    dialogClick()
                }
                if (Hawk.get(Constant.FRIEND_SQUARE, false)) {
                    if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI ") {
                        autoZan()
                    }
                }
                if (Hawk.get(Constant.RED_PACKET, false)) {
                    when (className) {
                        "com.tencent.mm.ui.LauncherUI" -> openRedPacket()
                        "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI" -> clickRedPacket()
                        "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI" -> performBackClick()
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

        //            //滚动的时候也去监听红包，不过有点卡
////            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
////                if (className == "android.widget.ListView") {
////                    openRedPacket()
////                }
////            }
//        }
//    }
        }

    }


    //1.打开群聊
    private fun openGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val tabNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cdj")
            for (tabNode in tabNodes) {
                if (tabNode.text.toString() == "通讯录") {
                    tabNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    handler.postDelayed({
                        val newNodeInfo = rootInActiveWindow
                        if (newNodeInfo != null) {
                            val tagNodes = newNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/k3")
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

    //2.搜索群聊
    private fun searchGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aaq")
            for (info in nodes) {
                if (info.text.toString() == Hawk.get(Constant.GROUP_NAME)) {
                    info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    break
                }
            }
        }
    }

    //3.打开群聊设置
    private fun openGroupSetting() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hh")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    //4.滚动后点击添加按钮，打开添加成员页面
    private fun openSelectContact() {
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
                        val nodes = scrollNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d4_")
                        for (info in nodes) {
                            if (info.contentDescription.toString() == "添加成员") {
                                info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                break
                            }
                        }
                    }, 1000L)
                }
            }
        }
    }

    //5.添加成员
    private fun addMembers() {
        val members = Hawk.get(Constant.MEMBER_LIST, mutableListOf<String>())
        if (members.size > 0) {
            for (i in 0 until members.size) {
                handler.postDelayed({
                    val nodeInfo = rootInActiveWindow
                    if (nodeInfo != null) {
                        val editNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/au7")
                        if (editNodes != null && editNodes.size > 0) {
                            val editNode = editNodes[0]
                            val arguments = Bundle()
                            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, members[i])
                            editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        }
                    }
                }, 500L * (i + 1))
                handler.postDelayed({
                    val cbNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/lp")
                    if (cbNodes != null) {
                        val cbNode: AccessibilityNodeInfo?
                        if (cbNodes.size > 0) {
                            cbNode = cbNodes[0]
                            cbNode?.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                    //最后一次的时候清空记录，并且点击顶部确定按钮
                    if (i == members.size - 1) {
                        Hawk.put(Constant.MEMBER_LIST, mutableListOf<String>())
                        val sureNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hg")
                        if (sureNodes != null && sureNodes.size > 0) {
                            sureNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }

                    }
                }, 700L * (i + 1))
            }
        }
    }

    //对话框自动点击
    private fun dialogClick() {
        val inviteNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/an3")[0]
        inviteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        handler.postDelayed({
            val sureNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/an3")
            if (sureNodes != null && sureNodes.size > 0) {
                val sureNode = sureNodes[0]
                sureNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }, 1000L)
        performBackClick()
    }

    //自动点赞
    private fun autoZan() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            while (true) {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    val listNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dja")
                    if (listNodes != null && listNodes.size > 0) {
                        val listNode = listNodes[0]
                        val zanNodes = listNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dg4")
                        for (zan in zanNodes) {
                            zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(300)
                            val zsNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/df4")
                            Thread.sleep(300)
                            if (zsNodes != null && zsNodes.size > 0) {
                                if (zsNodes[0].findAccessibilityNodeInfosByText("赞").size > 0) {
                                    zsNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }
                            Thread.sleep(300)
                        }
                        listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    }
                } else {
                    break
                }
            }
        }
    }

    //遍历获得未打开红包
    private fun openRedPacket() {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val listNode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ab_")
            if (listNode != null && listNode.size > 0) {
                val msgNodes = listNode[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aem")
                if (msgNodes != null && msgNodes.size > 0) {
                    for (rpNode in msgNodes) {
                        val rpStatusNode = rpNode.findAccessibilityNodeInfosByText("领取红包")
                        if (rpStatusNode != null && rpStatusNode.size > 0) {
                            rpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            break
                        }
                    }
                }
            }
        }
    }

    //打开红包
    private fun clickRedPacket() {
        val nodeInfo = rootInActiveWindow
        val clickNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c85")
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
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 1300L)
    }

}