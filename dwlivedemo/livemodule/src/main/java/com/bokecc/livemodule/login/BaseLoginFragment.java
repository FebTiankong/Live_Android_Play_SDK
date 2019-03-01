package com.bokecc.livemodule.login;

import android.content.Context;
import android.support.v4.app.Fragment;

import java.util.Map;

public abstract class BaseLoginFragment extends Fragment {

    String userIdStr = "userid";  // 用户id
    String roomIdStr = "roomid";  // 房间id
    String liveIdStr = "liveid";  // 直播id
    String recordIdStr = "recordid";  // 回放id 手动录制参数 @since SDK 2.2.2 版本添加

    int nameMax = 20;

    public abstract void setLoginInfo(Map<String, String> map);

    Context mContext;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
}