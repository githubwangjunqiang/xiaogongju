package com.app.aiyingli.xiaogongju.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import com.app.aiyingli.xiaogongju.App;
import com.app.aiyingli.xiaogongju.MainActivity;
import com.app.aiyingli.xiaogongju.utils.ScreenUtils;
import com.app.aiyingli.xiaogongju.utils.To;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * @author Android-小强 on 2018/11/8 15:48
 * @email: 15075818555@163.com
 * @ProjectName: iMoney
 */
public class AppAccessibility extends BaseAccessibilityService {
    /**
     * 需要监听的应用包名
     */
    public static volatile String sPackageName;
    /**
     * 当前的包名
     */
    public static volatile String current_packageName;
    /**
     * app 名称
     */
    public static volatile String viewKey;
    /**
     * 界面 名称
     */
    public static volatile String activity_name;
    /**
     * 详情界面
     */
    public static volatile String appinfoActivity;
    /**
     * 服务是否正在进行中
     */
    private static boolean rungIng = false;
    /**
     * 滑动列表
     */
    private AccessibilityNodeInfo listViewNodeInfo;

    public static boolean isRungIng() {
        return rungIng;
    }

    public static void setRungIng(boolean rungIng) {
        AppAccessibility.rungIng = rungIng;
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        setRungIng(true);
//        Log.d(TAG, "TYPE_VIEW_TEXT_CHANGED: " + AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
//        Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED: " + AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        Log.d(TAG, "onAccessibilityEvent: " + event.getAction() + "-" + event.getEventType());
        //试图滚动
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.d(TAG, "视图滚动了: ");
        }
        //试图内容变化
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            Log.d(TAG, "视图内容变化了: ");
        }
        //窗口变化
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            StringBuilder builder = new StringBuilder();
            current_packageName = String.valueOf(event.getPackageName());
            builder.append("\n前台activity：")
                    .append(event.getClassName() + "")
                    .append("\n前台包名->")
                    .append(current_packageName);
            Log.d(TAG, "onAccessibilityEvent: " + builder.toString());
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            if (rootInActiveWindow == null) {
                rootInActiveWindow = getRootInActiveWindow();
            }
            if (rootInActiveWindow == null) {
                To.toast("rootInActiveWindow=null");
                return;
            }
            String s = rootInActiveWindow.toString();

            Log.d(TAG, "rootInActiveWindow: " + s);
            String className = String.valueOf(event.getClassName());
            if (sPackageName.equals(current_packageName) && className.contains(activity_name)) {
                listViewNodeInfo = null;
                goTask();
                return;
            }
