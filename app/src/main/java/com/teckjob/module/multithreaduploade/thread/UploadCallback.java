package com.teckjob.module.multithreaduploade.thread;

/**
 * Created by IMBV on 2018/3/23.
 */

public interface UploadCallback {
    void onSuccess(UploaderTask task);

    void onProgress(UploaderTask task,double proress);

    void onError(UploaderTask task,String hint);

}
