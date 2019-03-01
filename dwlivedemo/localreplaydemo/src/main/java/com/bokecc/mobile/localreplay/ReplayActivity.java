package com.bokecc.mobile.localreplay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.mobile.localreplay.adapter.LivePublicChatAdapter;
import com.bokecc.mobile.localreplay.adapter.LiveQaAdapter;
import com.bokecc.mobile.localreplay.base.BaseActivity;
import com.bokecc.mobile.localreplay.global.QaInfo;
import com.bokecc.mobile.localreplay.manage.ReplayPlayerManager;
import com.bokecc.mobile.localreplay.module.ChatEntity;
import com.bokecc.mobile.localreplay.popup.CommonPopup;
import com.bokecc.mobile.localreplay.util.DownloadConfig;
import com.bokecc.mobile.localreplay.util.DownloadUtil;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplayListener;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayAnswerMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLiveInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQuestionMsg;
import com.bokecc.sdk.mobile.live.widget.DocView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


/**
 * 直播离线回放页
 * Created by liufh on 2016/12/8.
 */
public class ReplayActivity extends BaseActivity implements TextureView.SurfaceTextureListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnCompletionListener{

    private final static String TAG = "ReplayActivity";

    @BindView(R.id.textureview_pc_live_play)
    TextureView mPlayerContainer;

    @BindView(R.id.replay_player_control_layout)
    RelativeLayout playerControlLayout;

    @BindView(R.id.pc_live_infos_layout)
    RelativeLayout rlLiveInfosLayout;

    @BindView(R.id.pc_portrait_progressBar)
    ProgressBar pcPortraitProgressBar;

