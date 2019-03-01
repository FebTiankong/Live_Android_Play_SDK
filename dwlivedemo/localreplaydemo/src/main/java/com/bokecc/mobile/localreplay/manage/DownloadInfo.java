package com.bokecc.mobile.localreplay.manage;

/**
 * Created by liufh on 2017/2/15.
 */

public class DownloadInfo {
    private String downloadUrl;
    private String fileName;
    private long start;
    private long end;
    private int status;


    public String getDownloadUrl() {
        return downloadUrl;
    }

    public DownloadInfo setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public DownloadInfo setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getStart() {
        return start;
    }

    public DownloadInfo setStart(long start) {
        this.start = start;
        return this;
    }

    public long getEnd() {
        return end;
    }

    public DownloadInfo setEnd(long end) {
        this.end = end;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public DownloadInfo setStatus(int status) {
        this.status = status;
        return this;
    }
}
