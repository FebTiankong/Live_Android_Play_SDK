package com.bokecc.dwlivemoduledemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.dwlivemoduledemo.activity.extra.LivePlayDocActivity;
import com.bokecc.dwlivemoduledemo.activity.extra.ReplayPlayDocActivity;
import com.bokecc.dwlivemoduledemo.base.BaseActivity;
import com.bokecc.dwlivemoduledemo.popup.LoginPopupWindow;
import com.bokecc.dwlivemoduledemo.scan.qr_codescan.MipcaActivityCapture;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.login.BaseLoginFragment;
import com.bokecc.livemodule.login.LiveLoginFragment;
import com.bokecc.livemodule.login.LoginStatusListener;
import com.bokecc.livemodule.login.LoginType;
import com.bokecc.livemodule.login.ReplayLoginFragment;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录页面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, LoginStatusListener {

    private String[] listArray = new String[]{"观看直播", "观看回放"};

    View mRoot;
    RelativeLayout rlNaTitle;
    TextView tvNavTitle;

    LoginPopupWindow loginPopupWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        hideActionBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initFragments();
        initLoginStatusListener();
    }

    private void initViews() {
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.rl_na_title).setOnClickListener(this);
        findViewById(R.id.iv_scan).setOnClickListener(this);

        rlNaTitle = findViewById(R.id.rl_na_title);
        tvNavTitle = findViewById(R.id.tv_nav_title);

        loginPopupWindow = new LoginPopupWindow(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_scan:
                showScan();
                break;
        }
    }

    //—————————————————————————————————— 登录相关逻辑 ——————————————————————————————————————

    /**
     * 初始化登录状态监听
     */
    private void initLoginStatusListener() {
        DWLiveCoreHandler.getInstance().setLoginStatusListener(this);
        DWReplayCoreHandler.getInstance().setLoginStatusListener(this);
    }

    /**
     * 开始登录
     */
    @Override
    public void onStartLogin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    loginPopupWindow.show(mRoot);
                }
            }
        });
    }

    /**
     * 登录成功
     *
     * @param type 登录类型
     */
    @Override
    public void onLoginSuccess(final LoginType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissPopupWindow();
                if (type == LoginType.LIVE) {
                    toastOnUiThread("直播间登录成功");
                    go(LivePlayActivity.class); // 直播默认Demo页
                    // go(LivePlayDocActivity.class);    // 直播'文档大屏/视频小屏'的Demo页
                } else if (type == LoginType.REPLAY) {
                    toastOnUiThread("回放登录成功");
                    go(ReplayPlayActivity.class); // 回放默认Demo页
                    // go(ReplayPlayDocActivity.class);  // 回放'文档大屏/视频小屏'的Demo页
                }
            }
        });
    }

    /**
     * 登录失败
     *
     * @param reason 失败原因
     */
    @Override
    public void onLoginFailed(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissPopupWindow();
                toastOnUiThread(reason);
            }
        });
    }

    /**
     * 隐藏弹窗
     */
    private void dismissPopupWindow() {
        if (loginPopupWindow != null && loginPopupWindow.isShowing()) {
            loginPopupWindow.dismiss();
        }
    }

    //—————————————————————————————————— 扫码相关逻辑 ——————————————————————————————————————

    private static final int QR_REQUEST_CODE = 111;

    // 跳转到扫码页面
    private void showScan() {
        Intent intent = new Intent(this, MipcaActivityCapture.class);
        startActivityForResult(intent, QR_REQUEST_CODE);
    }

    // 接收并处理扫码页返回的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case QR_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    Log.e(LoginActivity.class.getSimpleName(), result);

                    if (!result.contains("userid=")) {
                        Toast.makeText(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, String> map = parseUrl(result);
                    if (map == null) {
                        return;
                    }
                    for (int i = 0; i < fragmentList.size(); i++) {
                        BaseLoginFragment fragment = fragmentList.get(i);
                        fragment.setLoginInfo(map);
                    }
                }
                break;
            default:
                break;
        }
    }

    //------------------------ Fragment 相关方法-----------------------------------------

    List<BaseLoginFragment> fragmentList = new ArrayList<>();

    private void initFragments() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        LiveLoginFragment liveFragment = new LiveLoginFragment();
        ReplayLoginFragment replayFragment = new ReplayLoginFragment();

        fragmentList.add(liveFragment);
        fragmentList.add(replayFragment);

        int fragmentIndex = getIntent().getIntExtra("fragmentIndex", 0);
        transaction.add(R.id.fl, fragmentList.get(fragmentIndex));
        transaction.commit();
        tvNavTitle.setText(listArray[fragmentIndex]);
    }

    //------------------------------------- 工具方法 -------------------------------------

    // 解析扫码获取到的URL
    private Map<String, String> parseUrl(String url) {
        Map<String, String> map = new HashMap<String, String>();
        String param = url.substring(url.indexOf("?") + 1, url.length());
        String[] params = param.split("&");

        if (params.length < 2) {
            return null;
        }
        for (String p : params) {
            String[] en = p.split("=");
            map.put(en[0], en[1]);
        }
        return map;
    }

}
