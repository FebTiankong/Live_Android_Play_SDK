package com.bokecc.mobile.localreplay;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bokecc.mobile.localreplay.manage.DownloaderWrapper;
import com.bokecc.mobile.localreplay.util.DataSet;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liufh on 2017/2/15.
 */

public class DownloadService extends Service {

    TimerTask timerTask;
    Timer timer = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();

        if (timerTask != null) {
            timerTask.cancel();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {

                DataSet.checkNewDownload(null);

                DataSet.checkNewUnzip();

                Intent intent = new Intent("com.service.action");
                sendBroadcast(intent);
            }
        };

        timer.schedule(timerTask, 1 * 1000, 1 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {}
}
