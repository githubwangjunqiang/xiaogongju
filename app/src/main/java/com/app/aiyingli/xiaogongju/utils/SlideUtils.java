package com.app.aiyingli.xiaogongju.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.Callback;

/**
 * @author Android-小强 on 2019/6/10 14:00
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: SlideUtils
 */
public class SlideUtils implements ISlide {
    /**
     * 滑动的 view
     */
    private static final String LIST_VIEW = "ListView";
    /**
     * 滑动的 view
     */
    private static final String RECYCLER_VIEW = "RecyclerView";
    /**
     * 屏幕高度
     */
    private int screenHeight;
    /**
     * 屏幕宽度
     */
    private int screenWidth;
    /**
     * 滑动的 起始 占比 十分比
     */
    private int startVery;
    /**
     * 滑动的 停止 占比 十分比
     */
    private int stopVery;
    /**
     * 滑动的 开始时间 以及滑动时长  毫秒值
     */
    private int startTime, duration;
    /**
     * 全局上下文
     */
    private Context mContext;

    /**
     * 构造器
     *
     * @param context
     */
    public SlideUtils(Context context) {
        mContext = context;
        screenHeight = ScreenUtils.getScreenHeight(mContext);
        screenWidth = ScreenUtils.getScreenWidth(mContext);

        startVery = 3;
        stopVery = 8;

        startTime = 50;
        duration = 50;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void slideContentOn(AccessibilityService accessibilityService, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("slideContentDown(accessibilityService,callback):callback->null;参数不能为空");
        }
        if (accessibilityService == null) {
            callback.onError("参数错误");
            return;
        }
        Path path = new Path();
        int start = (screenHeight / 10) * startVery;
        int stop = (screenHeight / 10) * stopVery;

        //;//如果只是设置moveTo就是点击
        path.moveTo(screenWidth / 2, stop);
        //如果设置这句就是滑动
        path.lineTo(screenWidth / 2, start);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.
                        StrokeDescription(path,
                        startTime,
                        duration))
                .build();
        accessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                LogUtils.d("onCompleted: " + "滑动结束" + gestureDescription.getStrokeCount());
                if (callback != null) {
                    callback.onSuccess("滑动结束");
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtils.d("滑动取消");
                if (callback != null) {
                    callback.onError("滑动取消");
                }
            }
        }, null);

    }

    @Override
    public void slideContentOn(AccessibilityNodeInfo info, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("slideContentDown(accessibilityService,callback):callback->null;参数不能为空");
        }
        if (info == null) {
            callback.onError("参数错误");
            return;
        }
        CharSequence className = info.getClassName();
        if (className == null) {
            callback.onError("控件不是滑动view");
            return;
        }
        boolean listView = String.valueOf(className).contains(LIST_VIEW);
        boolean RecyclerView = String.valueOf(className).contains(LIST_VIEW);
        if (listView || RecyclerView) {
            Bundle arg = new Bundle();
//            arg.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT,0);
//            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,arg);
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void slideContentDown(AccessibilityService accessibilityService, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("slideContentDown(accessibilityService,callback):callback->null;参数不能为空");
        }
        if (accessibilityService == null) {
            callback.onError("参数错误");
            return;
        }
        Path path = new Path();
        int start = (screenHeight / 10) * startVery;
        int stop = (screenHeight / 10) * stopVery;

        //;//如果只是设置moveTo就是点击
        path.moveTo(screenWidth / 2, start);
        //如果设置这句就是滑动
        path.lineTo(screenWidth / 2, stop);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.
                        StrokeDescription(path,
                        startTime,
                        duration))
                .build();
        accessibilityService.dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                LogUtils.d("onCompleted: " + "滑动结束" + gestureDescription.getStrokeCount());
                if (callback != null) {
                    callback.onSuccess("滑动结束");
                }
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtils.d("滑动取消");
                if (callback != null) {
                    callback.onError("滑动取消");
                }
            }
        }, null);

    }

    @Override
    public void slideContentDown(AccessibilityNodeInfo info, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("slideContentDown(accessibilityService,callback):callback->null;参数不能为空");
        }
        if (info == null) {
            callback.onError("参数错误");
            return;
        }
        CharSequence className = info.getClassName();
        if (className == null) {
            callback.onError("控件不是滑动view");
            return;
        }
        boolean listView = String.valueOf(className).contains(LIST_VIEW);
        boolean RecyclerView = String.valueOf(className).contains(LIST_VIEW);
        if (listView || RecyclerView) {
            Bundle arg = new Bundle();
//            arg.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT,0);
//            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,arg);
            info.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            callback.onSuccess("");
        } else {
            callback.onError("不是可以滑动的view");
        }
    }

    @Override
    public AccessibilityNodeInfo getSlideInfo(AccessibilityService service) {

        AccessibilityNodeInfo rootInActiveWindow = service.getRootInActiveWindow();
        int childCount = rootInActiveWindow.getChildCount();
        AccessibilityNodeInfo traversing = traversing(rootInActiveWindow, childCount);

        return traversing;
    }

    /**
     * 遍历
     *
     * @param rootInActiveWindow
     * @param childCount
     */
    private AccessibilityNodeInfo traversing(AccessibilityNodeInfo rootInActiveWindow, int childCount) {
        AccessibilityNodeInfo nodeInfo = null;
        if (childCount < 1) {
            return nodeInfo;
        }
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = rootInActiveWindow.getChild(i);
            if (child != null && child.getClassName() != null) {
                String className = child.getClassName().toString();
                boolean listView = className.contains(LIST_VIEW);
                boolean recyclerView = className.contains(RECYCLER_VIEW);
                if (listView || recyclerView) {
                    nodeInfo = child;
                    break;
                }
                int childCountZ = child.getChildCount();
                if (childCountZ > 0) {
                    nodeInfo = traversing(child, childCountZ);
                }
            }
        }
        return nodeInfo;
    }
}
