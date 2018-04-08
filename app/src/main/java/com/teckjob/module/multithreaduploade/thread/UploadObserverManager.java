package com.teckjob.module.multithreaduploade.thread;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by IMBV on 2018/3/24.
 */

public class UploadObserverManager {
    private static final int REGISTER_OBSERVER = 3;
    private static final int UNREGISTER_OBSERVER = 4;
    private static final int UNREGISTER_ALL_OBSERVER = 5;
    private static final int DELETE_TASK = 8;
    private static final int UPDATE_TASK = 9;

    private SparseArray<ListenerItem> mItemCache;
    private HandlerThread handlerThread;
    private Handler mHandler;
    private Context mContext;

    public UploadObserverManager(Context context) {
        mItemCache = new SparseArray<ListenerItem>();
        handlerThread = new HandlerThread("uploader_observer");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(),mHandlerCallback);
        mContext = context;
    }

    private static class ListenerItem {
        UploaderTask mUploadTask;
        LinkedList<UploadObserver> mListeners;

        ListenerItem() {
            mListeners = new LinkedList<UploadObserver>();
        }

        void removeByTag(int tag) {
            Iterator<UploadObserver> iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                UploadObserver observer = iterator.next();
                if (observer.tag == tag) {
                    iterator.remove();
                }
            }
        }
    }

    public abstract static class UploadObserver {
        public int tag;
        abstract public void onChanged(long id, UploaderTask task);
    }

    public void registerObserver(int id, UploadObserver listener) {
        mHandler.sendMessage(mHandler.obtainMessage(REGISTER_OBSERVER, 0, id, listener));
    }

    public void unregisterObserver(int id, UploadObserver listener) {
        mHandler.sendMessage(mHandler.obtainMessage(UNREGISTER_OBSERVER, 0, id, listener));
    }

    public void unregisterObserver(int tag) {
        mHandler.sendMessage(mHandler.obtainMessage(UNREGISTER_ALL_OBSERVER, tag, 0, null));
    }

    private ListenerItem getCacheItem(int id) {
        return mItemCache.get(id);
    }

    private void putCacheItem(int id, ListenerItem item) {
        mItemCache.put(id, item);

    }

    private void doRegisterObserver(int id, UploadObserver listener) {
        ListenerItem cacheItem = getCacheItem(id);
        if (cacheItem != null){
            if (!cacheItem.mListeners.contains(listener)){
                cacheItem.mListeners.add(listener);
                listener.onChanged(id, cacheItem.mUploadTask);
            }
        } else {
            UploaderTask item = new UploaderTask(id);
            if (item != null){
                cacheItem = new ListenerItem();
                cacheItem.mUploadTask = item;
                cacheItem.mListeners.add(listener);
                listener.onChanged(id, item);
                putCacheItem(id, cacheItem);
            } else {
                listener.onChanged(id, null);
            }
        }
    }

    private void doUnregisterObserver(int id, UploadObserver listener) {
        ListenerItem cacheItem = getCacheItem(id);
        if (cacheItem != null){
            cacheItem.mListeners.remove(listener);
            if (cacheItem.mListeners.size() == 0) {
                mItemCache.remove(id);
            }
        }
    }

    private void doUnregisterObserver(int tag) {
        for (int i = 0; i < mItemCache.size(); i ++) {
            ListenerItem cacheItem = mItemCache.valueAt(i);
            cacheItem.removeByTag(tag);
            if (cacheItem.mListeners.size() == 0){
                mItemCache.removeAt(i);
                i --;
            }
        }
    }

    private void dispatchItemChanged(ListenerItem item,  boolean delete ) {
        for (UploadObserver listener : item.mListeners) {
            listener.onChanged(item.mUploadTask.handle, delete ? null : item.mUploadTask);
        }
    }

    public void delete(int id) {
        delete(new int[]{id});
    }

    public void delete(int[] ids) {
        mHandler.sendMessage(mHandler.obtainMessage(DELETE_TASK, ids));
    }

    private void doDeleteTask(int[] ids) {
        for (int _id : ids) {
            ListenerItem cacheItem = getCacheItem(_id);
            if (cacheItem != null){
                mItemCache.remove(_id);
                dispatchItemChanged(cacheItem, true);
            }
        }
    }

    public void update(UploaderTask task) {
        mHandler.sendMessage(mHandler.obtainMessage(UPDATE_TASK, 0, 0, task));
    }

    public void update(int id, HashMap<String,String> map) {
        mHandler.sendMessage(mHandler.obtainMessage(UPDATE_TASK, id, 0, map));
    }

    private void doUpdateTask(UploaderTask task) {
        ListenerItem cacheItem = getCacheItem((int)task.handle);
        if (cacheItem != null){
            cacheItem.mUploadTask = task;
            dispatchItemChanged(cacheItem, false);
        }
    }

    private void doUpdateTask(int id,HashMap<String,Object> map) {
        ListenerItem cacheItem = getCacheItem((int)id);
        if (cacheItem != null){
            if (cacheItem.mUploadTask.update(map)) {
                dispatchItemChanged(cacheItem, false);
            }
        }
    }

    private Handler.Callback mHandlerCallback = new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == REGISTER_OBSERVER) {
                doRegisterObserver(msg.arg2, (UploadObserver) msg.obj);
            } else if (msg.what == UNREGISTER_OBSERVER) {
                doUnregisterObserver(msg.arg2, (UploadObserver) msg.obj);
            } else if (msg.what == UNREGISTER_ALL_OBSERVER) {
                doUnregisterObserver(msg.arg1);
            } else if (msg.what == DELETE_TASK) {
                doDeleteTask((int[]) msg.obj);
            } else if (msg.what == UPDATE_TASK) {
                if (msg.obj instanceof UploaderTask) {
                    doUpdateTask((UploaderTask) msg.obj);
                }else {
                    doUpdateTask(msg.arg1, (HashMap<String, Object>) msg.obj);
                }
            }
            return true;
        }
    };
}
