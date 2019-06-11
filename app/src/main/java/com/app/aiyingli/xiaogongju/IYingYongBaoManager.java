package com.app.aiyingli.xiaogongju;

import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.entity.WorkBean;

/**
 * @author Android-小强 on 2019/6/11 9:59
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: IYingYongBaoManager
 */
public interface IYingYongBaoManager {
    /**
     * 检测窗口 是否符合 任务标准
     * @param workBean 任务实体类
     * @param event 当前 窗口变化标识
     */
    boolean detectionWindow(WorkBean workBean, AccessibilityEvent event);

    /**
     * 开始执行任务
     * @param workBean 任务实体类
     * @param event 当前 窗口变化标识
     */
    void startWork(WorkBean workBean,AccessibilityEvent event);

    /**
     * 滑动 内容
     * @param workBean
     * @param event
     */
    void slideContentOn(WorkBean workBean,AccessibilityEvent event);

    /**
     * 查找 下载 或者 继续 或者 安装按钮
     * @param accessibilityNodeInfo
     */
    void findButton(AccessibilityNodeInfo accessibilityNodeInfo);


    /**
     * 停止任务
     */
    void stopWork();

    /**
     * 成功
     */
    void successWork();



}
