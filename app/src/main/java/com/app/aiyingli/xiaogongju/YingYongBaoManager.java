package com.app.aiyingli.xiaogongju;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.entity.WorkBean;
import com.app.aiyingli.xiaogongju.utils.AppUtils;
import com.app.aiyingli.xiaogongju.utils.FindViewUtils;
import com.app.aiyingli.xiaogongju.utils.ISlide;
import com.app.aiyingli.xiaogongju.utils.LogUtils;
import com.app.aiyingli.xiaogongju.utils.ScreenUtils;
import com.app.aiyingli.xiaogongju.utils.SlideUtils;
import com.app.aiyingli.xiaogongju.utils.To;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author Android-小强 on 2019/6/10 13:49
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: YingYongBaoManager
 */
public class YingYongBaoManager extends AccessibilityService implements IAccessibilityService {
    /**
     * 应用宝 包名
     */
    public static final String YING_YONG_BAO = "com.tencent.android.qqdownloader";
    /**
     * 安装 按钮
     */
    public static final String BUTTON_INSTALL = "安装";
    /**
     * 列表界面 的初始化按钮 下载 关键字
     */
    public static final String DOWN_KEYWOD = "下载";
    /**
     * 列表界面 的下载按钮点击过之后  是显示 继续 关键字
     */
    public static final String CONTINUE_KEYWOD = "继续";
    /**
     * 持有 辅助功能的软引用
     */
    private Reference<AccessibilityService> mServiceReference;
    /**
     * 当前 执行的 任务
     */
    private WorkBean mWorkBean;
    /**
     * 滑动工具类
     */
    private ISlide mSlide;

    /**
     * 私有构造器
     */
    private YingYongBaoManager() {
        mSlide = new SlideUtils(App.sContext);
    }

    /**
     * 单例 方法
     *
     * @return
     */
    public static final AccessibilityService getSingleCase() {
        return Holder.mAccessibilityService;
    }

    public WorkBean getWorkBean() {
        return mWorkBean;
    }

