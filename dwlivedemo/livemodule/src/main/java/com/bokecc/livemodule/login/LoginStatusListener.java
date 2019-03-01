package com.bokecc.livemodule.login;

/**
 * 登录状态监听接口
 */
public interface LoginStatusListener {

    /**
     * 开始登录
     */
    void onStartLogin();

    /**
     * 登录成功
     *
     * @param type 登录类型
     */
    void onLoginSuccess(LoginType type);

    /**
     * 登录失败
     *
     * @param reason 失败原因
     */
    void onLoginFailed(String reason);
}
