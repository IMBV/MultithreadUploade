package com.teckjob.module.multithreaduploade.thread;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;


/**
 * Created by IMBV on 2018/3/23.
 */

public class UploaderManager {

    private static final int WHAT = 1;

    private static final int CMD_NEW = 0;
    private static final int CMD_PAUSE = 1;
    private static final int CMD_RESUME = 2;
    private static final int CMD_DELETE = 3;

    //定义一个线程安全的单例
    private static UploaderManager inst;
    private Context mContext;
    private UploaderThreadManager mUploaderThreadManager;
    private UploadObserverManager mUploadObserverManager;

    private HandlerThread handlerThread;
    private Handler mHandler;

    public synchronized static UploaderManager getInstance(Context context){
        if (inst == null){
            inst = new UploaderManager(context);
        }
        return inst;
    }

    public UploaderManager(Context mContext) {
        this.mContext = mContext;
        initialize();
    }

    private void initialize() {
        handlerThread = new HandlerThread("upload_workerThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(),new MyHandlerCallback());

        //建立上传管理者
        mUploaderThreadManager = new UploaderThreadManager(3,mContext,mUploadCallback);
        mUploadObserverManager = new UploadObserverManager(mContext);
    }

    public boolean upload(UploaderTask task) {
        sendMessage(CMD_NEW , task);
        return true;
    }

    private UploadCallback mUploadCallback = new UploadCallback(){

        @Override
        public void onSuccess(UploaderTask task) {
            task.progress = 100;
            task.status = UploaderTask.RUN_STATUS_SUCCESS;
            mUploadObserverManager.update(task);
            mUploaderThreadManager.finishTask(task);
        }

        @Override
        public void onProgress(final UploaderTask task, final double proress) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    task.progress = proress;
                    mUploadObserverManager.update(task);
                }
            });
        }

        @Override
        public void onError(UploaderTask task, String hint) {
            mUploadObserverManager.update(task);
            mUploaderThreadManager.finishTask(task);
        }
    };

    private void sendMessage(int arg1, Object obj) {
        Message msg = mHandler.obtainMessage();
        msg.what = WHAT;
        msg.obj = obj;
        msg.arg1 = arg1;
        mHandler.sendMessage(msg);
    }

    private class MyHandlerCallback implements android.os.Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (WHAT == msg.what){
                switch (msg.arg1){
                    case CMD_NEW:
                        mUploaderThreadManager.addTask((UploaderTask) msg.obj);
                        break;
                }
            }
            return false;
        }
    }

    //ui  change
    public void registerObserver(int id, UploadObserverManager.UploadObserver listener) {
        mUploadObserverManager.registerObserver(id, listener);
    }

    public void unregisterObserver(int id, UploadObserverManager.UploadObserver listener) {
        mUploadObserverManager.unregisterObserver(id, listener);
    }

    public void unregisterObserver(int tag) {
        mUploadObserverManager.unregisterObserver(tag);
    }

}