    ReplayPlayerManager replayPlayerManager;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        replayPlayerManager.setScreenVisible(true, true);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setPortraitLayoutVisibility(View.VISIBLE);
            playerControlLayout.setVisibility(View.VISIBLE);
            replayPlayerManager.onConfiChanged(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setPortraitLayoutVisibility(View.GONE);
            playerControlLayout.setVisibility(View.VISIBLE);
            mPlayerContainer.setLayoutParams(getVideoSizeParams());
            replayPlayerManager.onConfiChanged(false);
        }
    }


    @OnClick(R.id.rl_pc_live_top_layout)
    void onPlayOnClick(View v) {
        replayPlayerManager.OnPlayClick();
    }

    // 退出界面弹出框
    private CommonPopup mExitPopup;

    private View mRoot;
    private IjkMediaPlayer player;
    private DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();

    private WindowManager wm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pc_replay;
    }


    @Override
    protected void onViewCreated() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        replayPlayerManager = new ReplayPlayerManager(this, playerControlLayout, mRoot);
        replayPlayerManager.init();

        initClosePopup();
        initPlayer();
        initLocalReplay();
    }

    private void initClosePopup() {
        mExitPopup = new CommonPopup(this);
        mExitPopup.setOutsideCancel(true);
        mExitPopup.setKeyBackCancel(true);
        mExitPopup.setTip("您确认结束观看吗?");
        mExitPopup.setOKClickListener(new CommonPopup.OnOKClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });

    }

    private void initPlayer() {
        mPlayerContainer.setSurfaceTextureListener(this);
        player = new IjkMediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnInfoListener(this);
        player.setOnVideoSizeChangedListener(this);
        player.setOnCompletionListener(this);
    }

    private void initLocalReplay() {
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");

        if (fileName == null) {
            finish();
        }

        inflater = LayoutInflater.from(this);
        initDocLayout(inflater);
        docView = docLayoutController.getDocView();

        File oriFile = new File(DownloadConfig.DOWNLOAD_DIR, fileName);
        dwLiveLocalReplay.setReplayParams(myDWLiveLocalReplayListener, player, docView, DownloadUtil.getUnzipDir(oriFile));
    }

    private ArrayList<ChatEntity> mChatEntities;
     private LinkedHashMap<String, QaInfo> mQaInfoMap;

    private ChatEntity getReplayChatEntity(ReplayChatMsg msg) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(msg.getUserId());
        chatEntity.setUserName(msg.getUserName());
        chatEntity.setPrivate(false);
        chatEntity.setPublisher(true);
        chatEntity.setMsg(msg.getContent());
        chatEntity.setTime(String.valueOf(msg.getTime()));
        chatEntity.setUserAvatar(msg.getAvatar());
        return chatEntity;
    }

    private DWLiveLocalReplayListener myDWLiveLocalReplayListener = new DWLiveLocalReplayListener() {

        @Override
        public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
            final LinkedHashMap<String, QaInfo> qaInfoMap = new LinkedHashMap<>();

            for (ReplayQAMsg qaMsg: qaMsgs) {
                ReplayQuestionMsg questionMsg = qaMsg.getReplayQuestionMsg();
                Question question = new Question();
                question.setContent(questionMsg.getContent())
                        .setId(questionMsg.getQuestionId())
                        .setQuestionUserId(questionMsg.getQuestionUserId())
                        .setQuestionUserName(questionMsg.getQuestionUserName())
                        .setTime(String.valueOf(questionMsg.getTime()))
                        .setUserAvatar(questionMsg.getQuestionUserAvatar());

                TreeSet<ReplayAnswerMsg> answerMsgs = qaMsg.getReplayAnswerMsgs();

                // 没有回答
                if (answerMsgs.size() < 1) {
                    if (questionMsg.getIsPublish() == 0) {
                        // 未发布的问题
                        continue;
                    } else if (questionMsg.getIsPublish() == 1) {
                        // 发布的问题
                        QaInfo qaInfo = new QaInfo(question);
                        qaInfoMap.put(question.getId(), qaInfo);
                        continue;
                    }
                }

                // 回答过
                QaInfo qaInfo = new QaInfo(question);
                for (ReplayAnswerMsg answerMsg:answerMsgs) {
                    Answer answer = new Answer();
                    answer.setUserAvatar(answerMsg.getUserAvatar())
                            .setContent(answerMsg.getContent())
                            .setAnswerUserId(answerMsg.getUserId())
                            .setAnswerUserName(answerMsg.getUserName())
                            .setReceiveTime(String.valueOf(answerMsg.getTime()))
                            .setUserRole(answerMsg.getUserRole());
                    qaInfo.addAnswer(answer);
                }

                qaInfoMap.put(question.getId(), qaInfo);
            }

            mQaInfoMap = qaInfoMap;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 如果不需要根据时间轴展示，就直接交给UI去做展示
                    if (!DWApplication.REPLAY_QA_FOLLOW_TIME) {
                        qaLayoutController.addReplayQAInfos(mQaInfoMap);
                    }
                }
            });
        }

        @Override
        public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {

            ArrayList<ChatEntity> chatEntities = new ArrayList<>();

            for (ReplayChatMsg msg: replayChatMsgs) {
                chatEntities.add(getReplayChatEntity(msg));
            }

            mChatEntities = chatEntities;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 如果不需要根据时间轴展示，就直接交给UI去做展示
                    if (!DWApplication.REPLAY_CHAT_FOLLOW_TIME) {
                        if (chatLayoutController != null) {
                            chatLayoutController.addChatEntities(mChatEntities);
                        }
                    }
                }
            });

        }

        /**
         * 广播信息
         *
         * @param broadCastMsgList 广播信息列表
         */
        @Override
        public void onBroadCastMessage(ArrayList<ReplayBroadCastMsg> broadCastMsgList) {
            if (broadCastMsgList == null) {
                return;
            }

            if (broadCastMsgList.size() > 0) {
                for (ReplayBroadCastMsg broadCastMsg : broadCastMsgList) {
                    Log.i(TAG, "广播内容 ：" + broadCastMsg.getContent() + ", 发布时间：" + broadCastMsg.getTime());
                }
            }
        }

        @Override
        public void onPageInfoList(ArrayList<ReplayPageInfo> infoList) {
            // TODO 回放页面信息列表
        }

        /**
         * 回调当前翻页的信息<br/>
         * 注意：<br/>
         * 白板docTotalPage一直为0，pageNum从1开始<br/>
         * 其他文档docTotalPage为正常页数，pageNum从0开始
         *
         * @param docId        文档Id
         * @param docName      文档名称
         * @param pageNum      当前页码
         * @param docTotalPage 当前文档总共的页数
         */
        @Override
        public void onPageChange(String docId, String docName, int pageNum, int docTotalPage) {
            Log.i(TAG, "文档ID ：" + docId + ", 文档名称：" + docName + ", 当前页码：" + pageNum + ", 总共页数：" + docTotalPage);
        }

        @Override
        public void onException(DWLiveException exception) {}

        @Override
        public void onInfo(TemplateInfo templateInfo, final RoomInfo roomInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    replayPlayerManager.setTitle(roomInfo.getName());

                    // 如果还没初始化ViewPager就执行一次，不能多次执行初始化
                    if (!hasInitViewPager) {
                        initViewPager();
                    }
                }
            });
        }

        @Override
        public void onInitFinished() {
            // 回放的直播开始时间和结束时间必须在登录成功后再获取，否则为空

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ReplayLiveInfo replayLiveInfo = DWLiveLocalReplay.getInstance().getReplayLiveInfo();
                    if (replayLiveInfo != null) {
                        Toast.makeText(ReplayActivity.this, "直播开始时间：" + replayLiveInfo.getStartTime() + "\n"
                                + "直播结束时间：" +  replayLiveInfo.getEndTime(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        replayPlayerManager.onDestroy();

        if (timerTask != null) {
            timerTask.cancel();
        }

        if (player != null) {
            player.pause();
            player.stop();
            player.release();
        }

        dwLiveLocalReplay.onDestroy();

        super.onDestroy();
    }

    boolean isOnPause = false;
    long currentPosition;

    @Override
    protected void onPause() {
        isPrepared = false;
        isOnPause = true;
        if (player != null) {
            player.pause();
            currentPosition = player.getCurrentPosition();
        }

        dwLiveLocalReplay.stop();
        stopTimerTask();

        super.onPause();
    }


    /** isOnResumeStart 的意义在于部分手机从Home跳回到APP的时候，不会触发onSurfaceTextureAvailable */
    boolean isOnResumeStart = false;

    @Override
    protected void onResume() {
        super.onResume();
        isOnResumeStart = false;
        if (surface != null) {
            dwLiveLocalReplay.start(surface);
            isOnResumeStart = true;
        }
    }

    Surface surface;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        surface = new Surface(surfaceTexture);
        if (isOnResumeStart) {
            return;
        }
        dwLiveLocalReplay.start(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        surface = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}

    boolean isPrepared = false;

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        isPrepared = true;
        player.start();

        if (currentPosition > 0) {
            player.seekTo(currentPosition);
        }

        pcPortraitProgressBar.setVisibility(View.GONE);
        playerControlLayout.setVisibility(View.VISIBLE);

        if (isPortrait()) {
            setPortraitLayoutVisibility(View.VISIBLE);
        } else {
            setPortraitLayoutVisibility(View.GONE);
        }

        if (replayPlayerManager != null) {
            replayPlayerManager.onPrepared();
            replayPlayerManager.setDurationTextView(player.getDuration());
        }

        // 更新一下当前播放的按钮的状态
        replayPlayerManager.changePlayIconStatus(player.isPlaying());

        startTimerTask();
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        // 监听播放结束后，更改当前播放器的按钮的状态
        replayPlayerManager.changePlayIconStatus(player.isPlaying());
    }

    Timer timer = new Timer();
    TimerTask timerTask;
    private void startTimerTask() {
        stopTimerTask();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                replayPlayerManager.setCurrentTime(player.getCurrentPosition());

                // 回放的聊天内容随时间轴推进展示
                if (DWApplication.REPLAY_CHAT_FOLLOW_TIME) {
                    if (mChatEntities != null && mChatEntities.size() >= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<ChatEntity> temp_chatEntities = new ArrayList<>();
                                int time = Math.round(player.getCurrentPosition() / 1000);
                                for (ChatEntity entity : mChatEntities) {
                                    if (!TextUtils.isEmpty(entity.getTime()) && time >= Integer.valueOf(entity.getTime())) {
                                        temp_chatEntities.add(entity);
                                    }
                                }
                                chatLayoutController.addChatEntities(temp_chatEntities);
                            }
                        });
                    }
                }

                // 回放的问答内容随时间轴推进展示
                if (DWApplication.REPLAY_QA_FOLLOW_TIME) {
                    if (mQaInfoMap != null && mQaInfoMap.size() >= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LinkedHashMap<String, QaInfo> temp_qaInfoMap = new LinkedHashMap<>();
                                int time = Math.round(player.getCurrentPosition() / 1000);
                                Iterator it = mQaInfoMap.entrySet().iterator();
                                while(it.hasNext()) {
                                    Map.Entry entity = (Map.Entry) it.next();
                                    QaInfo qaInfo = (QaInfo) entity.getValue();
                                    String key = entity.getKey().toString();
                                    if (!TextUtils.isEmpty(qaInfo.getQuestion().getTime()) && time >= Integer.valueOf(qaInfo.getQuestion().getTime())) {
                                        temp_qaInfoMap.put(key, qaInfo);
                                    }
                                }
                                qaLayoutController.addReplayQAInfos(temp_qaInfoMap);
                            }
                        });
                    }
                }
            }
        };

        timer.schedule(timerTask, 0, 1 * 1000);
    }

    private void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    private void setPortraitLayoutVisibility(int i) {
        rlLiveInfosLayout.setVisibility(i);
    }

    private void setLandScapeVisibility(int i) {
        playerControlLayout.setVisibility(i);
        rlLiveInfosLayout.setVisibility(i);
    }


    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {

        if (width == 0 || height == 0) {
            return;
        }
        mPlayerContainer.setLayoutParams(getVideoSizeParams());
    }

    // 视频等比缩放
    private RelativeLayout.LayoutParams getVideoSizeParams() {

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        int vWidth = player.getVideoWidth();
        int vHeight = player.getVideoHeight();

        if (vWidth == 0) {
            vWidth = 600;
        }
        if (vHeight == 0) {
            vHeight = 400;
        }

        if (vWidth > width || vHeight > height) {
            float wRatio = (float) vWidth / (float) width;
            float hRatio = (float) vHeight / (float) height;
            float ratio = Math.max(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth / ratio);
            height = (int) Math.ceil((float) vHeight / ratio);
        } else {
            float wRatio = (float) width / (float) vWidth;
            float hRatio = (float) height / (float) vHeight;
            float ratio = Math.min(wRatio, hRatio);

            width = (int) Math.ceil((float) vWidth * ratio);
            height = (int) Math.ceil((float) vHeight * ratio);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        return params;
    }

    public boolean isPortrait() {
        int mOrientation = getApplicationContext().getResources().getConfiguration().orientation;
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPortrait()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        } else {
            if (chatLayoutController != null && chatLayoutController.onBackPressed()) {
                return;
            }
        }

        mExitPopup.show(mRoot);
    }

    //------------------------下方布局------------------------

    @BindView(R.id.rg_infos_tag)
    RadioGroup tagRadioGroup;

    @BindView(R.id.live_portrait_info_document)
    RadioButton docTag;

    @BindView(R.id.live_portrait_info_chat)
    RadioButton chatTag;

    @BindView(R.id.live_portrait_info_qa)
    RadioButton qaTag;

    @BindView(R.id.live_portrait_container_viewpager)
    ViewPager infoLayoutContainer;

    List<View> infoList = new ArrayList<>();
    List<Integer> tagIdList = new ArrayList<>();
    List<RadioButton> tagRBList = new ArrayList<>();

    View docLayout;
    View chatLayout;
    View qaLayout;

    DocLayoutController docLayoutController;
    ChatLayoutController chatLayoutController;
    QaLayoutController qaLayoutController;

    private String viewVisibleTag = "1";

    private DocView docView;
    LayoutInflater inflater;

    private boolean hasInitViewPager = false;

    private void initViewPager() {

        //TODO 看看怎么解决这个

        if (viewVisibleTag.equals(dwLiveLocalReplay.getTemplateInfo().getPdfView())) {
            tagIdList.add(R.id.live_portrait_info_document);
            tagRBList.add(docTag);
            infoList.add(docLayout);
            docTag.setVisibility(View.VISIBLE);
        }

        if (viewVisibleTag.equals(dwLiveLocalReplay.getTemplateInfo().getChatView())) {
            initChatLayout(inflater);
        }

        if (viewVisibleTag.equals(dwLiveLocalReplay.getTemplateInfo().getQaView())) {
            initQaLayout(inflater);
        }

        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return infoList.size();
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {

                container.addView(infoList.get(position));
                return infoList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(infoList.get(position));
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        };

        infoLayoutContainer.setAdapter(adapter);


        infoLayoutContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tagRBList.get(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tagRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                infoLayoutContainer.setCurrentItem(tagIdList.indexOf(i), true);
            }
        });


        if (tagRBList.contains(chatTag)) {
            chatTag.performClick();
        } else if (tagRBList.size() > 0) {
            tagRBList.get(0).performClick();
        }

        hasInitViewPager = true;
    }

    private void initDocLayout(LayoutInflater inflater) {
        docLayout = inflater.inflate(R.layout.live_portrait_doc_layout, null);
        docLayoutController = new DocLayoutController(this, docLayout);
    }

    private void initChatLayout(LayoutInflater inflater) {
        tagIdList.add(R.id.live_portrait_info_chat);
        tagRBList.add(chatTag);
        chatTag.setVisibility(View.VISIBLE);
        chatLayout = inflater.inflate(R.layout.live_portrait_chat_layout, null);
        infoList.add(chatLayout);

        chatLayoutController = new ChatLayoutController(this, chatLayout);
        chatLayoutController.initChat();

    }

    private void initQaLayout(LayoutInflater inflater) {
        tagIdList.add(R.id.live_portrait_info_qa);
        tagRBList.add(qaTag);
        qaTag.setVisibility(View.VISIBLE);
        qaLayout = inflater.inflate(R.layout.live_portrait_qa_layout, null);
        infoList.add(qaLayout);

        qaLayoutController = new QaLayoutController(this, qaLayout);
        qaLayoutController.initQaLayout();
    }


    //----------------------文档----------------------------
    public class DocLayoutController {

        @BindView(R.id.live_docview)
        DocView mDocView;

        Context mContext;

        public DocLayoutController(Context context, View view) {
            mContext = context;
            ButterKnife.bind(this, view);
        }

        public DocView getDocView() {
            return mDocView;
        }


    }

    //----------------------聊天-----------------------------
    public class ChatLayoutController {

        //TODO 多个pager切换的隐藏操作需要实现

        @BindView(R.id.chat_container)
        RecyclerView mChatList;

        @BindView(R.id.iv_live_pc_private_chat)
        ImageView mPrivateChatIcon;

        @BindView(R.id.id_private_chat_user_layout)
        LinearLayout mPrivateChatUserLayout;

        @BindView(R.id.id_push_chat_layout)
        RelativeLayout mChatLayout;

        int mChatInfoLength;

        Context mContext;

        public ChatLayoutController(Context context, View view) {
            mContext = context;
            ButterKnife.bind(this, view);
            mChatInfoLength = 0;
            mChatLayout.setVisibility(View.GONE);
            mPrivateChatIcon.setVisibility(View.GONE);
        }

        LivePublicChatAdapter mChatAdapter;

        public void initChat() {
            mChatList.setLayoutManager(new LinearLayoutManager(mContext));
            mChatAdapter = new LivePublicChatAdapter(mContext);
            mChatList.setAdapter(mChatAdapter);
        }


        public boolean onBackPressed() {

            return false;
        }

        /**
         * 回放的聊天添加
         * @param chatEntities
         */
        public void addChatEntities(ArrayList<ChatEntity> chatEntities) {
            // 回放的聊天内容随时间轴推进展示
            if (DWApplication.REPLAY_CHAT_FOLLOW_TIME) {
                // 如果数据长度没发生变化就不刷新
                if (mChatInfoLength != chatEntities.size()) {
                    mChatAdapter.add(chatEntities);
                    mChatList.scrollToPosition(chatEntities.size() - 1);
                    mChatInfoLength = chatEntities.size();
                }
            } else {
                mChatAdapter.add(chatEntities);
            }
        }
    }

    //----------------------问答----------------------------
    public class QaLayoutController {

        @BindView(R.id.rv_qa_container)
        RecyclerView mQaList;

        @BindView(R.id.rl_qa_input_layout)
        RelativeLayout mInputLayout;

        LiveQaAdapter mQaAdapter;

        int mQaInfoLength;

        Context mContext;

        public QaLayoutController(Context context, View view) {
            mContext = context;
            ButterKnife.bind(this, view);
            mQaInfoLength = 0;
            mInputLayout.setVisibility(View.GONE);
        }

        public void initQaLayout() {
            mQaList.setLayoutManager(new LinearLayoutManager(mContext));
            mQaAdapter = new LiveQaAdapter(mContext);
            mQaList.setAdapter(mQaAdapter);
            //TODO 增加分割线
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(ReplayActivity.this, DividerItemDecoration.VERTICAL);
        }

        public void clearQaInfo() {
            mQaAdapter.resetQaInfos();
        }

        public void addReplayQAInfos(LinkedHashMap<String, QaInfo> replayQaInfos) {
            // 回放的问答内容随时间轴推进展示
            if (DWApplication.REPLAY_QA_FOLLOW_TIME) {
                // 如果数据长度没发生变化就不刷新
                if (mQaInfoLength != replayQaInfos.size()) {
                    mQaAdapter.addReplayQuestoinAnswer(replayQaInfos);
                    mQaList.scrollToPosition(replayQaInfos.size() - 1);
                    mQaInfoLength = replayQaInfos.size();
                }
            } else {
                mQaAdapter.addReplayQuestoinAnswer(replayQaInfos);
            }
        }

        public void addQuestion(Question question) {
            mQaAdapter.addQuestion(question);
            //TODO 跳转到那个地方
        }

        public void addAnswer(Answer answer) {
            mQaAdapter.addAnswer(answer);
        }

    }

    public void setPlayerStatus(boolean isPlaying) {
        if (isPlaying) {
            player.start();
        } else {
            player.pause();
        }
    }

    public void setScreenStatus(boolean isFull) {
        if (isFull) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void setSeekPosition(int position) {
        player.seekTo(position);
    }

}