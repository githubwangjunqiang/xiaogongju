package com.app.aiyingli.xiaogongju.utils;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.app.aiyingli.xiaogongju.App;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author Android-小强 on 2019/6/6 9:54
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.utils
 * @ClassName: To
 */
public class To {
    /**
     * 全局土司
     */
    private static Toast sToast;

    /**
     * 土司
     */
    public static void toast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Disposable subscribe = Observable.just(msg)
                .compose(RxCompose.composeThread())
                .subscribe(s -> {
                    Log.d("12345", "toast: " + msg);
                    if (sToast != null) {
                        sToast.cancel();
                        sToast = null;
                    }
                    sToast = Toast.makeText(App.sContext, msg, Toast.LENGTH_SHORT);
                    sToast.show();
                }, throwable -> {
                });
    }
}
