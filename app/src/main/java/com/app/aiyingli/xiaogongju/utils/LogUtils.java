package com.app.aiyingli.xiaogongju.utils;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.app.aiyingli.xiaogongju.App;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author Android-小强 on 2019/6/10 14:09
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.utils
 * @ClassName: LogUtils
 */
public class LogUtils {

    private static String customTagPrefix = "12345";

    public static void d(String msg) {
        StackTraceElement caller = getCallerStackTraceElement();
        String tag = generateTag(caller);
        d(tag, msg);
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        return tag;
    }

    public static void d(String tag, String msg) {
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        String finalTag = tag;
        Disposable subscribe = Observable.just(msg)
                .compose(RxCompose.composeThread())
                .subscribe(s -> {
                    Log.d(finalTag, "" + msg);
                }, throwable -> {
                });
    }
}
