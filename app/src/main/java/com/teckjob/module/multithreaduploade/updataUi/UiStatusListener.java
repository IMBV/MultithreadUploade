package com.teckjob.module.multithreaduploade.updataUi;

/**
 * Created by IMBV on 2018/3/24.
 */

public interface UiStatusListener<T> {
        void onUiStatusChange(UiChangeCache.UICacheInfo info, T t);
}
