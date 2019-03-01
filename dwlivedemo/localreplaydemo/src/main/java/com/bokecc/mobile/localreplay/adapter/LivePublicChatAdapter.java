package com.bokecc.mobile.localreplay.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.module.ChatEntity;
import com.bokecc.mobile.localreplay.util.EmojiUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class LivePublicChatAdapter extends RecyclerView.Adapter<LivePublicChatAdapter.ChatViewHolder> {

    private Context mContext;
    private ArrayList<ChatEntity> mChatEntities;
    private LayoutInflater mInflater;
    private String selfId;

    public LivePublicChatAdapter(Context context) {
        mChatEntities = new ArrayList<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        selfId = "";
    }

    /**
     * 添加数据，用于回放的添加
     */
    public void add(ArrayList<ChatEntity> mChatEntities) {
        this.mChatEntities = mChatEntities;
        notifyDataSetChanged();
    }

    /**
     * 添加数据
     */
    public void add(ChatEntity chatEntity) {
        mChatEntities.add(chatEntity);
        if (mChatEntities.size() > 300) { // 当消息达到300条的时候，移除最早的消息
            mChatEntities.remove(0);
        }
        notifyDataSetChanged();
    }

    public ArrayList<ChatEntity> getChatEntities() {
        return mChatEntities;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == selfType) {
            itemView = mInflater.inflate(R.layout.live_portrait_chat_single_self, parent, false);
            // TODO
        } else {
            itemView = mInflater.inflate(R.layout.live_portrait_chat_single_other, parent, false);
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
        }



        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatEntity chatEntity = mChatEntities.get(position);

        String msg = chatEntity.getMsg();
        SpannableString ss = new SpannableString(msg);
        holder.mContent.setText(EmojiUtil.parseFaceMsg(mContext, ss));

        holder.mName.setText(chatEntity.getUserName());
    }

    private int otherType = 0;
    private int selfType = 1;

    @Override
    public int getItemViewType(int position) {
        ChatEntity chat = mChatEntities.get(position);
        if (chat.getUserId().equals(selfId)) {
            return selfType;
        } else {
            return otherType;
        }
    }

    @Override
    public int getItemCount() {
        return mChatEntities == null ? 0 : mChatEntities.size();
    }

    final class ChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pc_chat_single_msg)
        TextView mContent;

        @BindView(R.id.pc_chat_single_name)
        TextView mName;

        ChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
