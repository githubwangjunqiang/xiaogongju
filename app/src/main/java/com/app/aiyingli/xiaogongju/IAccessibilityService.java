package com.app.aiyingli.xiaogongju;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
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
     * 是否是指定app 运行打开 提示的 让用户知道 下面操作 开始获取权限的界面 或者 dialog
     *
     * @param workBean
     * @param event
     * @return 如果是 真 那么 开始 寻找 去授予按钮 模拟点击
     */
    boolean detectionPermission(WorkBean workBean, AccessibilityEvent event);

    /**
     * 如果是权限提示界面 那么 自动寻找 去授予 按钮 并且点击她 让下面 继续操作 系统权限申请
     *
     * @param workBean
     * @param service
     */
    void permitPermission(WorkBean workBean, AccessibilityService service);

    /**
     * 检测是否系统 权限申请框  如果是指定app 的权限申请 那么去寻找 允许按钮
     * @param workBean
     * @param event
     * @return
     */
    boolean detectionSystemPermission(WorkBean workBean, AccessibilityEvent event);

    /**
     * 如果是 系统权限 申请 而且 需要自动允许 那么 寻找 允许 按钮 并点击 它
     * @param workBean
     * @param service
     */
    void clickPermission(WorkBean workBean, AccessibilityService service);

    /**
     * 开始执行任务
     *
     * @param workBean 任务实体类
     * @param event    当前 窗口变化标识
     */
    void startWork(WorkBean workBean, AccessibilityEvent event);

    /**
     * 获取 指定app 的名称 的 view
     * @param workBean 任务 对象
     * @param service  服务
     * @return 返回如果找到了 否则为null
     */
    AccessibilityNodeInfo getKeyWordnodeInfo(WorkBean workBean, AccessibilityService service);

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
     * 比较 两个 位置  来判断 是否 属于 统一item 的按钮
     * @param rectAppName
     * @param rectButton
     * @return
     */
    boolean compareRect(Rect rectAppName,Rect rectButton);



    /**
     * 停止任务
     */
    void stopWork();

    /**
     * 成功
     */
    void successWork();


    /**
     * 是否是 安装界面 不是静默安装的市场 会跳转安装界面 那么需要我们 寻找安装按钮
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
     * 自动安装 如果市场不具备静默安装 那么必须寻找 安装按钮 模拟点击安装
     *
     * @param nodeInfo
     */
    void autoInstallation(AccessibilityNodeInfo nodeInfo);

    /**
     * 打开app   市场安装完成 寻找打开app 按钮 然后模拟点击
     *
     * @param service
     */
    void openApp(AccessibilityService service);


}
