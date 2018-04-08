package com.coderpig.wechathelper;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 描述：
 *
 * @author CoderPig on 2018/04/04 13:46.
 */

public class HelperService extends AccessibilityService {

    private static final String TAG = "HelperService";
    private Handler handler = new Handler();
    private String userName = "呵呵呵";

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
                Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED");
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
                        inviteGroup();
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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo != null) {
                        recycle(nodeInfo);
                    }
                }
            }, 500L);
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
                if(info.getText().toString().equals("小猪的Python学习交流群")) {
                    Log.d(TAG, "inviteGroup: Test" + info.getParent().getParent().getClassName().toString());
                    info.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
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
            Log.i(TAG, "resId:" + info.getViewIdResourceName());
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
