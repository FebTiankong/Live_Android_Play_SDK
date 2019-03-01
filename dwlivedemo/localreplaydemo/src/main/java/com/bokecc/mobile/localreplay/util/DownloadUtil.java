package com.bokecc.mobile.localreplay.util;

import java.io.File;

/**
 * Created by liufh on 2017/2/17.
 */

public class DownloadUtil {

    //------------------------下载状态信息------------------------
    public final static int WAIT = 100;
    public final static int DOWNLOAD = 200;
    public final static int PAUSE = 300;
    public final static int FINISH = 400;
    public final static int WRONG = 500;

    //------------------------解压状态状态信息------------------------
    public final static int ZIP_WAIT= 10;
    public final static int ZIP_ING = 11;
    public final static int ZIP_FINISH = 12;
    public final static int ZIP_ERROR = 13;


    /**
     * 删除文件夹和文件夹下的所有文件
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if(file.isDirectory()){

            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }

            file.delete();
        }
    }

    public static String getUnzipDir(File oriFile) {

        String fileName = oriFile.getName();

        StringBuilder sb = new StringBuilder();
        sb.append(oriFile.getParent());
        sb.append("/");
        int index = fileName.indexOf(".");
        if (index == -1) {
            sb.append(fileName);
        } else {
            sb.append(fileName.substring(0, index));
        }

        return sb.toString();
    }

}
