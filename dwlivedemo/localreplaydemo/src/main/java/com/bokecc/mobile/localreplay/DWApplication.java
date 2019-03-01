package com.bokecc.mobile.localreplay;

import android.app.Application;
import android.content.Context;

import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.tencent.bugly.crashreport.CrashReport;


/**
 * 作者 ${郭鹏飞}.<br/>
 */
public class DWApplication extends Application {

    public static final boolean REPLAY_CHAT_FOLLOW_TIME = true; // 是否让回放的聊天内容随时间轴推进展示

    public static final boolean REPLAY_QA_FOLLOW_TIME = true; // 是否让回放的问答内容随时间轴推进展示

    static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        if (context == null) {
            context = this;
        }

        CrashReport.initCrashReport(getApplicationContext(), "a662d046b0", true);
        // 初始化 CC LIVE SDK
        DWLiveEngine.init(this);
    }

    public static Context getContext() {
        return context;
    }

}
