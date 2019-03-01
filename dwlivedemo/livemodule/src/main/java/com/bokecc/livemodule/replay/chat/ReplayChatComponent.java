package com.bokecc.livemodule.replay.chat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.replay.DWReplayChatListener;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.chat.adapter.ReplayChatAdapter;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * 回放聊天控件
 */
public class ReplayChatComponent extends RelativeLayout implements DWReplayChatListener {

    Context mContext;
    RecyclerView mChatList;

    int mChatInfoLength;

    public ReplayChatComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
        mChatInfoLength = 0;
    }

    public ReplayChatComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
        mChatInfoLength = 0;
    }

    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.replay_portrait_chat_layout, this, true);
        mChatList = findViewById(R.id.chat_container);
        initChat();
    }

    ReplayChatAdapter mChatAdapter;

    public void initChat() {
        mChatList.setLayoutManager(new LinearLayoutManager(mContext));
        mChatAdapter = new ReplayChatAdapter(mContext);
        mChatList.setAdapter(mChatAdapter);

        // 设置监听
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.setReplayChatListener(this);
        }
    }

    /**
     * 回放的聊天添加
     *
     * @param chatEntities
     */
    public void addChatEntities(ArrayList<ChatEntity> chatEntities) {
        mChatAdapter.add(chatEntities);
    }

    private ArrayList<ChatEntity> mChatEntities;

    private ChatEntity getReplayChatEntity(ReplayChatMsg msg) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(msg.getUserId());
        chatEntity.setUserName(msg.getUserName());
        chatEntity.setUserRole(msg.getUserRole());
        chatEntity.setPrivate(false);
        chatEntity.setPublisher(true);
        chatEntity.setMsg(msg.getContent());
        chatEntity.setTime(String.valueOf(msg.getTime()));
        chatEntity.setUserAvatar(msg.getAvatar());
        return chatEntity;
    }

    /**
     * 聊天信息
     *
     * @param replayChatMsgs 聊天信息
     */
    @Override
    public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
        ArrayList<ChatEntity> chatEntities = new ArrayList<>();

        for (ReplayChatMsg msg : replayChatMsgs) {
            // 判断聊天信息的状态 0：显示  1：不显示
            if ("0".equals(msg.getStatus())) {
                chatEntities.add(getReplayChatEntity(msg));
            }
        }

        mChatEntities = chatEntities;

        mChatList.post(new Runnable() {
            @Override
            public void run() {
                addChatEntities(mChatEntities);
            }
        });
    }
}
