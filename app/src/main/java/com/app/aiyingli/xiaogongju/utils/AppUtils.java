package com.app.aiyingli.xiaogongju.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.app.aiyingli.xiaogongju.App;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author Android-小强 on 2019/6/6 10:50
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.utils
 * @ClassName: AppUtils
 */
public class AppUtils {
    /**
     * 应用是否安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstalled(Context context, String packageName) {
        try {
            boolean installed = false;
            if (TextUtils.isEmpty(packageName)) {
                return false;
            }
            List<ApplicationInfo> installedApplications = context.getPackageManager().getInstalledApplications(0);
            for (ApplicationInfo in : installedApplications) {
                if (packageName.equals(in.packageName)) {
                    installed = true;
                    break;
                } else {
                    installed = false;
                }
            }
            return installed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 刷新图库
     *
     * @param stringArrayExtr
     */
    public static void scanGalleryFile(String[] stringArrayExtr) {
        Disposable subscribe = Observable.just(stringArrayExtr)
                .compose(RxCompose.composeThread())
                .subscribe(strings -> {
                    if (strings == null) {
                        return;
                    }
                    if (strings.length < 1) {
                        return;
                    }
                    String[] types = new String[strings.length];
                    for (int i = 0; i < strings.length; i++) {
                        types[i] = "image/" + strings[i].substring(strings[i].lastIndexOf(".") + 1);
                    }
                    MediaScannerConnection.scanFile(App.sContext, strings, types,
                            new MediaScannerConnection.MediaScannerConnectionClient() {
                                @Override
                                public void onMediaScannerConnected() {

                                }

                                @Override
                                public void onScanCompleted(String path, Uri uri) {

                                }
                            });
                }, throwable -> {

                });
    }

    /**
     * 跳转到应用市场 指定app 界面
     *
     * @param pactName
     * @return
     */
    public static boolean startActivityForPackName(String pactName, String appName) {


        if (TextUtils.isEmpty(appName)) {
            return startActivityForPackName(pactName);
        }
        try {
            Uri uri = Uri.parse("market://details?id=" + appName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(pactName)) {
                intent.setPackage(pactName);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.sContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            return startActivityForPackName(pactName);
        }
    }

    /**
     * 跳转到指定app界面
     *
     * @param name
     * @return
     */
    public static boolean startActivityForPackName(String name) {
        try {
            if (!TextUtils.isEmpty(name)) {
                PackageManager packageManager = App.sContext.getPackageManager();
                Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(name);
                launchIntentForPackage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntentForPackage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                launchIntentForPackage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                App.sContext.startActivity(launchIntentForPackage);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 跳转到应用市场 指定关键词 界面
     *
     * @param pactName
     * @return
     */
    public static boolean startMaskForkeyName(String pactName, String keyName) {

        if (TextUtils.isEmpty(pactName)) {
            return false;
        }
        if (TextUtils.isEmpty(keyName)) {
            return false;
        }
        try {
            Uri uri = Uri.parse("market://search?q=" + keyName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(pactName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            App.sContext.startActivity(intent);
            return true;
        } catch (Exception e) {
            return startActivityForPackName(pactName);
        }
    }

    /**
     * 隐藏键盘
     */
    public static void hideInput(EditText editText) {
        InputMethodManager imm = (InputMethodManager) App.sContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != editText) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
}
