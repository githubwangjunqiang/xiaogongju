package com.app.aiyingli.xiaogongju.utils;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Android-小强 on 2018/11/8 15:48
 * @email: 15075818555@163.com
 * @ProjectName: iMoney
 */
public class RxCompose {


    /**
     * 只改变线程调度
     *
     * @return
     */
    public static <T> ObservableTransformer<T, T> composeThread() {
        return upstream -> upstream.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }


}
