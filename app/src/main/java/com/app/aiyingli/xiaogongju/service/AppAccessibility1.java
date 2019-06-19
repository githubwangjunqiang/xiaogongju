package com.app.aiyingli.xiaogongju.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.App;
import com.app.aiyingli.xiaogongju.Callback;
import com.app.aiyingli.xiaogongju.MainActivity;
import com.app.aiyingli.xiaogongju.entity.WorkBean;
import com.app.aiyingli.xiaogongju.utils.ScreenUtils;
import com.app.aiyingli.xiaogongju.utils.To;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * @author Android-小强 on 2018/11/8 15:48
 * @email: 15075818555@163.com
 * @ProjectName: iMoney
 */
public class AppAccessibility1 extends BaseAccessibilityService {


    private static Map<String, AccessibilityService> mServiceMap = new ArrayMap<>(1);
    private static WorkBean sWorkBean;
    private String current_packageName;
    private String sPackageName;
    private boolean start;
    private CharSequence activity_name;
    private AccessibilityNodeInfo listViewNodeInfo;
    private String viewKey;

    public static final void setWorkBean(WorkBean workBean) {
        sWorkBean = workBean;
        if (sWorkBean == null) {
            return;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (sWorkBean != null && sWorkBean.getAccessibilityService() != null) {
            sWorkBean.getAccessibilityService().onAccessibilityEvent(event);
        }

        Log.d(TAG, "onAccessibilityEvent: " + event.getAction() + "-" + event.getEventType() + "-" + event.getPackageName() + "-" + event.getClassName());
        //试图滚动
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.d(TAG, "视图滚动了: ");
            if (!start) {
                //如果 窗口变化监听 并没有识别是指定界面 并没有开始任务 那么 暂时使用这一种方式来开启任务 监听指定包名的 指定listview 的滚动事件
                String className = "android.widget.ListView";
                if (event.getPackageName().equals(sPackageName)
                        && event.getClassName().equals(className)) {
                    startTask();
                }
            }
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

            Log.d(TAG, "rootInActiveWindow: " + new Gson().toJson(rootInActiveWindow));
            String className = String.valueOf(event.getClassName());
            if (sPackageName.equals(current_packageName) && className.contains(activity_name)) {
                startTask();
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
        if (sWorkBean != null && sWorkBean.getAccessibilityService() != null) {
            sWorkBean.getAccessibilityService().onInterrupt();
        }
        Log.d(TAG, "MyAccessibility中断了");
        sWorkBean = null;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "已经连接 辅助功能服务");
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "辅助功能关闭了");
        if (sWorkBean != null && sWorkBean.getAccessibilityService() != null) {
            return sWorkBean.getAccessibilityService().onUnbind(intent);
        }
        sWorkBean = null;
        return super.onUnbind(intent);
    }

    /**
     * 开始任务
     */
    private void startTask() {
        listViewNodeInfo = null;
        goTask();
    }

    /**
     * 开始检测
     *
     * @return
     */
    private void goTask() {
        Log.d(TAG, "goTask: 开始任务");
        start = true;
        Disposable subscribe = Observable.timer(2, TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        lookingForKeywords();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        start = false;
                        To.toast("" + throwable.getLocalizedMessage());
                    }
                });

    }

    /**
     * 寻找关键字
     */
    private void lookingForKeywords() throws Exception {

        if (!getRootInActiveWindow().getPackageName().equals(sPackageName)) {
            start = false;
            To.toast("不是指定app包名");
            return;
        }

        List<AccessibilityNodeInfo> viewByTexts = findViewByTexts(viewKey);
        AccessibilityNodeInfo nodeInfo = null;
        if (viewByTexts != null) {
            Log.d(TAG, "findViewByText: " + new Gson().toJson(viewByTexts));
            for (AccessibilityNodeInfo data : viewByTexts) {
                if (data.getText() == null) {
                    continue;
                }
                if (data.getText().toString().trim().equals(viewKey)) {
                    nodeInfo = data;
                    break;
                }
            }
        }
        if (nodeInfo == null) {
            //没有找到 尝试 滑动列表
//            Callback callback = new Callback() {
//                @Override
//                public void onSuccess() {
//                    goTask();
//                }
//
//                @Override
//                public void onError() {
//                    start = false;
//                    Log.d(TAG, "onError: 滑动失败");
//                    To.toast("滑动失败");
//                }
//            };
//            slideTheList(callback);
//            performScrollForward(callback);
        } else {
            //先测试此位置在哪里
            final AccessibilityNodeInfo dataInfo = nodeInfo;
            Rect rect = new Rect();
            dataInfo.getBoundsInScreen(rect);
            int screenHeight = ScreenUtils.getScreenHeight(App.sContext);
            if (rect.bottom > screenHeight - screenHeight * 0.1F) {
//                performScrollForward(new Callback() {
//                    @Override
//                    public void onSuccess() {
//
//                        //寻找 同级别的 下载按钮
////            loadDownloadBtn(viewByText);
//                        Disposable subscribe = Observable.timer(1050, TimeUnit.MILLISECONDS)
//                                .subscribe(aLong -> {
//                                    loadDownloadBtn2(dataInfo);
//                                }, throwable -> {
//                                    throwable.printStackTrace();
//                                    To.toast(throwable.getLocalizedMessage());
//                                });
//                    }
//
//                    @Override
//                    public void onError() {
//                        start = false;
//                        To.toast("调整位置失败");
//                    }
//                });
            } else {

                //寻找 同级别的 下载按钮
//            loadDownloadBtn(viewByText);
                loadDownloadBtn2(dataInfo);
            }


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
                    if (nodeInfo.getText() == null) {
                        continue;
                    }
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
                        CharSequence charSequence = nodeInfo.getText() == null ? nodeInfo.getContentDescription() : nodeInfo.getText();
                        if (charSequence == null) {
                            continue;
                        }
                        String trim = charSequence.toString().trim();

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
        start = false;
        if (clickInfo == null) {
            To.toast("没有找到‘下载’‘继续’按钮");
            return;
        }
        boolean b = performViewClick(clickInfo);
        To.toast("点击按钮->" + b);
        sPackageName = "";
        Disposable subscribe = Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
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
                callback.onSuccess("");
            }
            return;
        }
        if (callback != null) {
            callback.onError("");
        }
    }


}
