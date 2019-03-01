package com.bokecc.mobile.localreplay.base.contract;

import android.content.Intent;
import android.os.Bundle;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public interface BaseContract {

    interface View {

        void onBindPresenter();

        /**
         * 显示吐司提示在UI线程
         *
         * @param msg 提示内容
         */
        void toastOnUiThread(String msg);

        /**
         * 跳转activity
         */
        void go(Class clazz);

        /**
         * 跳转activity带参数
         */
        void go(Class clazz, Bundle bundle);

        /**
         * 跳转activity带参数
         */
        void goForResult(Class clazz, int requestCode);

        /**
         * 跳转activity带参数
         */
        void goForResult(Class clazz, int requestCode, Bundle bundle);

        void finishSelf();

        void finishWithData(int resultCode, Intent data);
    }

    interface Presenter {
    }

}

