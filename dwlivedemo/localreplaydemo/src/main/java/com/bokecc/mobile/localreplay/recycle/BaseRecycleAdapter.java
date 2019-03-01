package com.bokecc.mobile.localreplay.recycle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public abstract class BaseRecycleAdapter<VH extends BaseRecycleAdapter.BaseViewHolder, T> extends RecyclerView.Adapter<VH> {

    private Context mContext;
    protected ArrayList<T> mDatas;

    public BaseRecycleAdapter(Context context) {
        mContext = context;
        mDatas = new ArrayList<>();
    }

    public void bindDatas(ArrayList<T> datas) {
        mDatas = datas;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).
                inflate(getItemView(), parent, false);
        return getViewHolder(itemView);
    }

    @Override
    public abstract void onBindViewHolder(VH holder, int position);

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract int getItemView();

    public abstract VH getViewHolder(View itemView);

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

