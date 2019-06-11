package com.app.aiyingli.xiaogongju.entity;

import android.accessibilityservice.AccessibilityService;
import android.text.TextUtils;

import com.app.aiyingli.xiaogongju.YingYongBaoManager;

/**
 * @author Android-小强 on 2019/6/10 13:54
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.entity
 * @ClassName: WorkBean
 */
public class WorkBean {
    /**
     * 应用宝
     */
    public static final String YING_YONG_BAO = "com.tencent.android.qqdownloader";


    /**
     * 应用市场包名
     */
    private String maskPackName;
    /**
     * 任务指定的 金主 app 名称
     */
    private String appName;
    /**
     * 任务指定的 金主 关键字
     */
    private String keyWord;
    /**
     * 搜索界面的 活动名字
     */
    private String activityName;


    /**
     * 该任务 是否已经执行过 开始
     */
    private boolean start;
    /**
     * 该任务是否已经停止
     */
    private boolean stop;
    /**
     * 该任务如果 已经开始 那么 完成状态是啥样子的
     */
    private boolean success;


    public WorkBean() {
    }

    public AccessibilityService getAccessibilityService() {
        if (TextUtils.isEmpty(getMaskPackName())) {
            return null;
        }
        if (getMaskPackName().equals(YING_YONG_BAO)) {
            YingYongBaoManager singleCase = (YingYongBaoManager) YingYongBaoManager.getSingleCase();
            singleCase.setWorkBean(this);
            return YingYongBaoManager.getSingleCase();
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
}
