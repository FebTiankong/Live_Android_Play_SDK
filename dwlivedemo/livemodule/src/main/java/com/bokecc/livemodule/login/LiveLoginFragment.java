package com.bokecc.livemodule.login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLiveLoginListener;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.LoginInfo;
import com.bokecc.sdk.mobile.live.pojo.PublishInfo;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.pojo.Viewer;

import java.util.Map;

/**
 * 直播观看登录页
 */
public class LiveLoginFragment extends BaseLoginFragment {

    private static final String TAG = "LiveLoginFragment";

    SharedPreferences preferences;

    Button btnLoginLive;
    LoginLineLayout lllLoginLiveUid;
    LoginLineLayout lllLoginLiveRoomid;
    LoginLineLayout lllLoginLiveName;
    LoginLineLayout lllLoginLivePassword;

    private void initViews(View rootView) {
        btnLoginLive = rootView.findViewById(R.id.btn_login_live);
        lllLoginLiveUid = rootView.findViewById(R.id.lll_login_live_uid);
        lllLoginLiveRoomid = rootView.findViewById(R.id.lll_login_live_roomid);
        lllLoginLiveName = rootView.findViewById(R.id.lll_login_live_name);
        lllLoginLivePassword = rootView.findViewById(R.id.lll_login_live_password);
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
            boolean isLoginEnabled = isNewLoginButtonEnabled(lllLoginLiveName, lllLoginLiveRoomid, lllLoginLiveUid);
            btnLoginLive.setEnabled(isLoginEnabled);
            btnLoginLive.setTextColor(isLoginEnabled ? Color.parseColor("#ffffff") : Color.parseColor("#f7d8c8"));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_live, container, false);
        initViews(view);
        lllLoginLiveUid.setHint(getResources().getString(R.string.login_uid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginLiveRoomid.setHint(getResources().getString(R.string.login_roomid_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginLiveName.setHint(getResources().getString(R.string.login_name_hint)).addOnTextChangeListener(myTextWatcher);
        lllLoginLiveName.maxEditTextLength = nameMax;
        lllLoginLivePassword.setHint(getResources().getString(R.string.login_s_password_hint)).addOnTextChangeListener(myTextWatcher)
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
            lllLoginLiveRoomid.setText(map.get(roomIdStr));
        }

        if (map.containsKey(userIdStr)) {
            lllLoginLiveUid.setText(map.get(userIdStr));
        }
    }

    @Override
    public void setLoginInfo(Map<String, String> map) {
        this.map = map;
        if (lllLoginLiveUid != null) {
            initEditTextInfo();
        }
    }

    //————————————————————————————————————登录相关方法—————————————————————————————————————————

    /**
     * 执行直播登录操作
     */
    private void doLiveLogin() {
        final LoginStatusListener listener = DWLiveCoreHandler.getInstance().getLoginStatusListener();
        // 回调登录开始状态
        if (listener != null) {
            listener.onStartLogin();
        }
        // 创建登录信息
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setRoomId(lllLoginLiveRoomid.getText());
        loginInfo.setUserId(lllLoginLiveUid.getText());
        loginInfo.setViewerName(lllLoginLiveName.getText());
        loginInfo.setViewerToken(lllLoginLivePassword.getText());

        // 设置登录参数
        DWLive.getInstance().setDWLiveLoginParams(new DWLiveLoginListener() {
            @Override
            public void onLogin(TemplateInfo templateInfo, Viewer viewer, final RoomInfo roomInfo, PublishInfo publishInfo) {
                if (templateInfo != null) {
                    Log.i(TAG, "DWLive Login Success，templateInfo type = " + templateInfo.getType());
                }
                // 缓存登陆的参数
                writeSharePreference();
                // 回调登录成功
                if (listener != null) {
                    listener.onLoginSuccess(LoginType.LIVE);
                }
            }

            @Override
            public void onException(final DWLiveException e) {
                Log.e(TAG, "DWLive Login Failed");
                // 回调登录失败
                if (listener != null) {
                    listener.onLoginFailed(e.getLocalizedMessage());
                }
            }
        }, loginInfo);

        // 执行登录操作
        DWLive.getInstance().startLogin();
    }

    //------------------------------- 缓存数据相关方法-----------------------------------------

    private void writeSharePreference() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("liveuid", lllLoginLiveUid.getText());
        editor.putString("liveroomid", lllLoginLiveRoomid.getText());
        editor.putString("liveusername", lllLoginLiveName.getText());
        editor.putString("livepassword", lllLoginLivePassword.getText());
        editor.commit();
    }

    private void getSharePrefernce() {
        lllLoginLiveUid.setText(preferences.getString("liveuid", ""));
        lllLoginLiveRoomid.setText(preferences.getString("liveroomid", ""));
        lllLoginLiveName.setText(preferences.getString("liveusername", ""));
        lllLoginLivePassword.setText(preferences.getString("livepassword", ""));
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
