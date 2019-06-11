package com.app.aiyingli.xiaogongju;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.aiyingli.xiaogongju.entity.WorkBean;
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
public class YingYongBaoManager extends AccessibilityService implements IYingYongBaoManager, IAccessibilityService {

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
//        LogUtils.d("event->" + event.toString());

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!detectionWindow(mWorkBean, event)) {
                return;
            }
            mWorkBean.setStart(true);
            startWork(mWorkBean, event);
        }
    }

    @Override
    public void onInterrupt() {

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
        String keyDown = "下载";
        List<AccessibilityNodeInfo> btn = FindViewUtils.findViewByTexts(keyDown, this);
        if (mServiceReference.get() == null) {
            return;
        }
        btn = FindViewUtils.findViewByTexts(keyDown, mServiceReference.get());
        if (btn != null) {
            for (AccessibilityNodeInfo nodeInfo : btn) {
                if (nodeInfo != null) {
                    if (nodeInfo.getText() == null) {
                        continue;
                    }
                    String trim = nodeInfo.getText().toString().trim();
                    if (!trim.equals(keyDown)) {
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
            keyDown = "继续";
            List<AccessibilityNodeInfo> proceedBtn = FindViewUtils.findViewByTexts(keyDown, this);
            if (mServiceReference.get() == null) {
                return;
            }
            proceedBtn = FindViewUtils.findViewByTexts(keyDown, mServiceReference.get());
            if (proceedBtn != null) {
                for (AccessibilityNodeInfo nodeInfo : proceedBtn) {
                    if (nodeInfo != null) {
                        CharSequence charSequence = nodeInfo.getText() == null ? nodeInfo.getContentDescription() : nodeInfo.getText();
                        if (charSequence == null) {
                            continue;
                        }
                        String trim = charSequence.toString().trim();

                        if (!trim.equals(keyDown)) {
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
        To.toast("点击按钮->" + b);
        successWork();
        Disposable subscribe = Observable.timer(500, TimeUnit.MILLISECONDS)
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

    /**
     * 内部类 单例模式
     */
    private static final class Holder {
        private static final AccessibilityService mAccessibilityService = new YingYongBaoManager();
    }
}
