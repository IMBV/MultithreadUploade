package com.teckjob.module.multithreaduploade.thread;

import android.media.MediaActionSound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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


    public boolean update(HashMap<String,Object> map){
        if (map != null){
            boolean isChange = false;
            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Object> it = iterator.next();
                if ("progress".equals(it.getKey())){
                    progress = (double) it.getValue();
                    isChange = true;
                }
                if ("status".equals(it.getKey())) {
                    status = (int) it.getValue();
                    isChange = true;
                }
            }
            return isChange;
        }
        return false;
    }
    //可以添加优先级level
}
