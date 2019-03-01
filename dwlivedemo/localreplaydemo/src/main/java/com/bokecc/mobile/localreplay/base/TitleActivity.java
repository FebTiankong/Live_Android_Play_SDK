package com.bokecc.mobile.localreplay.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.base.contract.TitleContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public abstract class TitleActivity<V extends TitleActivity.ViewHolder> extends BaseActivity implements TitleContract.View {

    @BindView(R.id.id_title_tool_bar)
    protected Toolbar mTitleBar;
    @BindView(R.id.id_list_back)
    ImageView mLeft;
    @BindView(R.id.id_list_title)
    TextView mTitle;
    @BindView(R.id.id_list_right)
    TextView mRight;
    @BindView(R.id.id_title_content_layout)
    FrameLayout mContent;

    private View mContentView;
    private OnTitleClickListener mOnTitleClickListener;
    protected V mViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewHolder != null) {
            if (mViewHolder.mUnbinder != null) {
                mViewHolder.mUnbinder.unbind();
                mViewHolder.mUnbinder = null;
            }
            mViewHolder = null;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_title;
    }

    @Override
    protected void onViewCreated() {
        mContent.removeAllViews();
        mContentView = LayoutInflater.from(this).inflate(getContentLayoutId(), null);
        mContent.addView(mContentView);
        onBindViewHolder();
    }

    /**
     * 获取布局内容
     */
    protected abstract int getContentLayoutId();

    protected abstract void onBindViewHolder();

    @OnClick(R.id.id_list_back)
    void onLeftClick() {
        if (mOnTitleClickListener != null) {
            mOnTitleClickListener.onLeft();
        }
    }

    @OnClick(R.id.id_list_right)
    void onRightClick() {
        if (mOnTitleClickListener != null) {
            mOnTitleClickListener.onRight();
        }
    }

    public View getContentView() {
        return mContentView;
    }

    @Override
    public void setTitleOptions(TitleOptions options) {
        mTitleBar.setTitle(""); // 屏蔽原始的标题
        setSupportActionBar(mTitleBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0f);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTitleBar.setElevation(0f);
        }

        if (options.leftResId != 0) {
            mLeft.setImageResource(options.leftResId);
        }
        if (options.rightResId != 0) {
            mRight.setBackgroundResource(options.rightResId);
        }
        if (!TextUtils.isEmpty(options.rightValue)) {
            mRight.setText(options.rightValue);
        }
        if (!TextUtils.isEmpty(options.title)) {
            mTitle.setText(options.title);
        }

        setLeftStatus(options.leftStatus);
        setTitleStatus(options.titleStatus);
        setRightStatus(options.rightStatus);

        mOnTitleClickListener = options.onTitleClickListener;

    }

    /**
     * 设置左边状态
     */
    private void setLeftStatus(int status) {
        if (status == TitleOptions.VISIBLE) {
            mLeft.setVisibility(View.VISIBLE);
        } else if (status == TitleOptions.INVISIBLE){
            mLeft.setVisibility(View.INVISIBLE);
        } else {
            mLeft.setVisibility(View.GONE);
        }
    }

    /**
     * 设置右边状态
     */
    private void setRightStatus(int status) {
        if (status == TitleOptions.VISIBLE) {
            mRight.setVisibility(View.VISIBLE);
        } else if (status == TitleOptions.INVISIBLE){
            mRight.setVisibility(View.INVISIBLE);
        } else {
            mRight.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题状态
     */
    private void setTitleStatus(int status) {
        if (status == TitleOptions.VISIBLE) {
            mTitle.setVisibility(View.VISIBLE);
        } else if (status == TitleOptions.INVISIBLE){
            mTitle.setVisibility(View.INVISIBLE);
        } else {
            mTitle.setVisibility(View.GONE);
        }
    }

    public interface OnTitleClickListener {
        void onLeft();
        void onRight();
    }

    public abstract class OnLeftClickListener implements OnTitleClickListener {
        @Override
        public void onRight() {
            // Ignore
        }
    }

    public abstract class OnRightClickListener implements OnTitleClickListener {
        @Override
        public void onLeft() {
            // Ignore
        }
    }

    public static class ViewHolder {

        Unbinder mUnbinder;

        public ViewHolder(View view) {
            mUnbinder = ButterKnife.bind(this, view);
        }
    }

}
