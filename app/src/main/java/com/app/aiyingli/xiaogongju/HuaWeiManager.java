package com.app.aiyingli.xiaogongju;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.entity.WorkBean;
import com.app.aiyingli.xiaogongju.media.MediaManager;
import com.app.aiyingli.xiaogongju.service.SuperAccessibilityService;
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

/**
 * @author Android-小强 on 2019/6/10 13:49
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: YingYongBaoManager
 */
public class HuaWeiManager extends SuperAccessibilityService implements IAccessibilityService {
    /**
     * 应用宝 包名
     */
    public static final String PACK_NAME = "com.huawei.appmarket";
    /**
     * 列表界面 的初始化按钮 下载 关键字
     */
    public static final String DOWN_KEYWOD = "安装";
    /**
     * 列表界面 的下载按钮点击过之后  是显示 继续 关键字
     */
    public static final String CONTINUE_KEYWOD = "继续";

    protected HuaWeiManager(Context context) {
        super(context);
    }


    /**
     * 单例 方法
     *
     * @return
     */
    public static final SuperAccessibilityService getSingleCase() {
        return Holder.mAccessibilityService;
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        LogUtils.d("event->" + event.toString());
        if (getWorkBean() == null) {
            return;
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //窗口变化 执行任务
            if (detectionWindow(getWorkBean(), event)) {
                getWorkBean().setStart(true);
                startWork(getWorkBean(), event);
            }
            //窗口变化 点击系统安装界面 安装按钮
            if (isTaskInstallActivity(getWorkBean(), event)) {
                if (mServiceReference.get() != null) {
                    autoInstallation(mServiceReference.get().getRootInActiveWindow());
                }
            }
            // 已经打开 指定 app 那么 设置 打开状态 为true
            if (getWorkBean() != null && event.getPackageName().equals(getWorkBean().getAppPackName())) {
                if (!getWorkBean().isOpenApp()) {
                    String absolutePath = new File(getWorkBean().getFileDoc(), getWorkBean().getAppName() + ".jpeg").getAbsolutePath();
                    MediaManager.getManager().startGetBitmap(absolutePath);
                }
                getWorkBean().setOpenApp(true);
            }

        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            //内容变化  判断是否执行任务
            if (detectionContentChange(getWorkBean(), event)) {
                getWorkBean().setStart(true);
                startWork(getWorkBean(), event);
            }

            //内容变化 判断是否是安装界面
            if (isTaskInstallActivity(getWorkBean(), event)) {
                if (mServiceReference.get() != null) {
                    autoInstallation(mServiceReference.get().getRootInActiveWindow());
                }
            }
            //安装完成 的界面 点击打开按钮
            if (isSystemOpenActivity(getWorkBean(), event)) {
                openApp(mServiceReference.get());
            }
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            //有内容滚动  判断是否执行任务
            if (detectionContentChange(getWorkBean(), event)) {
                getWorkBean().setStart(true);
                startWork(getWorkBean(), event);
            }
        }

    }

    @Override
    public boolean detectionPermission(WorkBean workBean, AccessibilityEvent event) {
        return false;
    }

    @Override
    public void permitPermission(WorkBean workBean, AccessibilityService service) {

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
        //重写此方法 判断如果是 华为手机 会静默安装 所以 不存在点击安装按钮
        String brand = Build.BRAND;
        if (!WorkBean.Hua_wei.equalsIgnoreCase(brand)) {
            if (!workBean.isStartInstall()) {
                return false;
            }
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
    public void openApp(AccessibilityService service) {
        if (getWorkBean() == null) {
            return;
        }
        if (service == null) {
            return;
        }

        AccessibilityNodeInfo keyWordnodeInfo = getKeyWordnodeInfo(getWorkBean(), service);
        if (keyWordnodeInfo == null) {
            To.toast("没有找到app名称");
            return;
        }
        List<AccessibilityNodeInfo> accessibilityNodeInfosByText = FindViewUtils.findViewByTexts(WorkBean.OPEN, service);
        if (accessibilityNodeInfosByText == null || accessibilityNodeInfosByText.isEmpty()) {
            return;
        }
        AccessibilityNodeInfo clickInfo = null;
        Rect rectAppName = new Rect();
        keyWordnodeInfo.getBoundsInScreen(rectAppName);
        for (AccessibilityNodeInfo data : accessibilityNodeInfosByText) {
            if (data.getText() != null) {
                String trim = data.getText().toString().trim();
                if (trim.equals(WorkBean.OPEN)) {
                    Rect rect = new Rect();
                    data.getBoundsInScreen(rect);
                    if (compareRect(rectAppName, rect)) {
                        clickInfo = data;
                        break;
                    }
                }
            }
        }
        if (clickInfo != null) {
            FindViewUtils.performViewClick(clickInfo);
        } else {
            To.toast("没有找到打开按钮");
        }
    }

    @Override
    protected String getContiueBtnText() {
        return DOWN_KEYWOD;
    }

    @Override
    protected String getDownBtnText() {
        return CONTINUE_KEYWOD;
    }

    /**
     * 内部类 单例模式
     */
    private static final class Holder {
        private static final SuperAccessibilityService mAccessibilityService = new HuaWeiManager(App.sContext);
    }
}
