package com.app.aiyingli.xiaogongju.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

/**
 * @author Android-小强 on 2019/6/13 10:59
 * @email: 15075818555@163.com
 * @ProjectName: xiaogongju
 * @Package: com.app.aiyingli.xiaogongju.media
 * @ClassName: IMediaManager
 */
public interface IMediaManager {

    /**
     * 创建 截屏权限 管理器
     * @param context 上下文
     * @return 返回创建好的对象
     */
    MediaProjectionManager createMediaProjectionManager(Context context);
    /**
     * 开始录屏
     *
     * @param context
     * @param resultCode
     * @param data
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void startScreenRecording(Context context, int resultCode, @Nullable Intent data);

    /**
     * 准备 保存当前截图
     *
     * @param fileName
     */
    void startGetBitmap(String fileName);

    /**
     * 开始倒计时
     *
     * @param reader
     */
    void startCountdown(ImageReader reader);

    /**
     * 通过image 获取 bitmap
     *
     * @param image
     * @return
     */
    Bitmap getBitmapForImage(Image image);

    /**
     * 保存 bitmap 到文件
     *
     * @param bitmap
     * @param filePath
     * @return
     */
    String saveFileForBitmap(Bitmap bitmap, String filePath);


    /**
     * 停止倒计时
     */
    void stopCountdown();

    /**
     * 停止当前任务
     */
    void stopWork();

    /**
     * 获取 事件分发
     *
     * @return
     */
    Handler getBackgroundHandler();
}
