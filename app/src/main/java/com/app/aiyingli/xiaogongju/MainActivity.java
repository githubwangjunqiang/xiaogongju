package com.app.aiyingli.xiaogongju;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.app.aiyingli.xiaogongju.entity.WorkBean;
import com.app.aiyingli.xiaogongju.media.MediaManager;
import com.app.aiyingli.xiaogongju.service.AppAccessibility;
import com.app.aiyingli.xiaogongju.utils.AppUtils;
import com.app.aiyingli.xiaogongju.utils.FindViewUtils;
import com.app.aiyingli.xiaogongju.utils.ScreenUtils;
import com.app.aiyingli.xiaogongju.utils.To;
import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_MEDIA_PROJECTION = 1001;
    String key = "金融";
//    String key = "视频";
//    String app = "分期花";
//    String appPackName = "com.fenqi.loan";

    String app = "金元宝";
    String appPackName = "com.tianpin.juehuan";

//    String app = "抖音短视频";
//    String appPackName = "com.ss.android.ugc.aweme";


//    String maskName = HuaWeiManager.PACK_NAME;
    String maskName = YingYongBaoManager.PACK_NAME;
    String activityName = "SearchActivity";


    private EditText mEditTextAppPackName;
    private Button mButton;
    /**
     * 开启辅助功能界面 标识
     */
    private boolean goOpenAccessService = false;
    private ImageView mImageView;
    private EditText mEditTextKey, mEditTextAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.ivimage);
        mEditTextKey = findViewById(R.id.etkey);
        mEditTextAppName = findViewById(R.id.etappname);
        mEditTextAppPackName = findViewById(R.id.erapp_packname);
        mButton = findViewById(R.id.btn);

        mImageView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                To.toast("打开相机失败");
                return;
            }
        });


        Disposable subscribe = new RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mEditTextKey.setText(key);
                        mEditTextAppName.setText(app);
                        mEditTextAppPackName.setText(appPackName);
                        mButton.setText("打开" + maskName + "搜索关键词下载");
                    } else {
                        To.toast("没有权限");
                        finish();
                    }
                }, throwable -> {
                    To.toast("出错了");
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.getManager().stopWork();
    }

    /**
     * 检测辅助功能 是否开启
     *
     * @param view
     */
    public void checkIfAccessibilityIsEnabled(View view) {

        boolean b = detectionAuthority();
        if (b) {
            To.toast("已经开启辅助功能");
            return;
        }
        FindViewUtils.goOpenAccessService(this);
        goOpenAccessService = true;
    }

    /**
     * 检测权限
     *
     * @return
     */
    private boolean detectionAuthority() {
        boolean accessibilitySettingsOn = FindViewUtils.isAccessibilitySettingsOn(this);
        return accessibilitySettingsOn;
    }

    /**
     * 打开应用宝关键字界面
     */
    public void openTheApplicationTreasureKeywordInterface(View view) {

        AppUtils.hideInput(mEditTextAppName);
        AppUtils.hideInput(mEditTextKey);


        boolean b = detectionAuthority();
        if (!b) {
            To.toast("请开启辅助功能");
            return;
        }

        String key = mEditTextKey.getText().toString().trim();
        String appName = mEditTextAppName.getText().toString().trim();

        if (TextUtils.isEmpty(key)) {
            To.toast("请输入关键词");
            return;
        }
        if (TextUtils.isEmpty(appName)) {
            To.toast("请输入真实app名称");
            return;
        }
        MediaProjectionManager mediaProjectionManager = MediaManager.getManager().createMediaProjectionManager(this);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != RESULT_OK) {
                To.toast("用户取消了");
                return;
            }
            init(resultCode, data);
            startTask();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (goOpenAccessService) {
            boolean b = detectionAuthority();
            if (b) {
                To.toast("已经开启辅助功能");
                return;
            }
            goOpenAccessService = false;
        }
    }

    private void init(int resultCode, @Nullable Intent data) {
        MediaManager.getManager().startScreenRecording(this, resultCode, data);
    }

    /**
     * 开始任务
     */
    private void startTask() {

        String keyName = mEditTextKey.getText().toString().trim();
        String appName = mEditTextAppName.getText().toString().trim();
        String appPackName = mEditTextAppPackName.getText().toString().trim();

        if (AppUtils.isInstalled(this, appPackName)) {
            To.toast("您已经下载指定app了");
            return;
        }
        if (!AppUtils.isInstalled(this, maskName)) {
            To.toast("您还没有下载指定应用市场");
            return;
        }


        WorkBean workBean = new WorkBean(appName, maskName, appName, appPackName, keyName, activityName);
        AppAccessibility.setWorkBean(workBean);
        boolean b = AppUtils.startMaskForkeyName(maskName, keyName);
    }

}
