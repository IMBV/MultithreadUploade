package com.teckjob.module.multithreaduploade;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.teckjob.module.multithreaduploade.thread.UploaderManager;
import com.teckjob.module.multithreaduploade.thread.UploaderTask;
import com.teckjob.module.multithreaduploade.updataUi.UiChangeCache;
import com.teckjob.module.multithreaduploade.updataUi.UiStatusListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IMBV on 2018/3/24.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements UiStatusListener<Adapter.ViewHolder>{

    private Context mContext;

    private List<AdapterData> datas;

    public Adapter(Context context, ArrayList<AdapterData> datas) {
        this.mContext = context;
        this.datas = datas;
        uiChangeCache = new UiChangeCache(mContext,this);
    }
    private UiChangeCache uiChangeCache;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final AdapterData data = datas.get(position);
        holder.data = data;
        updataUI(holder,holder.data,null);
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer status = (Integer) holder.textView.getTag();
                if (status == null){
                    down(data,position);
                }else{
                    if (status == UploaderTask.RUN_STATUS_CANCLE){
                        down(data,position);
                    }else if (status == UploaderTask.RUN_STATUS_RUNING || status == UploaderTask.RUN_STATUS_PENDING){
                        UploaderManager.getInstance(mContext).doCommand(UploaderManager.CMD_PAUSE,position);
                    }else if (status == UploaderTask.RUN_STATUS_PAUSE){

                    }else if(status == UploaderTask.RUN_STATUS_SUCCESS){
                        Toast.makeText(mContext,"下载完成",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void down(AdapterData data,int position) {
        UploaderManager.getInstance(mContext).upload(new UploaderTask(position));
        data.transferId = position;
        Adapter.this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onUiStatusChange(UiChangeCache.UICacheInfo info, ViewHolder holder) {
        if (holder.data != null && holder.data.transferId == info.transferId) {
            updataUI(holder, holder.data, info);
        }
    }

    private void updataUI(ViewHolder holder,AdapterData data,UiChangeCache.UICacheInfo info){
        int progressBarVis = View.GONE;
        holder.textView.setTag(UploaderTask.RUN_STATUS_CANCLE);
        UiChangeCache.UICacheInfo centerStatusInfo = null;
        if (data.transferId >= 0){
            if (info == null){
                centerStatusInfo = uiChangeCache.getUploadCacheInfo(data.transferId,holder);
            }else {
                centerStatusInfo = info;
            }
            if (centerStatusInfo != null) {
                if (centerStatusInfo.status == UploaderTask.RUN_STATUS_CANCLE) {
                    data.transferId = -1;
                }
            }

        }
        if (data.transferId < 0){
            holder.textView.setText("下载");
        }else {
            if (centerStatusInfo == null) {
                holder.textView.setText("下载");
            } else {
                holder.textView.setTag(centerStatusInfo.status);
                if (centerStatusInfo.status == UploaderTask.RUN_STATUS_SUCCESS) {
                    holder.textView.setText("下载完成");
                } else if (centerStatusInfo.status == UploaderTask.RUN_STATUS_RUNING) {
                    progressBarVis = View.VISIBLE;
                    int progress = (int) centerStatusInfo.progress;
                    holder.textView.setText("下载中...");
                    holder.progress.setProgress(progress);
                } else if (centerStatusInfo.status == UploaderTask.RUN_STATUS_PAUSE) {
                    progressBarVis = View.VISIBLE;
                    int progress = (int) centerStatusInfo.progress;
                    holder.textView.setText("继续");
                    holder.progress.setProgress(progress);
                } else if (centerStatusInfo.status == UploaderTask.RUN_STATUS_PENDING) {
                    progressBarVis = View.VISIBLE;
                    int progress = (int) centerStatusInfo.progress;
                    holder.textView.setText("等待");
                    holder.progress.setProgress(progress);
                } else {
                    holder.textView.setText("下载");
                }
            }
        }
        if (progressBarVis != holder.progress.getVisibility()) {
            holder.progress.setVisibility(progressBarVis);
        }
    }



    static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;
        private ProgressBar progress;

        AdapterData data;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            progress = itemView.findViewById(R.id.progress);
        }
    }

}
