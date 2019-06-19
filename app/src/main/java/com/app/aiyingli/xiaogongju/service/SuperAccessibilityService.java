package com.app.aiyingli.xiaogongju.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.app.aiyingli.xiaogongju.App;
import com.app.aiyingli.xiaogongju.Callback;
import com.app.aiyingli.xiaogongju.IAccessibilityService;
import com.app.aiyingli.xiaogongju.entity.WorkBean;
import com.app.aiyingli.xiaogongju.media.MediaManager;
import com.app.aiyingli.xiaogongju.utils.AppUtils;
import com.app.aiyingli.xiaogongju.utils.FindViewUtils;
import com.app.aiyingli.xiaogongju.utils.ISlide;
import com.app.aiyingli.xiaogongju.utils.LogUtils;
import com.app.aiyingli.xiaogongju.utils.ScreenUtils;
import com.app.aiyingli.xiaogongju.utils.SlideUtils;
import com.app.aiyingli.xiaogongju.utils.To;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public abstract class SuperAccessibilityService extends AccessibilityService implements IAccessibilityService {


    /**
     * 持有 辅助功能的软引用
     */
    protected Reference<AccessibilityService> mServiceReference;
    /**
     * 滑动工具类
     */
    protected ISlide mSlide;
    /**
     * 当前 执行的 任务
     */
    private WorkBean mWorkBean;

    protected SuperAccessibilityService(Context context) {
        mSlide = new SlideUtils(context);
    }


    @Override
    public void onInterrupt() {

    }

    @Override
    public void setAccessService(AccessibilityService accessService) {
        if (mServiceReference != null) {
            mServiceReference.clear();
        }
        mServiceReference = new SoftReference<>(accessService);
    }

    @Override
    public boolean detectionWindow(WorkBean workBean, AccessibilityEvent event) {
        if (event == null || workBean == null) {
            return false;
        }
        CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return false;
        }

        if (workBean.isSuccess()) {
            return false;
        }
        if (workBean.isStop()) {
            return false;
        }

        if (!workBean.getMaskPackName().equals(packageName)) {
            return false;
        }
        if (AppUtils.isInstalled(App.sContext, getWorkBean().getAppPackName())) {
            return false;
        }

        String activityName = workBean.getActivityName();
        if (TextUtils.isEmpty(activityName)) {
            return true;
        }
        if (event.getClassName().toString().contains(workBean.getActivityName())) {
            return true;
        }

        return false;
    }

    @Override
    public boolean detectionContentChange(WorkBean workBean, AccessibilityEvent event) {
        if (workBean == null) {
            return false;
        }
        if (workBean.isStart()) {
            return false;
        }
        if (workBean.isSuccess()) {
            return false;
        }
        if (!TextUtils.equals(event.getPackageName(), workBean.getMaskPackName())) {
            return false;
        }
        if (AppUtils.isInstalled(App.sContext, getWorkBean().getAppPackName())) {
            return false;
        }
        CharSequence className = event.getClassName();
        if (className != null) {
            boolean listView = className.toString().contains(SlideUtils.LIST_VIEW);
            boolean recyclerView = className.toString().contains(SlideUtils.RECYCLER_VIEW);
            return listView || recyclerView;
        }
        return false;
    }

    @Override
    public boolean detectionSystemPermission(WorkBean workBean, AccessibilityEvent event) {
        if (workBean == null || event == null) {
            return false;
        }
        if (workBean.isStart()) {
            return false;
        }
        if (event.getPackageName().equals(WorkBean.getPermission())) {
            return true;
        }

        return false;
    }

    @Override
    public void clickPermission(WorkBean workBean, AccessibilityService service) {
        if (workBean == null || service == null) {
            return;
        }

        AccessibilityNodeInfo viewByText = FindViewUtils.findViewByText(WorkBean.getPermissionBtnText(), service);
        if (viewByText == null) {
            return;
        }
        boolean b = FindViewUtils.performViewClick(viewByText);
        LogUtils.d("点击了 " + WorkBean.getPermissionBtnText() + "--》 " + b);

    }

    @Override
    public void startWork(WorkBean workBean, AccessibilityEvent event) {

        if (mServiceReference.get() == null) {
            return;
        }

        List<AccessibilityNodeInfo> keyWords = FindViewUtils.findViewByTexts(workBean.getAppName(), mServiceReference.get());
        keyWords = FindViewUtils.findViewByTexts(workBean.getAppName(), mServiceReference.get());

        AccessibilityNodeInfo data = null;
        if (keyWords != null && !keyWords.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : keyWords) {
                CharSequence text = nodeInfo.getText();
                if (TextUtils.isEmpty(text)) {
                    continue;
                }
                if (TextUtils.equals(text, workBean.getAppName())) {
                    data = nodeInfo;
                    break;
                }
            }
        }
        if (data == null) {
            slideContentOn(workBean, event);
        } else {
            Rect rect = new Rect();
            data.getBoundsInScreen(rect);
            int screenHeight = ScreenUtils.getScreenHeight(App.sContext);
            if (rect.bottom > screenHeight - screenHeight * 0.1F) {
                slideContentOn(workBean, event);
            } else {
                findButton(data);
            }
        }

    }

    @Override
    public AccessibilityNodeInfo getKeyWordnodeInfo(WorkBean workBean, AccessibilityService service) {
        if (service == null) {
            return null;
        }
        if (workBean == null) {
            return null;
        }

        List<AccessibilityNodeInfo> keyWords = FindViewUtils.findViewByTexts(workBean.getAppName(), service);
        keyWords = FindViewUtils.findViewByTexts(workBean.getAppName(), service);

        AccessibilityNodeInfo data = null;
        if (keyWords != null && !keyWords.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : keyWords) {
                CharSequence text = nodeInfo.getText();
                if (TextUtils.isEmpty(text)) {
                    continue;
                }
                if (TextUtils.equals(text, workBean.getAppName())) {
                    data = nodeInfo;
                    break;
                }
            }
        }
        return data;
    }

    @Override
    public void slideContentOn(WorkBean workBean, AccessibilityEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mServiceReference.get() == null) {
                return;
            }
            mSlide.slideContentOn(mServiceReference.get(), new Callback() {
                @Override
                public void onSuccess(String msg) {
                    startWork(workBean, event);
                }

                @Override
                public void onError(String msg) {
                    To.toast(msg);
                    stopWork();
                }
            });
        } else {
            if (mServiceReference.get() == null) {
                return;
            }
            mSlide.slideContentOn(mSlide.getSlideInfo(mServiceReference.get()), new Callback() {
                @Override
                public void onSuccess(String msg) {
                    startWork(workBean, event);
                }

                @Override
                public void onError(String msg) {
                    To.toast(msg);
                    stopWork();
                }
            });
        }
    }

    @Override
    public void findButton(AccessibilityNodeInfo accessibilityNodeInfo) {
        AccessibilityNodeInfo clickInfo = null;

        Rect viewRect = new Rect();
        accessibilityNodeInfo.getBoundsInScreen(viewRect);
        if (mServiceReference.get() == null) {
            return;
        }
        List<AccessibilityNodeInfo> btn = FindViewUtils.findViewByTexts(getDownBtnText(), mServiceReference.get());
        btn = FindViewUtils.findViewByTexts(getDownBtnText(), mServiceReference.get());
        if (btn != null) {
            for (AccessibilityNodeInfo nodeInfo : btn) {
                if (nodeInfo != null) {
                    if (nodeInfo.getText() == null) {
                        continue;
                    }
                    String trim = nodeInfo.getText().toString().trim();
                    if (!trim.equals(getDownBtnText())) {
                        continue;
                    }
                    Rect rectNode = new Rect();
                    nodeInfo.getBoundsInScreen(rectNode);
                    if (compareRect(viewRect, rectNode)) {
                        clickInfo = nodeInfo;
                        break;
                    }
                }
            }
        }
        if (clickInfo == null) {
            To.toast("没有找到‘下载’按钮\n开始寻找‘继续’按钮");
            if (mServiceReference.get() == null) {
                return;
            }
            List<AccessibilityNodeInfo> proceedBtn = FindViewUtils.findViewByTexts(getContiueBtnText(), mServiceReference.get());
            proceedBtn = FindViewUtils.findViewByTexts(getContiueBtnText(), mServiceReference.get());
            if (proceedBtn != null) {
                for (AccessibilityNodeInfo nodeInfo : proceedBtn) {
                    if (nodeInfo != null) {
                        CharSequence charSequence = nodeInfo.getText() == null ? nodeInfo.getContentDescription() : nodeInfo.getText();
                        if (charSequence == null) {
                            continue;
                        }
                        String trim = charSequence.toString().trim();

                        if (!trim.equals(getContiueBtnText())) {
                            continue;
                        }
                        Rect rectNode = new Rect();
                        nodeInfo.getBoundsInScreen(rectNode);
                        if (compareRect(viewRect, rectNode)) {
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
        boolean b = FindViewUtils.performViewClick(clickInfo);
        successWork();
        Disposable subscribe = Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    File file = new File(getWorkBean().getFileDoc(), getDownBtnText() + ".jpeg");
                    MediaManager.getManager().startGetBitmap(file.getAbsolutePath());
                }, throwable -> {
                });
    }

    @Override
    public boolean compareRect(Rect rectAppName, Rect rectButton) {
        if (rectAppName == null || rectButton == null) {
            return false;
        }
        if ((rectAppName.bottom + rectAppName.height() * 2) > rectButton.top
                && ((rectAppName.top - rectAppName.height()) < rectButton.top)) {
            return true;
        }
        return false;
    }

    @Override
    public void stopWork() {
        if (getWorkBean() == null) {
            return;
        }
        getWorkBean().setStop(true);
    }

    @Override
    public void successWork() {
        if (getWorkBean() == null) {
            return;
        }
        getWorkBean().setStop(true);
        getWorkBean().setSuccess(true);
    }

    @Override
    public boolean isTaskInstallActivity(WorkBean workBean, AccessibilityEvent event) {
        if (workBean == null) {
            return false;
        }
        if (event == null) {
            return false;
        }
        if (workBean.isOpenApp()) {
            return false;
        }

        if (workBean.isSuccess() &&
                event.getPackageName().equals(WorkBean.getInstallName()) &&
                !AppUtils.isInstalled(App.sContext, workBean.getAppPackName())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSystemOpenActivity(WorkBean workBean, AccessibilityEvent event) {
        if (workBean == null) {
            return false;
        }
        if (event == null) {
            return false;
        }
        if (!workBean.isSuccess()) {
            return false;
        }
        if (!AppUtils.isInstalled(App.sContext, workBean.getAppPackName())) {
            return false;
        }
        if (!workBean.isStartInstall()) {
            return false;
        }
        if (workBean.isOpenApp()) {
            return false;
        }
        if (event.getPackageName().equals(getWorkBean().getMaskPackName())) {
            return true;
        }

        return false;
    }

    @Override
    public void autoInstallation(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> accessibilityNodeInfosByText = nodeInfo.findAccessibilityNodeInfosByText(WorkBean.getPackageInstallerButton());

        if (accessibilityNodeInfosByText == null || accessibilityNodeInfosByText.isEmpty()) {
            To.toast("安装界面，没有获取到安装按钮");
            return;
        }
        AccessibilityNodeInfo infoInstall = null;
        for (AccessibilityNodeInfo data : accessibilityNodeInfosByText) {
            if (data.getText() != null) {
                String trim = data.getText().toString().trim();
                if (TextUtils.equals(trim, WorkBean.getPackageInstallerButton())) {
                    infoInstall = data;
                    break;
                }

            }
        }
        if (infoInstall == null) {
            To.toast("没有找到安装按钮");
        } else {
            boolean performViewClick = FindViewUtils.performViewClick(infoInstall);
            if (performViewClick) {
                getWorkBean().setStartInstall(true);
            }
        }

    }

    @Override
    public void openApp(AccessibilityService service) {
        if (getWorkBean() == null) {
            return;
        }
        if (service == null) {
            return;
        }
        AccessibilityNodeInfo rootInActiveWindow = service.getRootInActiveWindow();
        List<AccessibilityNodeInfo> accessibilityNodeInfosByText = rootInActiveWindow.findAccessibilityNodeInfosByText(WorkBean.OPEN);
        if (accessibilityNodeInfosByText == null || accessibilityNodeInfosByText.isEmpty()) {
            return;
        }
        AccessibilityNodeInfo clickInfo = null;
        for (AccessibilityNodeInfo data : accessibilityNodeInfosByText) {
            if (data.getText() != null) {
                String trim = data.getText().toString().trim();
                if (trim.equals(WorkBean.OPEN)) {
                    clickInfo = data;
                    break;
                }
            }
        }
        if (clickInfo != null) {
            FindViewUtils.performViewClick(clickInfo);
        } else {
            To.toast("没有找到打开按钮");
        }
    }

    public WorkBean getWorkBean() {
        return mWorkBean;
    }

    public void setWorkBean(WorkBean workBean) {
        mWorkBean = workBean;
    }

    /**
     * 获取 继续按钮的 文案 是啥
     *
     * @return
     */
    protected abstract String getContiueBtnText();

    /**
     * 获取下载 按钮的 文案 是 什么 下载 还是 安装 还是啥
     *
     * @return
     */
    protected abstract String getDownBtnText();

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
