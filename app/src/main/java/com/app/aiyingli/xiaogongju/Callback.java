package com.app.aiyingli.xiaogongju;

/**
 * @author Android-小强 on 2019/6/10 13:38
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: Callback
 */
public interface Callback {
    /**
     * 操作成功
     *
     * @param msg
     */
    void onSuccess(String msg);

    /**
     * 操作失败
     *
     * @param msg
     */
    void onError(String msg);
}
