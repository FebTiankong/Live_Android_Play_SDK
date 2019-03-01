package com.bokecc.livemodule.login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayLoginListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLoginInfo;

import java.util.Map;

/**
 * 回放登录页
 */
public class ReplayLoginFragment extends BaseLoginFragment {

    SharedPreferences preferences;

    Button btnLoginLive;
    LoginLineLayout lllLoginReplayUid;
    LoginLineLayout lllLoginReplayRoomid;
    LoginLineLayout lllLoginReplayLiveid;
    LoginLineLayout lllLoginReplayRecordid;
    LoginLineLayout lllLoginReplayName;
    LoginLineLayout lllLoginReplayPassword;

    private void initViews(View rootView) {
        btnLoginLive = rootView.findViewById(R.id.btn_login_replay);
        lllLoginReplayUid = rootView.findViewById(R.id.lll_login_replay_uid);
        lllLoginReplayRoomid = rootView.findViewById(R.id.lll_login_replay_roomid);
        lllLoginReplayLiveid = rootView.findViewById(R.id.lll_login_replay_liveid);
        lllLoginReplayRecordid = rootView.findViewById(R.id.lll_login_replay_recordid);
        lllLoginReplayName = rootView.findViewById(R.id.lll_login_replay_name);
        lllLoginReplayPassword = rootView.findViewById(R.id.lll_login_replay_password);
        btnLoginLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLiveLogin();
            }
        });
    }

    private TextWatcher myTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean isLoginEnabled = isNewLoginButtonEnabled(lllLoginReplayUid, lllLoginReplayRoomid, lllLoginReplayLiveid, lllLoginReplayName);
            btnLoginLive.setEnabled(isLoginEnabled);
            btnLoginLive.setTextColor(isLoginEnabled ? Color.parseColor("#ffffff") : Color.parseColor("#f7d8c8"));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_replay, container, false);
        initViews(view);
        lllLoginReplayUid.setHint(getResources().getString(R.string.login_uid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayRoomid.setHint(getResources().getString(R.string.login_roomid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayLiveid.setHint(getResources().getString(R.string.login_liveid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayRecordid.setHint(getResources().getString(R.string.login_recordid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayName.setHint(getResources().getString(R.string.login_name_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginReplayName.maxEditTextLength = nameMax;
        lllLoginReplayPassword.setHint(getResources().getString(R.string.login_s_password_hint)).addOnTextChangeListener(myTextWatcher)
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        preferences = getActivity().getSharedPreferences("live_login_info", Activity.MODE_PRIVATE);
        getSharePrefernce();
        if (map != null) {
            initEditTextInfo();
        }
        return view;
    }

    Map<String, String> map;

    private void initEditTextInfo() {
        if (map.containsKey(roomIdStr)) {
            lllLoginReplayRoomid.setText(map.get(roomIdStr));
        }

        if (map.containsKey(userIdStr)) {
            lllLoginReplayUid.setText(map.get(userIdStr));
        }

        if (map.containsKey(liveIdStr)) {
            lllLoginReplayLiveid.setText(map.get(liveIdStr));
        }

        if (map.containsKey(recordIdStr)) {
            lllLoginReplayRecordid.setText(map.get(recordIdStr));
        }
    }

    @Override
    public void setLoginInfo(Map<String, String> map) {
        this.map = map;
        if (lllLoginReplayUid != null) {
            initEditTextInfo();
        }
    }

    //————————————————————————————————————登录相关方法—————————————————————————————————————————

    /**
     * 执行直播登录操作
     */
    private void doLiveLogin() {

        final LoginStatusListener listener = DWReplayCoreHandler.getInstance().getLoginStatusListener();
        // 回调登录开始状态
        if (listener != null) {
            listener.onStartLogin();
        }

        // 创建登录信息
        ReplayLoginInfo replayLoginInfo = new ReplayLoginInfo();
        replayLoginInfo.setUserId(lllLoginReplayUid.getText());
        replayLoginInfo.setRoomId(lllLoginReplayRoomid.getText());
        replayLoginInfo.setLiveId(lllLoginReplayLiveid.getText());
        replayLoginInfo.setRecordId(lllLoginReplayRecordid.getText());
        replayLoginInfo.setViewerName(lllLoginReplayName.getText());
        replayLoginInfo.setViewerToken(lllLoginReplayPassword.getText());

        // 设置登录参数
        DWLiveReplay.getInstance().setLoginParams(new DWLiveReplayLoginListener() {

            @Override
            public void onException(final DWLiveException exception) {
                if (listener != null) {
                    listener.onLoginFailed(exception.getMessage());
                }
            }

            @Override
            public void onLogin(TemplateInfo templateInfo) {
                writeSharePreference();
                if (listener != null) {
                    listener.onLoginSuccess(LoginType.REPLAY);
                }
            }
        }, replayLoginInfo);

        // 执行登录操作
        DWLiveReplay.getInstance().startLogin();
    }

    //------------------------------- 缓存数据相关方法-----------------------------------------

    private void writeSharePreference() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("replayuid", lllLoginReplayUid.getText());
        editor.putString("replayroomid", lllLoginReplayRoomid.getText());
        editor.putString("replayliveid", lllLoginReplayLiveid.getText());
        editor.putString("replayrecordid", lllLoginReplayRecordid.getText());
        editor.putString("replayusername", lllLoginReplayName.getText());
        editor.putString("replaypassword", lllLoginReplayPassword.getText());
        editor.commit();
    }

    private void getSharePrefernce() {
        lllLoginReplayUid.setText(preferences.getString("replayuid", ""));
        lllLoginReplayRoomid.setText(preferences.getString("replayroomid", ""));
        lllLoginReplayLiveid.setText(preferences.getString("replayliveid", ""));
        lllLoginReplayRecordid.setText(preferences.getString("replayrecordid", ""));
        lllLoginReplayName.setText(preferences.getString("replayusername", ""));
        lllLoginReplayPassword.setText(preferences.getString("replaypassword", ""));
    }

    //------------------------------- 工具方法-----------------------------------------

    // 检测登录按钮是否应该可用
    public static boolean isNewLoginButtonEnabled(LoginLineLayout... views) {
        for (int i = 0; i < views.length; i++) {
            if ("".equals(views[i].getText().trim())) {
                return false;
            }
        }
        return true;
    }
}