    public void setWorkBean(WorkBean workBean) {
        mWorkBean = workBean;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        LogUtils.d("event->" + event.toString());
        if (mWorkBean == null) {
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //窗口变化 执行任务
            if (detectionWindow(mWorkBean, event)) {
                mWorkBean.setStart(true);
                startWork(mWorkBean, event);
            }
            //窗口变化 点击系统安装界面 安装按钮
            if (isTaskInstallActivity(mWorkBean, event)) {
                if (mServiceReference.get() != null) {
                    autoInstallation(mServiceReference.get().getRootInActiveWindow());
                }
            }
            // 已经打开 指定 app 那么 设置 打开状态 为true
            if (mWorkBean != null && event.getPackageName().equals(mWorkBean.getAppPackName())) {
                mWorkBean.setOpenApp(true);
            }

        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            //内容变化  判断是否执行任务
            if (detectionContentChange(mWorkBean, event)) {
                mWorkBean.setStart(true);
                startWork(mWorkBean, event);
            }

            //内容变化 判断是否是安装界面
            if (isTaskInstallActivity(mWorkBean, event)) {
                if (mServiceReference.get() != null) {
                    autoInstallation(mServiceReference.get().getRootInActiveWindow());
                }
            }
            //安装完成 的界面 点击打开按钮
            if (isSystemOpenActivity(mWorkBean, event)) {
                openApp(mServiceReference.get());
            }

        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            //有内容滚动  判断是否执行任务
            if (detectionContentChange(mWorkBean, event)) {
                mWorkBean.setStart(true);
                startWork(mWorkBean, event);
            }
        }
//        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
//            //是否是安装界面 点击安装
//            LogUtils.d("已经收到 点击事件 【安装】" + event.getText() + "-" + mWorkBean.isSuccess() + "" +
//                    "-" + event.getPackageName().equals(WorkBean.getInstallName()) + "" +
//                    "-" + !AppUtils.isInstalled(App.sContext, mWorkBean.getAppPackName()));
//            if (mWorkBean != null &&
//                    BUTTON_INSTALL.equals(event.getText()) &&
//                    mWorkBean.isSuccess() &&
//                    event.getPackageName().equals(WorkBean.getInstallName()) &&
//                    !AppUtils.isInstalled(App.sContext, mWorkBean.getAppPackName())) {
//                LogUtils.d("已经收到 点击事件 【安装】");
//                mWorkBean.setStartInstall(true);
//            }
//        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
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
        if (AppUtils.isInstalled(App.sContext, mWorkBean.getAppPackName())) {
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
        if (AppUtils.isInstalled(App.sContext, mWorkBean.getAppPackName())) {
            return false;
        }
        CharSequence className = event.getClassName();
        if (className != null) {
            boolean contains = className.toString().contains(SlideUtils.LIST_VIEW);
            return contains;
        }


        return false;
    }

    @Override
    public void startWork(WorkBean workBean, AccessibilityEvent event) {

        if (mServiceReference.get() == null) {
            return;
        }

        List<AccessibilityNodeInfo> keyWords = FindViewUtils.findViewByTexts(workBean.getKeyWord(), mServiceReference.get());
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
        List<AccessibilityNodeInfo> btn = FindViewUtils.findViewByTexts(DOWN_KEYWOD, this);
        if (mServiceReference.get() == null) {
            return;
        }
        btn = FindViewUtils.findViewByTexts(DOWN_KEYWOD, mServiceReference.get());
        if (btn != null) {
            for (AccessibilityNodeInfo nodeInfo : btn) {
                if (nodeInfo != null) {
                    if (nodeInfo.getText() == null) {
                        continue;
                    }
                    String trim = nodeInfo.getText().toString().trim();
                    if (!trim.equals(DOWN_KEYWOD)) {
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
            if (mServiceReference.get() == null) {
                return;
            }
            List<AccessibilityNodeInfo> proceedBtn = FindViewUtils.findViewByTexts(CONTINUE_KEYWOD, mServiceReference.get());
            proceedBtn = FindViewUtils.findViewByTexts(CONTINUE_KEYWOD, mServiceReference.get());
            if (proceedBtn != null) {
                for (AccessibilityNodeInfo nodeInfo : proceedBtn) {
                    if (nodeInfo != null) {
                        CharSequence charSequence = nodeInfo.getText() == null ? nodeInfo.getContentDescription() : nodeInfo.getText();
                        if (charSequence == null) {
                            continue;
                        }
                        String trim = charSequence.toString().trim();

                        if (!trim.equals(CONTINUE_KEYWOD)) {
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
        boolean b = FindViewUtils.performViewClick(clickInfo);
//        To.toast("点击按钮->" + b);
        successWork();
        Disposable subscribe = Observable.timer(100, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    MainActivity.kaishi = true;
                }, throwable -> {
                });
    }

    @Override
    public void stopWork() {
        if (mWorkBean == null) {
            return;
        }
        mWorkBean.setStop(true);
    }

    @Override
    public void successWork() {
        if (mWorkBean == null) {
            return;
        }
        mWorkBean.setStop(true);
        mWorkBean.setSuccess(true);
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
//            To.toast("安装界面，符合标准");
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

        if (event.getPackageName().equals(WorkBean.getOpenAppcPackName())) {
            return true;
        }

        return false;
    }

    @Override
    public void autoInstallation(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> accessibilityNodeInfosByText = nodeInfo.findAccessibilityNodeInfosByText(BUTTON_INSTALL);

        if (accessibilityNodeInfosByText == null || accessibilityNodeInfosByText.isEmpty()) {
            To.toast("安装界面，没有获取到安装按钮");
            return;
        }
        AccessibilityNodeInfo infoInstall = null;
        for (AccessibilityNodeInfo data : accessibilityNodeInfosByText) {
            if (data.getText() != null) {
                String trim = data.getText().toString().trim();
                if (TextUtils.equals(trim, BUTTON_INSTALL)) {
                    infoInstall = data;
                    break;
                }

            }
        }
        if (infoInstall == null) {
            To.toast("没有找到安装按钮");
        } else {
            boolean performViewClick = FindViewUtils.performViewClick(infoInstall);
//            To.toast("点击安装按钮-》" + performViewClick);
            if (performViewClick) {
                mWorkBean.setStartInstall(true);
            }
        }

    }

    @Override
    public void openApp(AccessibilityService service) {
        if (mWorkBean == null) {
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

    /**
     * 内部类 单例模式
     */
    private static final class Holder {
        private static final AccessibilityService mAccessibilityService = new YingYongBaoManager();
    }
}
