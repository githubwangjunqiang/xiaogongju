package com.app.aiyingli.xiaogongju.entity;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.app.aiyingli.xiaogongju.App;
import com.app.aiyingli.xiaogongju.HuaWeiManager;
import com.app.aiyingli.xiaogongju.YingYongBaoManager;
import com.app.aiyingli.xiaogongju.service.SuperAccessibilityService;

import java.io.File;

/**
 * @author Android-小强 on 2019/6/10 13:54
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.entity
 * @ClassName: WorkBean
 */
public class WorkBean {
    /**
     * 安装界面 包名  系统
     */
    public static final String INSTALL_ACTIVITY = "com.android.packageinstaller";
    //小米手机
    public static final String Xiao_mi = "xiaomi";
    //华为手机
    public static final String Hua_wei = "huawei";

    /**
     * 打开  按钮
     */
    public static final String OPEN = "打开";


    /**
     * 任务id
     */
    private String id;
    /**
     * 应用市场包名
     */
    private String maskPackName;
    /**
     * 任务指定的 金主 app 名称
     */
    private String appName;
    /**
     * 任务指定的 金主 app 包名
     */
    private String appPackName;
    /**
     * 任务指定的 金主 关键字
     */
    private String keyWord;
    /**
     * 搜索界面的 活动名字
     */
    private String activityName;

    /**
     * 是否 是第一次打开 点击权限提示框 去授权 按钮 后 现实的 系统权限申请界面
     */
    private boolean clickAccess;
    /**
     * 该任务 是否已经执行过 开始
     */
    private boolean start;
    /**
     * 该任务是否已经停止
     */
    private boolean stop;
    /**
     * 该任务如果 已经开始 并且 已经找到指定app的位置 点击了 下载按钮
     */
    private boolean success;
    /**
     * 下载指定app完成后 不是静默安装的市场 需要自动安装 标识已经找到了自动安装的按钮 并且点击了
     */
    private boolean startInstall;
    /**
     * 市场 安装完成后 找到打开按钮 打开app 标识是否打开了 app
     */
    private boolean openApp;

    public WorkBean(String id, String maskPackName, String appName, String appPackName, String keyWord, String activityName) {
        this.id = id;
        this.maskPackName = maskPackName;
        this.appName = appName;
        this.appPackName = appPackName;
        this.keyWord = keyWord;
        this.activityName = activityName;
    }

    /**
     * 安装界面 包名  小米优化
     */
    public static final String getInstallName() {
        String brand = Build.BRAND;
        if (Xiao_mi.equalsIgnoreCase(brand)) {
            return "com.miui.packageinstaller";
        }
        if (Hua_wei.equalsIgnoreCase(brand)) {
            return INSTALL_ACTIVITY;
        }
        return INSTALL_ACTIVITY;
    }

    /**
     * 获取 安装完成后 提示的打开 app 界面
     *
     * @return
     */
    public static String getOpenAppcPackName() {
        String brand = Build.BRAND;
        if (Xiao_mi.equalsIgnoreCase(brand)) {
            return "com.android.systemui";
        }
        if (Hua_wei.equalsIgnoreCase(brand)) {
            return "com.android.packageinstalle";
        }
        return "com.android.systemui";
    }

    /**
     * 权限 界面 根据手机 返回不同的 包名 来匹配
     *
     * @return
     */
    public static String getPermission() {
        String brand = Build.BRAND;
        if (Xiao_mi.equalsIgnoreCase(brand)) {
            return "com.lbe.security.miui";
        }
        if (Hua_wei.equalsIgnoreCase(brand)) {
            return "com.android.packageinstaller";
        }
        return "com.lbe.security.miui";
    }

    /**
     * 根据机型 获取安装界面的 安装按钮 或者 继续安装
     *
     * @return
     */
    public static String getPackageInstallerButton() {
        String brand = Build.BRAND;
        if (Xiao_mi.equalsIgnoreCase(brand)) {
            return "安装";
        }
        if (Hua_wei.equalsIgnoreCase(brand)) {
            return "继续安装";
        }
        return "安装";
    }

    /**
     * 获取系统权限 弹框 的 按钮  允许 按钮
     * @return
     */
    public static String getPermissionBtnText() {
        String brand = Build.BRAND;
        if (Xiao_mi.equalsIgnoreCase(brand)) {
            return "允许";
        }
        if (Hua_wei.equalsIgnoreCase(brand)) {
            return "始终允许";
        }
        return "允许";
    }

    public boolean isClickAccess() {
        return clickAccess;
    }

    public void setClickAccess(boolean clickAccess) {
        this.clickAccess = clickAccess;
    }

    public boolean isOpenApp() {
        return openApp;
    }

    public void setOpenApp(boolean openApp) {
        this.openApp = openApp;
    }

    public boolean isStartInstall() {
        return startInstall;
    }

    public void setStartInstall(boolean startInstall) {
        this.startInstall = startInstall;
    }

    public String getAppPackName() {
        return appPackName == null ? "" : appPackName;
    }

    public void setAppPackName(String appPackName) {
        this.appPackName = appPackName;
    }

    public AccessibilityService getAccessibilityService() {
        if (TextUtils.isEmpty(getMaskPackName())) {
            return null;
        }
        if (getMaskPackName().equals(YingYongBaoManager.PACK_NAME)) {
            SuperAccessibilityService singleCase = YingYongBaoManager.getSingleCase();
            singleCase.setWorkBean(this);
            return singleCase;
        }
        if (getMaskPackName().equals(HuaWeiManager.PACK_NAME)) {
            SuperAccessibilityService singleCase = HuaWeiManager.getSingleCase();
            singleCase.setWorkBean(this);
            return singleCase;
        }

        return null;
    }

    public String getMaskPackName() {
        return maskPackName == null ? "" : maskPackName;
    }

    public void setMaskPackName(String maskPackName) {
        this.maskPackName = maskPackName;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getActivityName() {
        return activityName == null ? "" : activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getAppName() {
        return appName == null ? "" : appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getKeyWord() {
        return keyWord == null ? "" : keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * 获取 此任务 保存截图的文件夹
     *
     * @return
     */
    public String getFileDoc() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!file.exists()) {
            file.mkdirs();
        }
        File filedoc = new File(file, getId());
        if (!filedoc.exists()) {
            filedoc.mkdirs();
        }
        return filedoc.getAbsolutePath();
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
