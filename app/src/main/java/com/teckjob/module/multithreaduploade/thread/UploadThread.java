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
            percent = (int) mUploaderTask.progress;//这是的进度应该是上传的是文件已经上传的大小
            while(percent<=100){
                percent+=1;
                Thread.sleep(mRandom.nextInt(60)+300);
                mUploadCallback.onProgress(mUploaderTask,percent);
                try {
                    checkPause();
                } catch (StopRequest e) {
                    notifyThroughDatabase(e.mFinalStatus);
                    break;
                }
            }
            if (percent >= 100){
                mUploadCallback.onSuccess(mUploaderTask);
            }
        } catch (InterruptedException e) {
            mUploadCallback.onError(mUploaderTask,e.toString());
        }
    }

    private void notifyThroughDatabase(int mFinalStatus) {
        mUploadCallback.uploadEnd(mUploaderTask,mFinalStatus);
    }

    private void checkPause() throws  StopRequest{
        synchronized (mUploaderTask) {
            if (mUploaderTask.status == UploaderTask.RUN_STATUS_PAUSE) {
                throw new StopRequest(UploaderTask.RUN_STATUS_PAUSE, "download paused by owner");
            }
        }
    }

    private class StopRequest extends Throwable {
        public int mFinalStatus;

        public StopRequest(int finalStatus, String message) {
            super(message);
            mFinalStatus = finalStatus;
        }

        public StopRequest(int finalStatus, String message, Throwable throwable) {
            super(message, throwable);
            mFinalStatus = finalStatus;
        }
    }

}
