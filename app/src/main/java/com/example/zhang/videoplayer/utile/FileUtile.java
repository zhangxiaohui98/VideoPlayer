package com.example.zhang.videoplayer.utile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhang on 2019/4/25.
 */

public class FileUtile {
    private static final String TAG = "FileUtile";

    /**
     * 扫描文件夹下的子文件夹
     * @param src
     * @param listFile
     * @return
     */
    public static List<File> scanFile(File src, List<File> listFile){
        // 如果是文件夹则取出他的子文件夹
        if (src.isDirectory()) {
            //listFile.add(new File(src.getAbsolutePath()));
            for (File sub : src.listFiles()) {
                if (sub.isDirectory()) {
                    if(!sub.getName().contains(".")){
                        listFile.add(new File(sub.getAbsolutePath()));
                    }
                }
            }
        }else{
            Log.d(TAG, "scanFile: 这不是文件夹");
            return null;
        }
        return listFile;
    }

    /**
     * 选择文件夹下的所有视频文件
     * @param src
     * @return
     */
    public static List<File> selectVideo (File src ){
        List<File> listFile =new ArrayList<>();
        // 如果是文件夹则取出他的子文件夹
        if (src.isDirectory()) {
            //listFile.add(new File(src.getAbsolutePath()));
            for (File sub : src.listFiles()) {
                if (sub.isDirectory()) {
                    for (File sub2 : selectVideo(sub)) {
                        listFile.add(sub2);
                    }
                }else if(isVideoFile(sub)){
                    listFile.add(sub);
                    //如是视频文件则放入list中
                }
            }
        }else{
            Log.d(TAG, "scanFile: 这不是文件夹");
            return null;
        }
        return listFile;
    }

    /**
     * 判断文件是否为视频文件
     * @param sub
     * @return
     */
    private static boolean isVideoFile(File sub) {
        String path=sub.getAbsolutePath();

        if (path.contains(".avi")||path.contains(".wmv")||path.contains(".mpeg")||path.contains(".mp4")){
            return true;
        }else {
            return false;
        }

    }


    /**
     * 其子文件是否全部都是文件
     * @param file
     * @return
     */
    public static boolean isAllFile(File file) {
        File[] fileList=file.listFiles();

        for (File fileFor:fileList) {
            if (fileFor.isDirectory()){
                return false;
            }
        }
        return true;
    }

    /**
     * 获取权限
     */
    public static void checkPermission(String TAG, Context context, Activity activity) {
        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(activity, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        } else {
            Log.e(TAG, "checkPermission: 已经授权！");
        }
    }

    /**
     * 根据路径得到视频缩略图
     * @param videoPath
     * @return
     */
    public static Bitmap getVideoPhoto(String videoPath) {
        MediaMetadataRetriever media =new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();
        bitmap=Bitmap.createScaledBitmap(bitmap,200,160,true);
        return bitmap;
    }
    /**
     * 获取视频总时长
     * @param path
     * @return
     */
    public static Long getVideoDuration(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        Long duration=Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        return duration/(60*1000);
    }
}
