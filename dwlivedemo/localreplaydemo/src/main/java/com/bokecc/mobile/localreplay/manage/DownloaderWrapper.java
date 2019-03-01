package com.bokecc.mobile.localreplay.manage;

import android.util.Log;
import android.widget.Toast;

import com.bokecc.mobile.localreplay.util.DataSet;
import com.bokecc.mobile.localreplay.util.DownloadConfig;
import com.bokecc.mobile.localreplay.util.DownloadUtil;
import com.bokecc.mobile.localreplay.util.UnZiper;
import com.bokecc.mobile.localreplay.util.download.DownloadListener;
import com.bokecc.mobile.localreplay.util.download.Downloader;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;

import java.io.File;
import java.io.IOException;

/**
 * Created by liufh on 2017/2/15.
 */

public class DownloaderWrapper {
    Downloader downloader;
    UnZiper unZiper;
    DownloadInfo downloadInfo;

    long lastStart; // 用于计算下载速度
    long downloadSpeed;
    File downloadFile;

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
        createDownloadFile();

        // 初始化的状态判断
        if (downloadInfo.getStatus() == DownloadUtil.DOWNLOAD) {
            startDownload();
            return;
        }

        if (downloadInfo.getStatus() < DownloadUtil.ZIP_FINISH || downloadInfo.getStatus() == DownloadUtil.FINISH) {
            createUnziper();
            unZiper.setStatus(downloadInfo.getStatus());
            startUnzip();
        }
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void createDownloaderAndUnziper() {
        createDownloadFile();

        if (downloader == null) {
            downloader = new Downloader(new DownloadListener() {
                @Override
                public void handleVideoLength(long videoLength, String tag) {

                    downloadInfo.setEnd(videoLength);
                    DataSet.updateDownloadInfo2db(downloadInfo, DownloadUtil.WAIT);
                }

                @Override
                public void handleProcess(long start, long end, String videoId) {}

                @Override
                public void handleException(DWLiveException exception, int status) {}

                @Override
                public void handleStatus(String tag, int status) {
                    if (status == Downloader.FINISH) {
                        downloadInfo.setStart(downloadInfo.getEnd())
                                .setStatus(DownloadUtil.FINISH);
                        DataSet.updateDownloadInfo2db(downloadInfo);
                    }
                }

                @Override
                public void handleCancel(String tag) {}
            }, downloadFile, downloadInfo.getDownloadUrl(), downloadInfo.getFileName());

            if (downloadInfo.getEnd() > 0) {
                downloader.setEnd(downloadInfo.getEnd());
            }
        }

        createUnziper();
    }

    public void createUnziper() {
        if (unZiper == null) {
//            String name = downloadFile.getName();
//            File unzipDir = new File(downloadFile.getParentFile(), name.substring(0, name.indexOf(".")));
            String dir = DownloadUtil.getUnzipDir(downloadFile);
            unZiper = new UnZiper(new UnZiper.UnZipListener() {

                @Override
                public void onError(int errorCode, String message) {
                    Log.e("111", "解压失败，错误码 = " + errorCode + ", 错误内容" + message);
                }

                @Override
                public void onUnZipFinish() {
                    downloadInfo.setStatus(DownloadUtil.ZIP_FINISH);
                    unZiper.setStatus(DownloadUtil.ZIP_FINISH);
                    DataSet.updateDownloadInfo2db(downloadInfo);
                }
            }, downloadFile, dir);
        }
    }

    private void createDownloadFile() {
        if (downloadFile == null) {
            downloadFile = new File(DownloadConfig.DOWNLOAD_DIR, downloadInfo.getFileName());
        }
    }

    /**
     * 重置下载状态，用于解压失败的情况下
     */
    public void resetDownloadStatus() {
        downloadFile.delete();

        if (downloader == null) {
            createDownloaderAndUnziper();
        }

        downloadInfo.setStart(0);
        downloadInfo.setStatus(DownloadUtil.WAIT);
        DataSet.updateDownloadInfo2db(downloadInfo);
    }

    public void startDownload() {
        if (downloader == null) {
            createDownloaderAndUnziper();
        }
        downloader.start();
    }

    public void pauseDownload() {
        if (downloader != null) {
            downloader.pause();
        }
    }

    public void deleteDownload() {
        if (downloader != null) {
            downloader.cancel();
        }

        createDownloadFile();

        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFile.delete();
//                String name = downloadFile.getName();
//                File dir = new File(downloadFile.getParentFile(), name.substring(0, name.indexOf(".")));
                DownloadUtil.delete(new File(DownloadUtil.getUnzipDir(downloadFile)));
            }
        }).start();

    }

    public void update() {
        if (downloadInfo.getStatus() == DownloadUtil.ZIP_FINISH) {
            return;
        }

        if (downloader != null) {
            if (downloader.getStatus() == DownloadUtil.FINISH) {
                downloadInfo.setStatus(unZiper.getStatus())
                        .setStart(downloader.getEnd())
                        .setEnd(downloader.getEnd());
            } else {
                downloadInfo.setStatus(downloader.getStatus());

                long start = downloader.getStart();
                downloadSpeed = start - lastStart;
                if (downloadSpeed < 0) {
                    downloadSpeed = 0;
                }
                downloadInfo.setStart(start);
                lastStart = start;

                downloadInfo.setEnd(downloader.getEnd());
            }

        } else {
            if (unZiper == null) {
                return;
            }
            downloadInfo.setStatus(unZiper.getStatus());
        }
    }

    public void setStatus(int status) {
        downloadInfo.setStatus(status);
        if (downloader == null) {
            createDownloaderAndUnziper();
        }

        if (status < DownloadUtil.WAIT) {
            downloader.setStatus(DownloadUtil.FINISH);
            unZiper.setStatus(status);
        } else {
            downloader.setStatus(status);
            unZiper.setStatus(DownloadUtil.ZIP_WAIT);
        }

    }

    public long getStart() {
        return downloadInfo.getStart();
    }

    public long getEnd() {
        return downloadInfo.getEnd();
    }

    public int getStatus() {
        return downloadInfo.getStatus();
    }

    public String getFileName() {
        return downloadInfo.getFileName();
    }

    public String getDownloadUrl() {
        return downloadInfo.getDownloadUrl();
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void startUnzip() {
        if (unZiper == null) {
            createUnziper();
        }

        if (downloadFile.exists()) {
            unZiper.unZipFile();
        } else {
            resetDownloadStatus();
        }
    }

}
