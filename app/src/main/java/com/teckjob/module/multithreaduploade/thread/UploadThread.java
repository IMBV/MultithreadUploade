package com.teckjob.module.multithreaduploade.thread;

import android.content.Context;

import java.util.Random;

/**
 * Created by IMBV on 2018/3/24.
 */

public class UploadThread implements Runnable{
    private Context mContext;
    private UploaderTask mUploaderTask;
    private UploadCallback mUploadCallback;

    private int percent = 0;
    private Random mRandom;

    protected Thread thread;


    public UploadThread(Context mContext, UploaderTask mUploaderTask, UploadCallback mUploadCallback) {
        this.mContext = mContext;
        this.mUploaderTask = mUploaderTask;
        this.mUploadCallback = mUploadCallback;
        mRandom = new Random();
        thread  = new Thread(this,"uploader");
        thread.start();
    }

    @Override
    public void run() {
        //这可以上传阿里或者七牛云，我这使用使用模拟上传
        try {
            while(percent<=100){
                mUploadCallback.onProgress(mUploaderTask,percent);
                percent+=1;
                Thread.sleep(mRandom.nextInt(60)+30);
            }
            mUploadCallback.onSuccess(mUploaderTask);
        } catch (InterruptedException e) {
            mUploadCallback.onError(mUploaderTask,e.toString());
        }
    }
}