//            if (sPackageName.equals(current_packageName) && className.contains(appinfoActivity)) {
//                listViewNodeInfo = null;
//                goTask();
//                return;
//            }


        }

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            //点击事件
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                Log.i(TAG, "\ngetSource: " + source.toString());
            }


            String paname = event.getPackageName().toString();
            List<CharSequence> text = event.getText();
            StringBuilder msg = new StringBuilder();
            msg.append("\n点击事件")
                    .append("\n")
                    .append("包名：").append(paname)
                    .append("\n按钮的显示文字:")
                    .append(text);
            Log.i(TAG, "\n点击事件: " + msg.toString());
        }

    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "MyAccessibility中断了");
    }

    @Override
    protected void onServiceConnected() {
        sPackageName = "";
        super.onServiceConnected();
        Log.d(TAG, "已经连接 辅助功能服务");
        setRungIng(true);
    }

    /**
     * 开始检测
     *
     * @return
     */
    private void goTask() {
        Log.d(TAG, "goTask: 开始任务");
        Disposable subscribe = Observable.timer(2, TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        lookingForKeywords();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        To.toast("" + throwable.getLocalizedMessage());
                    }
                });

    }

    /**
     * 寻找关键字
     */
    private void lookingForKeywords() throws Exception {
        AccessibilityNodeInfo viewByText = findViewByText(viewKey);
        Log.d(TAG, "findViewByText: " + viewByText);
        if (viewByText == null) {
            //没有找到 尝试 滑动列表
            Callback callback = new Callback() {
                @Override
                public void onSuccess() {
                    goTask();
                }

                @Override
                public void onError() {
                    To.toast("滑动失败");
                }
            };
//            slideTheList(callback);
            performScrollForward(callback);
        } else {

            Rect rect = new Rect();
            viewByText.getBoundsInScreen(rect);
            int screenHeight = ScreenUtils.getScreenHeight(App.sContext);
            if (rect.bottom > screenHeight - screenHeight * 0.1F) {
                performScrollForward(null);
            }

            //先测试此位置在哪里
            //寻找 同级别的 下载按钮
//            loadDownloadBtn(viewByText);
            loadDownloadBtn2(viewByText);

//            boolean performViewClick = performViewClick(viewByText);
//            if (performViewClick) {
//                To.toast("跳转详情界面 成功");
//            }
        }
    }

    private void loadDownloadBtn2(AccessibilityNodeInfo viewByText) {

        AccessibilityNodeInfo clickInfo = null;

        Rect viewRect = new Rect();
        viewByText.getBoundsInScreen(viewRect);
        String keyDown = "下载";
        List<AccessibilityNodeInfo> btn = findViewByTexts(keyDown);
        if (btn != null) {
            for (AccessibilityNodeInfo nodeInfo : btn) {
                if (nodeInfo != null) {
                    String trim = nodeInfo.getText().toString().trim();
                    if (!trim.equals(keyDown)) {
                        continue;
                    }
                    Rect rectNode = new Rect();
                    nodeInfo.getBoundsInScreen(rectNode);
                    if ((viewRect.bottom + viewRect.height() * 2) > rectNode.top && (viewRect.top < rectNode.top)) {
                        clickInfo = nodeInfo;
                        break;
                    }
                }
            }
        }
        if (clickInfo == null) {
            To.toast("没有找到‘下载’按钮\n开始寻找‘继续’按钮");
            keyDown = "继续";
            List<AccessibilityNodeInfo> proceedBtn = findViewByTexts(keyDown);
            if (proceedBtn != null) {
                for (AccessibilityNodeInfo nodeInfo : proceedBtn) {
                    if (nodeInfo != null) {
                        String trim = nodeInfo.getText().toString().trim();
                        if (!trim.equals(keyDown)) {
                            continue;
                        }
                        Rect rectNode = new Rect();
                        nodeInfo.getBoundsInScreen(rectNode);
                        if ((viewRect.bottom + viewRect.height() * 2) > rectNode.top && (viewRect.top < rectNode.top)) {
                            clickInfo = nodeInfo;
                            break;
                        }
                    }
                }
            }
        }
        if (clickInfo == null) {
            To.toast("没有找到‘下载’‘继续’按钮");
            return;
        }
        boolean b = performViewClick(clickInfo);
        To.toast("点击按钮->" + b);
        Disposable subscribe = Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    MainActivity.kaishi = true;
                }, throwable -> {
                });


    }

    /**
     * 寻找 同级别的 下载按钮
     *
     * @param viewByText
     */
    private void loadDownloadBtn(AccessibilityNodeInfo viewByText) throws Exception {
        if (viewByText == null) {
            To.toast("寻找下载按钮失败");
            return;
        }
        AccessibilityNodeInfo parent = viewByText.getParent();
        if (parent == null) {
            To.toast("寻找下载按钮失败");
            return;
        }
        AccessibilityNodeInfo successInfo = null;
        for (int i = 0; i < parent.getChildCount(); i++) {
            AccessibilityNodeInfo child = parent.getChild(i);
            if (child.getChildCount() > 0) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    AccessibilityNodeInfo childj = child.getChild(j);
                    if (bianli(childj)) {
                        successInfo = childj;
                        break;
                    }
                }
            }
            if (successInfo != null) {
                break;
            }
            if (bianli(child)) {
                successInfo = child;
                break;
            }
        }
        if (successInfo == null) {
            loadDownloadBtn(parent);
        } else {
            boolean b = performViewClick(successInfo);
            To.toast("点击下载按钮->" + b);
            Disposable subscribe = Observable.timer(500, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        MainActivity.kaishi = true;
                    }, throwable -> {
                    });
        }
    }


    public boolean bianli(AccessibilityNodeInfo info) {
        CharSequence text = info.getText();
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        String string = text.toString().trim();
        if ("下载".equals(string) || "继续".equals(string)) {
            return true;
        }
        return false;
    }

    /**
     * 滑动列表  遍历整个view树 找到 列表控件 然后滑动
     */
    private void slideTheList(Callback callback) {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        int childCount = rootInActiveWindow.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (listViewNodeInfo != null) {
                break;
            }
            AccessibilityNodeInfo child = rootInActiveWindow.getChild(i);
            String className = String.valueOf(child.getClassName());
            Log.d(TAG, "accept: " + className);
            boolean listView = className.contains("ListView");
            boolean recyclerView = className.contains("RecyclerView");
            if (listView || recyclerView) {
                listViewNodeInfo = child;
                break;
            }
            for (int j = 0; j < child.getChildCount(); j++) {
                AccessibilityNodeInfo childj = child.getChild(j);
                String classNamej = String.valueOf(childj.getClassName());
                boolean listViewj = classNamej.contains("ListView");
                boolean recyclerViewj = classNamej.contains("RecyclerView");
                if (listViewj || recyclerViewj) {
                    listViewNodeInfo = childj;
                    break;
                }
            }
        }
        if (listViewNodeInfo != null) {
            listViewNodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }
        if (callback != null) {
            callback.onError();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sPackageName = "";
        Log.d(TAG, "辅助功能关闭了");
        setRungIng(false);
        return super.onUnbind(intent);
    }


}
