package com.bokecc.mobile.localreplay.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public abstract class CommonAdapter<T> extends BaseAdapter {

    protected Context mContext;

    protected ArrayList<T> datas;

    public CommonAdapter(Context context) {
        mContext = context;
        datas = new ArrayList<>();
    }

    public void bindData(ArrayList<T> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent,
                getItemViewId());
        onBindViewHolder(viewHolder, position);
        return viewHolder.getConvertView();
    }

    protected abstract void onBindViewHolder(ViewHolder viewHolder, int position);

    protected abstract int getItemViewId();

}
