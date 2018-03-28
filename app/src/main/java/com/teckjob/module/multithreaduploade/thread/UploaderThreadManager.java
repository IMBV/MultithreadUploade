package com.teckjob.module.multithreaduploade.thread;

import android.content.Context;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IMBV on 2018/3/23.
 */

public class UploaderThreadManager {
    private List<UploaderTask> runList;
    private List<UploaderTask> waitList;

    private int maxThread;
    private Context mContext;
    private UploadCallback mUploadCallback;

    public UploaderThreadManager(int maxThread, Context mContext, UploadCallback mUploadCallback) {
        runList = Collections
                .synchronizedList(new LinkedList<UploaderTask>());
        waitList = Collections
                .synchronizedList(new LinkedList<UploaderTask>());
        this.maxThread = maxThread;
        this.mContext = mContext;
        this.mUploadCallback = mUploadCallback;
    }

    private boolean startIfReady(UploaderTask task, UploadCallback cb) {
        if (task.isRuning) {
            return false;
        }
        task.status = UploaderTask.RUN_STATUS_RUNING;
        task.isRuning = true;

        new UploadThread(mContext, task, cb);
        return true;
    }

    private void download(UploaderTask task) {
        if(startIfReady(task, mUploadCallback)) {
            runList.add(task);
        } else {
            reschdule();
        }
    }

    public void addTask(UploaderTask task) {
        if (runList.contains(task) || waitList.contains(task)) {
            return;
        }
        // some high level task
        waitList.add(task);
        reschdule();
    }

    public UploaderTask pauseTask(long handle) {
        for (UploaderTask task : runList) {
            if (task.handle == handle) {
                task.isCancelled = true;
                return task;
            }
        }

        for (UploaderTask task : waitList) {
            if (task.handle == handle) {
                waitList.remove(task);
                return null;
            }
        }

        return null;
    }

    public UploaderTask cancelTask(long handle){
        for (UploaderTask task : runList) {
            if (task.handle == handle) {
                task.isCancelled = true;
                return task;
            }
        }

        for (UploaderTask task : waitList) {
            if (task.handle == handle) {
                waitList.remove(task);
                return task;
            }
        }

        return null;
    }

    public boolean isQueue(long handle) {
        for (UploaderTask task : runList) {
            if (task.handle == handle) {
                return true;
            }
        }

        for (UploaderTask task : waitList) {
            if (task.handle == handle) {
                return true;
            }
        }
        return false;
    }

    private void reschdule() {
        if ( runList.size() >= maxThread) {
            return;
        }
        for (UploaderTask tmp : waitList) {
            if (runList.size() < maxThread) {
                waitList.remove(tmp);
                download(tmp);
                reschdule();
                return;
            }
        }
    }

    public void finishTask(UploaderTask task) {
        task.isRuning = false;
        runList.remove(task);
        reschdule();
    }

    public void removeAll() {
        waitList.clear();
        for (UploaderTask task : runList) {
            task.isCancelled = true;
        }
        runList.clear();
    }
}
