package com.bokecc.mobile.localreplay.popup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.base.BasePopupWindow;
import com.bokecc.mobile.localreplay.base.PopupAnimUtil;


/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class ListPopup extends BasePopupWindow {

    private ImageView mBack, mClose;
    private TextView mTitle;
    private RecyclerView mList;
    private RelativeLayout mChatLayout;

    public ListPopup(Context context) {
        super(context);
    }

    public ListPopup(Context context, int width, int height) {
        super(context, width, height);
    }

    @Override
    protected void onViewCreated() {
        mBack = findViewById(R.id.id_list_back);
        mClose = findViewById(R.id.id_list_close);
        mTitle = findViewById(R.id.id_list_title);
        mList = findViewById(R.id.id_list_user);
        mChatLayout = findViewById(R.id.id_private_list_chat_layout);
    }

    public void setBackOnClickListener(View.OnClickListener listener) {
        mBack.setOnClickListener(listener);
    }

    public void setCloseOnClickListener(View.OnClickListener listener) {
        mClose.setOnClickListener(listener);
    }

    public void setBackVisibility(int visibility) {
        mBack.setVisibility(visibility);
    }

    public void setCloseVisibility(int visibility) {
        mClose.setVisibility(visibility);
    }

    public void setTitleVisibility(int visibility) {
        mTitle.setVisibility(visibility);
    }

    public void setChatLayoutVisibility(int visibility) {
        mChatLayout.setVisibility(visibility);
    }

    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        mTitle.setText(title);
    }

    public void setBackImageRes(int resId) {
        mBack.setImageResource(resId);
    }

    public void setCloseImageRes(int resId) {
        mClose.setImageResource(resId);
    }

    @Override
    protected int getContentView() {
        return R.layout.private_list;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefTranslateEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefTranslateExitAnim();
    }



}
