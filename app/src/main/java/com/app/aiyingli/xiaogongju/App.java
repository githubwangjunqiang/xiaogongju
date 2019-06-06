package com.app.aiyingli.xiaogongju;

import android.app.Application;
import android.content.Context;

/**
 * @author Android-小强 on 2019/6/6 9:56
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju
 * @ClassName: App
 */
public class App extends Application {
    /**
     * 全局上下文
     */
    public static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }
}
