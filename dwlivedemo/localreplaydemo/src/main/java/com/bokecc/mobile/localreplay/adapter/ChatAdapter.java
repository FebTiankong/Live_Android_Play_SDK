package com.bokecc.mobile.localreplay.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context mContext;
    private ArrayList<ChatEntity> mChatEntities;
    private LayoutInflater mInflater;

    public ChatAdapter(Context context) {
        mChatEntities = new ArrayList<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
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
        View itemView = mInflater.inflate(R.layout.public_list_item, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatEntity chatEntity = mChatEntities.get(position);
        String msg = chatEntity.getUserName() + ": " + chatEntity.getMsg();
        SpannableString ss = new SpannableString(msg);
        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#FFF5B108")), 0, chatEntity.getUserName().length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (chatEntity.isPublisher()) {
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFF6633")),
                    chatEntity.getUserName().length() + 2,
                    msg.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFFFF")),
                    chatEntity.getUserName().length() + 2,
                    msg.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        holder.mContent.setText(EmojiUtil.parseFaceMsg(mContext, ss));
    }

    @Override
    public int getItemCount() {
        return mChatEntities == null ? 0 : mChatEntities.size();
    }

    final class ChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.id_public_item_content)
        TextView mContent;

        ChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
