package com.app.aiyingli.xiaogongju.media;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.app.aiyingli.xiaogongju.utils.AppUtils;
import com.app.aiyingli.xiaogongju.utils.ScreenUtils;
import com.app.aiyingli.xiaogongju.utils.To;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * @author Android-小强 on 2019/6/13 10:58
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.media
 * @ClassName: MediaManager
 */
public class MediaManager implements IMediaManager {
    /**
     * 录屏 管理器
     */
    private MediaProjectionManager mMediaProjectionManager;
    /**
     * 多媒体 录屏工具
     */
    private MediaProjection mMediaProjection;
    /**
     * 设备 参数相关
     */
    private VirtualDisplay mVirtualDisplay;
    /**
     * 图片处理器
     */
    private ImageReader mImageReader;
    /**
     * 事件分发相关
     */
    private Handler handler;
    /**
     * 计时器
     */
    private Disposable mDisposable;
    /**
     * 存储文件的 路径  绝对路径
     */
    private String mFilePath;

    private MediaManager() {
    }

    public static final IMediaManager getManager() {
        return Holder.manager;
    }


    @Override
    public MediaProjectionManager createMediaProjectionManager(Context context) {
        mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        return mMediaProjectionManager;
    }

    @Override
    public void startScreenRecording(Context context, int resultCode, @Nullable Intent data) {
        if (mMediaProjectionManager == null) {
            mMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
        mImageReader = ImageReader.newInstance(ScreenUtils.getScreenWidth(context),
                ScreenUtils.getScreenHeight(context), PixelFormat.RGBA_8888, 2);
        //        mImageReader.setOnImageAvailableListener(reader -> {
//        }, getBackgroundHandler());
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("12212121212",
                mImageReader.getWidth(), mImageReader.getHeight(),
                Resources.getSystem().getDisplayMetrics().densityDpi,
//                    mImageReader.getWidth(), mImageReader.getHeight(), DisplayMetrics.DENSITY_260,
//                mImageReader.getWidth(), mImageReader.getHeight(), DisplayMetrics.DENSITY_XHIGH,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
        mFilePath = null;
        startCountdown(mImageReader);
    }

    @Override
    public void startGetBitmap(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            throw new NullPointerException("参数错误- filename=null");
        }
        mFilePath = fileName;
    }

    @Override
    public void startCountdown(ImageReader reader) {
        stopCountdown();
        mDisposable = Observable.interval(0, 800, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    if (!TextUtils.isEmpty(mFilePath)) {
                        Bitmap bitmapForImage = getBitmapForImage(mImageReader.acquireLatestImage());
                        if (bitmapForImage != null) {
                            String filePath = saveFileForBitmap(bitmapForImage, mFilePath);
                            if (!TextUtils.isEmpty(filePath)) {
                                mFilePath = null;
                            }
                        }
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    To.toast(throwable.getLocalizedMessage());
                });
    }

    @Override
    public Bitmap getBitmapForImage(Image image) {
        Bitmap bitmap = null;
        try {
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            To.toast(e.getLocalizedMessage());
        } finally {
            if (image != null) {
                image.close();
            }
        }
        return bitmap;
    }

    @Override
    public String saveFileForBitmap(Bitmap bitmap, String filePath) {

        FileOutputStream fileOutputStream = null;
        String path = "";
        try {
            fileOutputStream = new FileOutputStream(new File(filePath));
            boolean compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            if (compress) {
                path = filePath;
                AppUtils.scanGalleryFile(new String[]{path});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    @Override
    public void stopCountdown() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.isDisposed();
        }
        mDisposable = null;
    }

    @Override
    public void stopWork() {
        mFilePath = null;
        stopCountdown();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    @Override
    public Handler getBackgroundHandler() {
        if (handler == null) {
            HandlerThread backgroundThread =
                    new HandlerThread(getClass().getSimpleName(), android.os.Process
                            .THREAD_PRIORITY_BACKGROUND);
            backgroundThread.start();
            handler = new Handler(backgroundThread.getLooper());
        }
        return handler;
    }


    private static final class Holder {
        private static final IMediaManager manager = new MediaManager();
    }
}
