package com.bokecc.mobile.localreplay.global;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class Config {
    private Config() {
        throw new UnsupportedOperationException();
    }

    public static final String SP_NAME = "com_bokecc_live_sp";

    // 本地保存设置信息的key和默认value
    public static final String SETTING_ORIENTATION = "com_bokecc_setting_orientation";
    public static final String SETTING_BEAUTY = "com_bokecc_setting_beauty";
    public static final String SETTING_CAMERA = "com_bokecc_setting_camera";
    public static final String SETTING_RESOLUTION = "com_bokecc_setting_resolution";
    public static final String SETTING_FPS = "com_bokecc_setting_fps";
    public static final String SETTING_BITRATE = "com_bokecc_setting_bitrate";
    public static final int SETTING_MIN_FPS = 10;
    public static final int SETTING_MAX_FPS = 30;
    public static final int SETTING_DEFAULT_FPS = 15;
    public static final int SETTING_MIN_BITRATE = 300;
    public static final int SETTING_DEFAULT_BITRATE = 400;

    public static final int SETTING_REQUEST_CODE = 100;
    public static final int SELECT_RESULT_CODE = 200;
    public static final int SEEK_RESULT_CODE = 300;

    public static final String SELECT_TYPE = "com_bokecc_select_type";
    public static final int SELECT_TYPE_CAMERA = 0;
    public static final int SELECT_TYPE_RESOLUTION = 1;
    public static final int SELECT_TYPE_SERVER = 2;
    public static final String SELECT_POSITION = "com_bokecc_select_position";
    public static final int SELECT_POSITION_DEFAULT = 0;

    public static final String SEEK_TYPE = "com_bokecc_seek_type";
    public static final String SEEK_PROGRESS = "com_bokecc_seek_progress";
    public static final int SEEK_TYPE_FPS = 0;
    public static final int SEEK_TYPE_BITRATE = 1;

    public static final String SEEK_MIN = "com_bokecc_seek_min"; // 最小
    public static final String SEEK_MAX = "com_bokecc_seek_max"; // 最大
    public static final String SEEK_DEFAULT = "com_bokecc_seek_default"; // 默认

    public static final String PUSH_FPS = "cc_bokecc_push_fps";
    public static final String PUSH_BITRATE = "cc_bokecc_push_bitrate";
    public static final String PUSH_CAMERA = "cc_bokecc_push_camera";
    public static final String PUSH_BEAUTY = "cc_bokecc_push_beauty";
    public static final String PUSH_SERVER = "cc_bokecc_push_server";
    public static final String PUSH_ORIENTATION = "cc_bokecc_push_orientation";
    public static final String PUSH_RESOLUTION = "cc_bokecc_push_resolution";

}
