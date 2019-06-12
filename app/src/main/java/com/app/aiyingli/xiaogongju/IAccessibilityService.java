package com.app.aiyingli.xiaogongju;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.entity.WorkBean;

/**
 * @author Android-小强 on 2019/6/11 18:01
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: IAccessibilityService
 */
public interface IAccessibilityService {
    /**
     * 设置 服务
     *
     * @param accessService
     */
    void setAccessService(AccessibilityService accessService);

    /**
     * 检测窗口 是否符合 任务标准
     *
     * @param workBean 任务实体类
     * @param event    当前 窗口变化标识
     * @return 返回true 符合标准 可以使用 false-》不符合标准 不可以执行任务
     */
    boolean detectionWindow(WorkBean workBean, AccessibilityEvent event);

    /**
     * 内容变化后 是否符合 任务标准 任务是否开始 如果没有开始 那么就按照标准 执行任务 预防 窗口变化监听
     * 没有执行任务 造成的 延误
     *
     * @param workBean 任务实体类
     * @param event    当前 窗口变化标识
     * @return 返回true 符合标准 可以使用 false-》不符合标准 不可以执行任务
     */
    boolean detectionContentChange(WorkBean workBean, AccessibilityEvent event);

    /**
     * 开始执行任务
     *
     * @param workBean 任务实体类
     * @param event    当前 窗口变化标识
     */
    void startWork(WorkBean workBean, AccessibilityEvent event);

    /**
     * 滑动 内容
     *
     * @param workBean
     * @param event
     */
    void slideContentOn(WorkBean workBean, AccessibilityEvent event);

    /**
     * 查找 下载 或者 继续 或者 安装按钮
     *
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


    /**
     * 是否是 安装界面
     *
     * @param workBean
     * @param event
     * @return 返回true-》可以  false-》不可以
     */
    boolean isTaskInstallActivity(WorkBean workBean, AccessibilityEvent event);
    /**
     * 是否是 安装界面 完成 之后 打开的界面
     *
     * @param workBean
     * @param event
     * @return 返回true-》可以  false-》不可以
     */
    boolean isSystemOpenActivity(WorkBean workBean, AccessibilityEvent event);

    /**
     * 自动安装
     *
     * @param nodeInfo
     */
    void autoInstallation(AccessibilityNodeInfo nodeInfo);

    /**
     * 打开app
     * @param service
     */
    void openApp(AccessibilityService service);


}
