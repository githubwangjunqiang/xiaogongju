package com.app.aiyingli.xiaogongju.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.app.aiyingli.xiaogongju.IAccessibilityService;
import com.app.aiyingli.xiaogongju.entity.WorkBean;
import com.app.aiyingli.xiaogongju.utils.To;


/**
 * @author Android-小强 on 2018/11/8 15:48
 * @email: 15075818555@163.com
 * @ProjectName: iMoney
 */
public class AppAccessibility extends AccessibilityService {


    private static final String TAG = "12345";
    private static volatile WorkBean sWorkBean;

    public static final void setWorkBean(WorkBean workBean) {
        sWorkBean = workBean;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        try {
//            Log.d(TAG, "onAccessibilityEvent: " + event.toString());
            if (sWorkBean != null && sWorkBean.getAccessibilityService() != null) {
                sWorkBean.getAccessibilityService().onAccessibilityEvent(event);
                ((IAccessibilityService) sWorkBean.getAccessibilityService()).setAccessService(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            To.toast(e.getLocalizedMessage());
        }

    }

    @Override
    public void onInterrupt() {
        if (sWorkBean != null && sWorkBean.getAccessibilityService() != null) {
            sWorkBean.getAccessibilityService().onInterrupt();
        }
        Log.d(TAG, "MyAccessibility中断了");
        sWorkBean = null;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "已经连接 辅助功能服务");
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "辅助功能关闭了");
        if (sWorkBean != null && sWorkBean.getAccessibilityService() != null) {
            return sWorkBean.getAccessibilityService().onUnbind(intent);
        }
        sWorkBean = null;
        return super.onUnbind(intent);
    }


}
