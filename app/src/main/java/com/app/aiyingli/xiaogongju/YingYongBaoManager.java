package com.app.aiyingli.xiaogongju;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

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
public class YingYongBaoManager extends SuperAccessibilityService {
    /**
     * 应用宝 包名
     */
    public static final String PACK_NAME = "com.tencent.android.qqdownloader";
    /**
     * 列表界面 的初始化按钮 下载 关键字
     */
    public static final String DOWN_KEYWOD = "下载";
    /**
     * 列表界面 的下载按钮点击过之后  是显示 继续 关键字
     */
    public static final String CONTINUE_KEYWOD = "继续";
    /**
     * 第一次启动 有一个 弹窗提示用户 下面会申请权限 判断是弹框
     */
    public static final String ACCESS_DIALOG = "Dialog";
    /**
     * 第一次启动 有一个 弹窗提示用户 下面会申请权限 标题文案
     */
    public static final String ACCESS_TITLE = "权限申请";
    /**
     * 第一次启动 有一个 弹窗提示用户 下面会申请权限 标题文案
     */
    public static final String ACCESS_TITLE2 = "获取权限提示";
    /**
     * 第一次启动 有一个 弹窗提示用户 下面会申请权限 点击 去授权 按钮文案
     */
    public static final String ACCESS_GOTOGRANT = "去授权";
    /**
     * 第一次启动 有一个 弹窗提示用户 下面会申请权限 点击 确定 按钮文案
     */
    public static final String ACCESS_GOTOGRANT2 = "确定";

    protected YingYongBaoManager(Context context) {
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
            //应用宝第一次打开 有权限申请 请选择去授权
            if (detectionPermission(getWorkBean(), event)) {
                permitPermission(getWorkBean(), mServiceReference.get());
            }
            //系统权限申请框
            if (detectionSystemPermission(getWorkBean(), event)) {
                clickPermission(getWorkBean(), mServiceReference.get());
            }

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
            //系统权限申请框
            if (detectionSystemPermission(getWorkBean(), event)) {
                clickPermission(getWorkBean(), mServiceReference.get());
            }
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
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            //有内容滚动  判断是否执行任务
            if (detectionContentChange(getWorkBean(), event)) {
                getWorkBean().setStart(true);
                startWork(getWorkBean(), event);
            }
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            //应用宝 安装完成 app 后 会发出此通知
            //安装完成 的界面 点击打开按钮
            if (isSystemOpenActivity(getWorkBean(), event)) {
                openApp(mServiceReference.get());
            }
        }
    }


    @Override
    public boolean detectionPermission(WorkBean workBean, AccessibilityEvent event) {
        if (workBean == null || event == null) {
            return false;
        }
        if (!PACK_NAME.equals(event.getPackageName())) {
            return false;
        }
        if (workBean.isStart()) {
            return false;
        }
        CharSequence className = event.getClassName();
        if (TextUtils.isEmpty(className)) {
            return false;
        }
        if (!className.toString().contains(ACCESS_DIALOG)) {
            return false;
        }
        List<CharSequence> texts = event.getText();
        if (texts == null || texts.isEmpty()) {
            return false;
        }
        if (texts.contains(ACCESS_TITLE)||texts.contains(ACCESS_TITLE2)) {
            return true;
        }

        return false;
    }

    @Override
    public void permitPermission(WorkBean workBean, AccessibilityService service) {
        if (workBean == null || service == null) {
            return;
        }

        AccessibilityNodeInfo rootInActiveWindow = service.getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return;
        }
        AccessibilityNodeInfo viewByText = FindViewUtils.findViewByText(ACCESS_GOTOGRANT, service);
        if (viewByText == null) {
            To.toast("没有找到去授予按钮，去找确定按钮");
            viewByText = FindViewUtils.findViewByText(ACCESS_GOTOGRANT2, service);
        }
        if(viewByText == null){
            To.toast("没有找到 指定 按钮");
            return;
        }
        boolean performViewClick = FindViewUtils.performViewClick(viewByText);
        LogUtils.d("点击去授予成功");
        workBean.setClickAccess(true);

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
        private static final SuperAccessibilityService mAccessibilityService = new YingYongBaoManager(App.sContext);
    }
}
