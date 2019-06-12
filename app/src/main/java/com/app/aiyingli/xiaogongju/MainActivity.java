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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.app.aiyingli.xiaogongju.entity.WorkBean;
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
    public static boolean kaishi;
    String key = "金融";
//    String app = "慧金融";
        String app = "金元宝";
    String maskName = "com.tencent.android.qqdownloader";
    String activityName = "SearchActivity";
//    String appPackName = "hy.heebank";
        String appPackName = "com.tianpin.juehuan";
    private EditText mEditTextAppPackName;
    /**
     * 开启辅助功能界面 标识
     */
    private boolean goOpenAccessService = false;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;
    private Handler handler;
    private String doc;
    private Disposable subscribe;
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
        Disposable subscribe = new RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mEditTextKey.setText(key);
                        mEditTextAppName.setText(app);
                        mEditTextAppPackName.setText(appPackName);


                    } else {
                        To.toast("没有权限");
                        finish();
                    }
                }, throwable -> {
                    To.toast("出错了");
                });
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);

        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            doc = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(System.currentTimeMillis()) + "";
            kaishi = false;
            clearSubscribe();

            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);

            mImageReader = ImageReader.newInstance(ScreenUtils.getScreenWidth(this),
                    ScreenUtils.getScreenHeight(this), PixelFormat.RGBA_8888, 2);
            setImagelistener();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("12212121212",
//                    mImageReader.getWidth(), mImageReader.getHeight(), Resources.getSystem().getDisplayMetrics().densityDpi,
                    mImageReader.getWidth(), mImageReader.getHeight(), DisplayMetrics.DENSITY_260,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        }
    }

    /**
     * 开始任务
     */
    private void startTask() {

        String keyName = mEditTextKey.getText().toString().trim();
        String appName = mEditTextAppName.getText().toString().trim();
        String appPackName = mEditTextAppPackName.getText().toString().trim();

        WorkBean workBean = new WorkBean(maskName, appName, appPackName, keyName, activityName);
        AppAccessibility.setWorkBean(workBean);
        boolean b = AppUtils.startMaskForkeyName(maskName, keyName);
    }

    private void clearSubscribe() {
        if (subscribe != null && !subscribe.isDisposed()) {
            subscribe.dispose();
        }
        subscribe = null;
    }

    private void setImagelistener() {

        mImageReader.setOnImageAvailableListener(reader -> {
            getImage(mImageReader);

        }, getBackgroundHandler());

    }

    private void getImage(ImageReader reader) {


        if (subscribe != null && !subscribe.isDisposed()) {
            return;
        }
        subscribe = Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    if (!kaishi) {
                        return;
                    }
                    kaishi = false;

                    Image image = reader.acquireLatestImage();
                    if (image == null) {
                        return;
                    }
                    int width = image.getWidth();
                    int height = image.getHeight();
                    final Image.Plane[] planes = image.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;
                    Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    final Bitmap bitmapFile = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                    image.close();
                    saveFile(bitmapFile);
                }, throwable -> {
                    Log.e("12345", "getImage: ", throwable);
                    stopLuping();
                    stopImageRead();
                });
    }

    private Handler getBackgroundHandler() {
        if (handler == null) {
            HandlerThread backgroundThread =
                    new HandlerThread("catwindow", android.os.Process
                            .THREAD_PRIORITY_BACKGROUND);
            backgroundThread.start();
            handler = new Handler(backgroundThread.getLooper());
        }
        return handler;
    }

    private void saveFile(Bitmap images) {
        try {
            File fileImage = null;
            if (images != null) {
                try {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File fileBaseDoc = new File(file.getAbsolutePath(), "小工具");
                    if (!fileBaseDoc.exists()) {
                        fileBaseDoc.mkdirs();
                    }
//                    File filedoc = new File(fileBaseDoc.getAbsolutePath(), doc);
//                    if (!filedoc.exists()) {
//                        filedoc.mkdirs();
//                    }
                    fileImage = new File(fileBaseDoc.getAbsolutePath(),
                            new SimpleDateFormat("yyyy_MM_dd:HH_mm_ss").format(System.currentTimeMillis()) + ".jpeg");

                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        boolean compress = images.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        if (compress) {
                            AppUtils.scanGalleryFile(new String[]{fileImage.getAbsolutePath()});
                            To.toast("自动保存截图到相册");
                            showImage(fileImage);
                        }
                        out.flush();
                        out.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fileImage = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            To.toast("" + e.getLocalizedMessage());
        }
    }

    private void stopLuping() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;

        if (mMediaProjection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjection.stop();
            }
            mMediaProjection = null;
        }
    }

    private void stopImageRead() {
        mImageReader.setOnImageAvailableListener(null, null);
        if (mImageReader != null) {
            mImageReader.close();
        }
    }

    /**
     * 显示图片
     *
     * @param fileImage
     */
    private void showImage(File fileImage) {
        runOnUiThread(() -> {
            if (isDestroyed() || isFinishing()) {
                return;
            }
            Glide.with(mImageView).load(fileImage).into(mImageView);
        });

    }

    public void jieshu(View view) {
        stopLuping();
        stopImageRead();
        clearSubscribe();
    }
}
