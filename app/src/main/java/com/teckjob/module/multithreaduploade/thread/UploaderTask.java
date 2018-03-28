package com.teckjob.module.multithreaduploade.thread;

/**
 * Created by IMBV on 2018/3/23.
 */

public class UploaderTask {
    public static final int RUN_STATUS_PENDING = 0;
    public static final int RUN_STATUS_RUNING = 1;
    public static final int RUN_STATUS_PAUSE = 2;
    public static final int RUN_STATUS_SUCCESS = 3;
    public static final int RUN_STATUS_CANCLE = 4;


    public UploaderTask(long handle) {
        this.handle = handle;
    }

    public long handle = -1;//任务的标示
    public volatile boolean isRuning = false;
    public volatile boolean isCancelled = false;
    public int status = RUN_STATUS_PENDING;//运行状态

    public double progress;

    //可以添加优先级level

}
