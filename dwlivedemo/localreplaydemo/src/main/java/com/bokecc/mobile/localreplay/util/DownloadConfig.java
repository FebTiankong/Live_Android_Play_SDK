package com.bokecc.mobile.localreplay.util;

import android.os.Environment;

/**
 * Created by liufh on 2017/2/16.
 */

public class DownloadConfig {

    public static int MULTI_TASK_MAX = 2;

    public static String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/CCDownload";
}
