package com.teckjob.module.multithreaduploade.updataUi;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.LongSparseArray;

import com.teckjob.module.multithreaduploade.thread.UploadObserverManager;
import com.teckjob.module.multithreaduploade.thread.UploaderManager;
import com.teckjob.module.multithreaduploade.thread.UploaderTask;

import java.lang.ref.WeakReference;

/**
 * Created by IMBV on 2018/3/24.
 */

public class UiChangeCache<UpdateInfo> {

    private LongSparseArray<UICacheInfo> mCacheArray;

    private UploaderManager mUploaderManager;
    private MyUploadObserver mUploadObserver;

    private UiStatusListener<UpdateInfo> mStatusListener;

    private Context mContext;
    private int mObserverTag;

    private Handler mHandler;

    public UiChangeCache(Context mContext,UiStatusListener<UpdateInfo> mStatusListener) {
        this.mStatusListener = mStatusListener;
        this.mContext = mContext;
        this.mObserverTag = 10001;
        mCacheArray = new LongSparseArray<>();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private class MyUploadObserver extends UploadObserverManager.UploadObserver{

        public MyUploadObserver(int tag){
            this.tag = tag;
        }

        @Override
        public void onChanged(final long id, final UploaderTask task) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UICacheInfo info = mCacheArray.get(id);
                    if (info != null){
                        if (task != null){
                            info.status = task.status;
                            info.progress = task.progress;
                        } else {
                            info.status = UploaderTask.RUN_STATUS_CANCLE;
                        }

                        if (info.status == UploaderTask.RUN_STATUS_CANCLE) {
                            mUploaderManager.unregisterObserver((int) id, mUploadObserver);
                        }
                        if (mStatusListener != null) {
                            mStatusListener.onUiStatusChange(info, info.getUpdateInfo());
                        }
                    }
                }
            });
        }
    }

    private void checkInitUploadInfo() {
        if(mUploaderManager == null){
            mUploaderManager = UploaderManager.getInstance(mContext);
        }
        if(mUploadObserver == null){
            mUploadObserver = new MyUploadObserver(mObserverTag);
        }
    }

    public UICacheInfo getUploadCacheInfo(long position, UpdateInfo updateInfo) {
        if (position < 0) {
            return null;
        }
        UICacheInfo info = mCacheArray.get(position);
        if (info != null && (info.status == UploaderTask.RUN_STATUS_CANCLE
                || info.status == UploaderTask.RUN_STATUS_SUCCESS)) {
            return info;
        }
        if (info == null) {
            checkInitUploadInfo();
            info = new UICacheInfo(updateInfo);
            info.transferId = position;
            mUploaderManager.registerObserver((int) position, mUploadObserver);
            mCacheArray.put(position, info);
        } else {
            info.setUpdateInfo(updateInfo);
        }

        return info.status < 0 ? null : info;
    }


    public class UICacheInfo{
        public int status = -1;
        public double progress;
        public long transferId;
        public WeakReference<UpdateInfo> mUpdateInfo;

        UICacheInfo(UpdateInfo updateInfo) {
            mUpdateInfo = new WeakReference<>(updateInfo);
        }

        public void setUpdateInfo(UpdateInfo updateInfo) {
            mUpdateInfo = new WeakReference<>(updateInfo);
        }

        public UpdateInfo getUpdateInfo() {
            return mUpdateInfo.get();
        }

        public int getIntProgress(){
            return (int) progress;
        }
    }

}
