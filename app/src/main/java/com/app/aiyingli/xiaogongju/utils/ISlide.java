package com.app.aiyingli.xiaogongju.utils;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.Callback;

/**
 * @author Android-小强 on 2019/6/10 13:38
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: ISlide
 */
public interface ISlide {


    /**
     * 滑动 屏幕内容向上滚动  模拟手指自下而上 滑动
     *
     * @param callback 操作回调
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    void slideContentOn(AccessibilityService accessibilityService, Callback callback);

    /**
     * 滑动 屏幕内容向上滚动  模拟手指自下而上 滑动
     *
     * @param info     必须是 可以滚动的view 不然 调用此方式不会滚动[ListView...]
     * @param callback 操作回调
     */
    void slideContentOn(AccessibilityNodeInfo info, Callback callback);

    /**
     * 滑动 屏幕内容向下滚动  模拟手指自上而下 滑动
     *
     * @param callback 操作回调
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    void slideContentDown(AccessibilityService accessibilityService, Callback callback);

    /**
     * 滑动 屏幕内容向下滚动  模拟手指自上而下 滑动
     *
     * @param info     必须是 可以滚动的view 不然 调用此方式不会滚动[ListView...]
     * @param callback 操作回调
     */
    void slideContentDown(AccessibilityNodeInfo info, Callback callback);

    /**
     * 获取可以滑动的 viewl
     *
     * @param info
     * @return
     */
    AccessibilityNodeInfo getSlideInfo(AccessibilityService info);


}
