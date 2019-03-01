package com.bokecc.mobile.localreplay.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.manage.DownloaderWrapper;
import com.bokecc.mobile.localreplay.util.DataSet;
import com.bokecc.mobile.localreplay.util.DownloadUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by liufh on 2017/2/14.
 */

public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.DownloadViewHolder> {

    Context context;

    public DownloadListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.download_single_line, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DownloadViewHolder holder, int position) {

        ArrayList<DownloaderWrapper> list = new ArrayList<>(DataSet.getDownloadWrapperMap().values());

        DownloaderWrapper wrapper = list.get(position);

        holder.fileName.setText(wrapper.getFileName());

        long start = wrapper.getStart();
        long end = wrapper.getEnd();
        int status = wrapper.getStatus();
        int percent = 0;
        if (end > 0) {
            percent = (int)(start * 100 / end);
        }

        if (status == DownloadUtil.FINISH || status < DownloadUtil.WAIT) {
            start = end;
            percent = 100;
        }

        // 20MB/40MB(50%)
        String progressNumberic = Formatter.formatFileSize(context, start) + "/" + Formatter.formatFileSize(context, end) + "(" + percent + "%)";
        holder.downloadProgressNumberic.setText(progressNumberic);

        setDownloadProgressViewStyle(holder, status);

        holder.downloadProgressbar.setMax(100);
        if (status == DownloadUtil.FINISH || status < DownloadUtil.WAIT) {
            holder.downloadProgressbar.setProgress(100);
        } else {
            holder.downloadProgressbar.setProgress(percent);
        }

        //下载状态需要设置下载速度
        if (status == DownloadUtil.DOWNLOAD) {
            holder.downloadStatus.setText(parseStatus(wrapper.getStatus()) + "   " + Formatter.formatFileSize(context, wrapper.getDownloadSpeed()) + "/s");
        } else {
            holder.downloadStatus.setText(parseStatus(wrapper.getStatus()));
        }

    }

    private void setDownloadProgressViewStyle(DownloadViewHolder holder, int status) {
        switch(status) {
            case DownloadUtil.WAIT:
            case DownloadUtil.PAUSE:
                holder.downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_wait));
                holder.downloadProgressbar.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_finish_bg));
                break;
            case DownloadUtil.DOWNLOAD:
                holder.downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_ing));
                holder.downloadProgressbar.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_ing_bg));
                break;
            case DownloadUtil.WRONG:
                holder.downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_fail));
                holder.downloadProgressbar.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_fail_bg));
                break;
            default:
                holder.downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_success));
                holder.downloadProgressbar.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_finish_bg));
                break;
        }

    }

    private String parseStatus(int status) {
        switch(status) {
            case DownloadUtil.ZIP_WAIT:
                return "下载完成  等待解压";
            case DownloadUtil.ZIP_ING:
                return "下载完成  处理中";
            case DownloadUtil.ZIP_FINISH:
                return "下载完成  解压完成";
            case DownloadUtil.ZIP_ERROR:
                return "下载完成  解压失败";
            case DownloadUtil.WAIT:
                return "等待中";
            case DownloadUtil.DOWNLOAD:
                return "下载中";
            case DownloadUtil.PAUSE:
                return "暂停中";
            case DownloadUtil.FINISH:
                return "已完成";
            default:
                return "下载失败";
        }
    }

    @Override
    public int getItemCount() {
        return DataSet.getDownloadWrapperMap().size();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.id_file_name)
        public TextView fileName;

        @BindView(R.id.id_download_progress_numberic)
        TextView downloadProgressNumberic;

        @BindView(R.id.id_download_progressbar)
        ProgressBar downloadProgressbar;

        @BindView(R.id.id_download_icon)
        ImageView downloadIcon;

        @BindView(R.id.id_download_status)
        TextView downloadStatus;

        public DownloadViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
