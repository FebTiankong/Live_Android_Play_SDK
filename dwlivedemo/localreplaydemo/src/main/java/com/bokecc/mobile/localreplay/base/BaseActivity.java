package com.bokecc.mobile.localreplay.base;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;


import com.bokecc.mobile.localreplay.base.contract.BaseContract;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseContract.View {

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: 2016/12/13
//        if (DWApplication.mAppStatus == -1) { // 如果被强杀不执行初始化操作
//        } else {
            beforeSetContentView();
            setContentView(getLayoutId());
            mUnbinder = ButterKnife.bind(this);
            onBindPresenter();
            onViewCreated();
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
    }

    /**
     * 在SetContentView之前进行操作，父类空实现，子类根据需要进行实现
     */
    protected void beforeSetContentView() {}

    /**
     * 获取布局id
     */
    protected abstract int getLayoutId();

    /**
     * 界面创建完成
     */
    protected abstract void onViewCreated();

    /**
     * 进行吐司提示
     * @param msg 提示内容
     */
    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 判断当前的线程是否是UI线程
     */
    protected boolean checkOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    public void onBindPresenter() {
        // ignore 交由子类具体实现
    }

    @Override
    public void toastOnUiThread(final String msg) {
        // 判断是否处在UI线程
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(msg);
                }
            });
        } else {
            showToast(msg);
        }
    }

    @Override
    public void go(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    @Override
    public void go(Class clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void goForResult(Class clazz, int requestCode) {
        Intent intent = new Intent(this, clazz);
        startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void goForResult(Class clazz, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode, bundle);
    }

    @Override
    public void finishSelf() {
        finish();
    }

    @Override
    public void finishWithData(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }

}
