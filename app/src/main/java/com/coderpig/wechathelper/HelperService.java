package com.coderpig.wechathelper;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 描述：微信监控服务类
 *
 * @author CoderPig on 2018/04/04 13:46.
 */

public class HelperService extends AccessibilityService {

    private static final String TAG = "HelperService";
    private Handler handler = new Handler();
    private String userName = "123";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        CharSequence classNameChr = event.getClassName();
        String className = classNameChr.toString();
        Log.d(TAG, event.toString());
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                    Notification notification = (Notification) event.getParcelableData();
                    String content = notification.tickerText.toString();
                    if (content.contains("请求添加你为朋友")) {
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                switch (className) {
                    case "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI":
                        addFriend();
                        break;
                    case "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI":
                        verifyFriend();
                        break;
                    case "com.tencent.mm.plugin.profile.ui.ContactInfoUI":
                        performBackClick();
                        break;
                    case "com.tencent.mm.ui.LauncherUI":
                        if (!userName.equals("123")) {
                            openGroup();
                        }
                        break;
                    case "com.tencent.mm.ui.contact.ChatroomContactUI":
                        if (!userName.equals("123")) {
                            inviteGroup();
                        }
                        break;
                    case "com.tencent.mm.ui.chatting.ChattingUI":
                        if (!userName.equals("123")) {
                            openGroupSetting();
                        }
                        break;
                    case "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI":
                        if (userName.equals("123")) {
                            performBackClick();
                        } else {
                            addToGroup();
                        }
                        break;
                    case "com.tencent.mm.ui.base.i":
                        dialogClick();
                        break;
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

        }
    }

    private void addFriend() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("接受");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            } else {
                performBackClick();
            }
        }
    }

    private void verifyFriend() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        //获得用户名
        if (nodeInfo != null) {
            userName = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d0n").get(0).getText().toString();
            AccessibilityNodeInfo finishNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hd").get(0);
            finishNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void openGroup() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ca5");
            for (AccessibilityNodeInfo info : nodes) {
                if (info.getText().toString().equals("通讯录")) {
                    info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                            if (nodeInfo != null) {
                                List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j5");
                                for (AccessibilityNodeInfo info : nodes) {
                                    if (info.getText().toString().equals("群聊")) {
                                        info.getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        break;
                                    }
                                }
                            }
                        }
                    }, 500L);
                }
            }
        }
    }

    private void inviteGroup() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a9v");
            for (AccessibilityNodeInfo info : nodes) {
                if (info.getText().toString().equals("小猪的Python学习交流群")) {
                    info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }

    private void openGroupSetting() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/he").get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void addToGroup() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> listNodes = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");
            if(listNodes != null && listNodes.size() > 0) {
                AccessibilityNodeInfo listNode = listNodes.get(0);
                listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                final AccessibilityNodeInfo scrollNodeInfo = getRootInActiveWindow();
                if (scrollNodeInfo != null) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<AccessibilityNodeInfo> nodes = scrollNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d0b");
                            for (AccessibilityNodeInfo info : nodes) {
                                if (info.getContentDescription().toString().equals("添加成员")) {
                                    info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                }
                            }
                        }
                    },1000L);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<AccessibilityNodeInfo> editNodes = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/arz");
                            if(editNodes != null && editNodes.size() > 0) {
                                AccessibilityNodeInfo editNode = editNodes.get(0);
                                Bundle arguments = new Bundle();
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, userName);
                                editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            }
                        }
                    }, 2300L);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<AccessibilityNodeInfo> cbNodes = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kr");
                            if(cbNodes != null) {
                                AccessibilityNodeInfo cbNode = null;
                                if(cbNodes.size() == 1) {
                                    cbNode = cbNodes.get(0);
                                } else if(cbNodes.size() == 2) {
                                    cbNode = cbNodes.get(1);
                                }
                                if (cbNode != null) {
                                    cbNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    AccessibilityNodeInfo sureNode = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hd").get(0);
                                    sureNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                        }
                    }, 3000L);
                }
            }

        }

    }

    private void dialogClick() {
        AccessibilityNodeInfo inviteNode = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aln").get(0);
        inviteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        userName = "123";
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<AccessibilityNodeInfo> sureNodes = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aln");
                if(sureNodes != null && sureNodes.size() > 0) {
                    AccessibilityNodeInfo sureNode = sureNodes.get(0);
                    sureNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

            }
        },1000L);
    }

    private void performBackClick() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }, 300L);
    }

    //遍历控件的方法
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            Log.i(TAG, "child widget----------------------------" + info.getClassName().toString());
            Log.i(TAG, "showDialog:" + info.canOpenPopup());
            Log.i(TAG, "Text：" + info.getText());
            Log.i(TAG, "windowId:" + info.getWindowId());
            Log.i(TAG, "desc:" + info.getContentDescription());
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }


}
