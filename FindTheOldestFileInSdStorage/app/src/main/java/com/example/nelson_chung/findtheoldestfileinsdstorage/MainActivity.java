package com.example.nelson_chung.findtheoldestfileinsdstorage;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String ACTIVITY_TAG="FindTheOldestFileInSdStorage";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int MINIMAL_REMAILING_SIZE_IN_SDCARD = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void DoStartCheck(View view){

        String mStr_SDPath = GetSDCardPath();
        long mRemainingSizeinSDcard = CheckRemainingSizeinSDcard(mStr_SDPath);
        //if( mRemainingSizeinSDcard < MINIMAL_REMAILING_SIZE_IN_SDCARD)
        {
            //Delete oldest file
            FindOldestFileAndDelete(mStr_SDPath);
        }
    }
    public void FindOldestFileAndDelete(String strPath){
        File sd_directory = new File(strPath);
        File[] files = sd_directory.listFiles();

        int oldest_file_index = 0;
        long oldest_file_modified_time = 0;

        /*
        Log.d(ACTIVITY_TAG, "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                Log.d(ACTIVITY_TAG, "FileName:" + files[i].getName());
                Log.d(ACTIVITY_TAG, "FileName last modified time:" + files[i].lastModified());
            }
        }
        */

        //find the first file
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                oldest_file_index = i;
                oldest_file_modified_time = files[oldest_file_index].lastModified();
                i = files.length;
            }
        }

        for (int i = oldest_file_index+1; i < files.length; i++) {
            if (files[i].isFile()) {
                if (files[i].lastModified() < oldest_file_modified_time) {
                    oldest_file_index = i;
                    oldest_file_modified_time = files[oldest_file_index].lastModified();
                }
            }
        }
        Log.d(ACTIVITY_TAG, "Oldest FileName:" + files[oldest_file_index].getName());
        Log.d(ACTIVITY_TAG, "Oldest FileName last modified time:" + files[oldest_file_index].lastModified());

        try {

            /*
            String strCompleteFilepath = strPath + "/" + files[oldest_file_index].getName();
            File deletefile = new File(strCompleteFilepath);

            //delete oldest file
            if(deletefile.delete()){
                //delete ok
                Log.d(ACTIVITY_TAG, "Delete oldest file -\"" + deletefile.getName() + "\" done");
            } else{
                //delete fail
                Log.d(ACTIVITY_TAG, "Delete oldest file -\"" + deletefile.getName() + "\" fail");
            }
            */
            Log.d(ACTIVITY_TAG, "Oldest file -\"" + files[oldest_file_index].getAbsolutePath());

            if (files[oldest_file_index].delete()) {
                //delete ok
                Log.d(ACTIVITY_TAG, "Delete oldest file -\"" + files[oldest_file_index].getName() + "\" done");
            } else {
                //delete ok
                Log.d(ACTIVITY_TAG, "Delete oldest file -\"" + files[oldest_file_index].getName() + "\" fail");
            }

        }catch(Exception e){
            Log.d(ACTIVITY_TAG, "Exception is " + e.toString());
        }

    }
    public String GetSDCardPath(){
        try {
            StorageManager mStorageManager;
            Method mMethodGetPaths;
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths",null);
            String[] paths = (String[]) mMethodGetPaths.invoke(mStorageManager, null);
            Log.i(ACTIVITY_TAG, "NelsonDBG: sd card path is " + paths[1]);
            return paths[1];
        }
        catch (Exception e){
            Log.e(ACTIVITY_TAG, "getSecondaryStoragePath() failed", e);
            return null;
        }

    }

    public long CheckRemainingSizeinSDcard(String strPath){
        long mRemainingSize = 0;
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE
            );
        }else {
            //已有權限，執行
            File sd_directory = new File(strPath);
            /*
            File[] files = sd_directory.listFiles();

            Log.d(ACTIVITY_TAG, "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    Log.d(ACTIVITY_TAG, "FileName:" + files[i].getName());
                    Log.d(ACTIVITY_TAG, "FileName last modified time:" + files[i].lastModified());
                }
            }
            */

            StatFs stat = new StatFs(sd_directory.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            Log.d(ACTIVITY_TAG, "blockSize: " + blockSize);
            Log.d(ACTIVITY_TAG, "availableBlocks: " + availableBlocks);
            Log.d(ACTIVITY_TAG, "Remaining size in sd card(GB): " + formatSize(availableBlocks * blockSize));
            mRemainingSize = formatSizebyMB(availableBlocks * blockSize);
        }
        return mRemainingSize;
    }
    //Reference from https://stackoverflow.com/questions/8133417/android-get-free-size-of-internal-external-memory
    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
                if (size >= 1024){
                    suffix = "GB";
                    size /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
    public static long formatSizebyMB(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }
        return size;
    }
}
