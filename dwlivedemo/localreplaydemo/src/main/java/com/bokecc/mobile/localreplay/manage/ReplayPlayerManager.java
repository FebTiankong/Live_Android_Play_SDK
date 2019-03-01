package com.bokecc.mobile.localreplay.manage;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.ReplayActivity;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by liufh on 2016/12/27.
 */

public class ReplayPlayerManager {

    @BindView(R.id.replay_title)
    TextView mTitle;

    @BindView(R.id.replay_play_control_bottom)
    LinearLayout playControlBottom;

    @BindView(R.id.replay_play_control_top)
    LinearLayout playControlTop;

    @BindView(R.id.replay_play_control_top_left)
    LinearLayout playControlTopLeftLayout;

    @BindView(R.id.replay_current_time)
    TextView currentTime;

    @BindView(R.id.replay_duration)
    TextView durationTextView;

    @BindView(R.id.replay_sign)
    TextView replaySign;

    @BindView(R.id.replay_back)
    ImageView back;

    @BindView(R.id.replay_play_icon)
    ImageView playIcon;

    @BindView(R.id.replay_progressbar)
    SeekBar playSeekBar;

    @BindView(R.id.replay_speed)
    Button playSpeed;

    @BindView(R.id.replay_full_screen)
    ImageView fullScreen;

    ReplayActivity mContext;

    View mRoot;

    public ReplayPlayerManager(ReplayActivity context, View parentView, View rootView) {
        this.mContext = context;
        ButterKnife.bind(this, parentView);
        mRoot = rootView;
        fullScreen.setSelected(true);
        playIcon.setSelected(true);

        playSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setScreenVisible(true, false);
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mContext.setSeekPosition(progress);
                postDelayScreenDisappear();
            }
        });

    }


    public void init() {
        mTitle.setText("回放");
    } // 这里需要改

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @OnClick({R.id.replay_back, R.id.replay_play_icon, R.id.replay_full_screen, R.id.replay_speed})
    public void onClick(View view) {
        postDelayScreenDisappear(); // 重置消失时间
        switch (view.getId()) {
            case R.id.replay_back:
                onBackClick();
                break;
            case R.id.replay_play_icon:
                changePlayerStatus();
                break;
            case R.id.replay_full_screen:
                changeScreenStatus();
                break;
            case R.id.replay_speed:
                changePlaySpeed();
                break;
        }
    }

    public void onBackClick() {
        mContext.onBackPressed();
    }

    public void changePlayerStatus() {
        if (playIcon.isSelected()) {
            playIcon.setSelected(false);
            mContext.setPlayerStatus(false);
        } else {
            playIcon.setSelected(true);
            mContext.setPlayerStatus(true);
        }
    }

    // 根据当前是否在播放，修改播放按钮的图标
    public void changePlayIconStatus(boolean isPlaying) {
        if (isPlaying) {
            playIcon.setSelected(true);
        } else {
            playIcon.setSelected(false);
        }
    }

    public void changePlaySpeed() {
        float speed = DWLiveLocalReplay.getInstance().getSpeed();

        if (speed == 0.5f) {
            DWLiveLocalReplay.getInstance().setSpeed(1.0f);
            playSpeed.setText("1.0x");
        } else if (speed == 1.0f) {
            DWLiveLocalReplay.getInstance().setSpeed(1.5f);
            playSpeed.setText("1.5x");
        } else if (speed == 1.5f) {
            DWLiveLocalReplay.getInstance().setSpeed(0.5f);
            playSpeed.setText("0.5x");
        } else {
            playSpeed.setText("1.0x");
            DWLiveLocalReplay.getInstance().setSpeed(1.0f);
        }
    }

    public void onConfiChanged(boolean isPortrait) {
        if (isPortrait) {
            fullScreen.setSelected(true); //按下是半屏状态
        } else {
            fullScreen.setSelected(false);
        }
    }

    public void changeScreenStatus() {
        if (fullScreen.isSelected()) {
            mContext.setScreenStatus(true);
        } else {
            mContext.setScreenStatus(false);
        }

    }

    public void changeToPortraitLayout() {
            mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setScreenVisible(false, false);
        }
    };

    // 返回值表示显示或者隐藏，true表示显示
    public boolean OnPlayClick() {
        if (playControlTopLeftLayout.isShown()) {
            setScreenVisible(false, true);
            return false;
        } else {
            setScreenVisible(true, true);
            return true;
        }
    }

    private void postDelayScreenDisappear() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 5 * 1000);
    }

    Handler handler = new Handler();

    public void setScreenVisible(boolean isDisplay, boolean isAnimation) {
        handler.removeCallbacks(runnable);

        if (isDisplay) {
            // 延迟5s消失
            postDelayScreenDisappear();

            playControlTopLeftLayout.setVisibility(View.VISIBLE);
            playControlBottom.setVisibility(View.VISIBLE);

            if (isAnimation) {
                playControlTopLeftLayout.startAnimation(getTranslateAnimation(0.0f, 0.0f, -1 * playControlTop.getHeight(), 0.0f, true));
                playControlBottom.startAnimation(getTranslateAnimation(0.0f, 0.0f, playControlBottom.getHeight(), 0.0f, true));
            }
        } else {
            playControlBottom.startAnimation(getTranslateAnimation(0.0f, 0.0f, 0.0f, playControlBottom.getHeight(), false));
            playControlBottom.setVisibility(View.GONE);

            playControlTopLeftLayout.startAnimation(getTranslateAnimation(0.0f, 0.0f, 0.0f, -1 * playControlTop.getHeight(), false));
            playControlTopLeftLayout.setVisibility(View.INVISIBLE);

        }
    }

    public void onPrepared() {

        replaySign.setVisibility(View.VISIBLE);
        setScreenVisible(true, true);
    }

    void toastOnUiThread(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    private TranslateAnimation getTranslateAnimation(float fromX, float toX, float fromY, float toY, boolean isFillAfter) {
        TranslateAnimation animation = new TranslateAnimation(fromX, toX, fromY, toY);
        animation.setFillAfter(isFillAfter);
        animation.setDuration(300);
        return animation;
    }

    public void onDestroy() {
        handler.removeCallbacks(runnable);
    }

    public void onPause() {
    }

    public void setCurrentTime(final long time) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentTime.setText(getFormatTime(time));
                playSeekBar.setProgress((int) time);
            }
        });
    }

    public void setDurationTextView(long playerDuration) {
        durationTextView.setText(getFormatTime(playerDuration));
        playSeekBar.setMax((int)playerDuration);
    }

    public String getFormatTime(long oriTime) {
        oriTime = oriTime / 1000; // 单位为毫秒
        StringBuilder sb = new StringBuilder();

        sb.append(getFillZero(oriTime / 60));
        sb.append(":");
        sb.append(getFillZero(oriTime % 60));

        return sb.toString();
    }

    public String getFillZero(long number) {

        if (number < 10) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }

    public void setBufferPercent(int percent) {
        playSeekBar.setSecondaryProgress((int)((double)playSeekBar.getMax() * percent / 100));
    }
}
