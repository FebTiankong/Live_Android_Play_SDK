package com.bokecc.mobile.localreplay.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.global.QaInfo;
import com.bokecc.mobile.localreplay.util.DensityUtil;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class LiveQaAdapter extends RecyclerView.Adapter<LiveQaAdapter.ChatViewHolder> {

    private Context mContext;
    private LinkedHashMap<String, QaInfo> mQaInfoMap;
    private LayoutInflater mInflater;

    public LiveQaAdapter(Context context) {
        mQaInfoMap = new LinkedHashMap<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 重连的时候，需要重置
     */
    public void resetQaInfos() {
        //TODO 注意这块，别忘了写
        if (mQaInfoMap != null) {
            mQaInfoMap.clear();
        }
    }

    private boolean isOnlyShowSelf = false;
    public void setOnlyShowSelf(boolean isOnlyShowSelf) {
        this.isOnlyShowSelf = isOnlyShowSelf;
        notifyDataSetChanged();
    }

    public void addReplayQuestoinAnswer(LinkedHashMap<String, QaInfo> mQaInfoMap) {
        this.mQaInfoMap = mQaInfoMap;
        notifyDataSetChanged();
    }

    public void addQuestion(Question question) {
        if (mQaInfoMap.containsKey(question.getId())) {
            return;
        } else {
            mQaInfoMap.put(question.getId(), new QaInfo(question));
        }

        notifyDataSetChanged();
    }

    public void addAnswer(Answer answer) {
        if (mQaInfoMap.containsKey(answer.getQuestionId())) {
            mQaInfoMap.get(answer.getQuestionId()).addAnswer(answer);
            notifyDataSetChanged();
        }
    }


    public LinkedHashMap<String, QaInfo> getQaInfos() {
        return mQaInfoMap;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.live_pc_qa_single_line, parent, false);
        return new ChatViewHolder(itemView);
    }

    //TODO
    public void getPostion() {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ArrayList<String> list = new ArrayList<>(mQaInfoMap.keySet()); //TODO 看看能不能换一下
        QaInfo info = mQaInfoMap.get(list.get(position));

        Question question = info.getQuestion();
        ArrayList<Answer> answers = info.getAnswers();

        holder.questionName.setText(question.getQuestionUserName());
        holder.questionTime.setText(question.getTime());// TODO 如果是-1怎么处理
        holder.questionContent.setText(question.getContent());

        holder.answerContainer.removeAllViews();

        for (Answer answer: answers) {
            String msg = answer.getAnswerUserName() + "：" + answer.getContent();
            SpannableString ss = new SpannableString(msg);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")),
                    0, answer.getAnswerUserName().length() + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")),
                    answer.getAnswerUserName().length() + 2, msg.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            TextView textView = new TextView(mContext);
            textView.setText(ss);
            textView.setLineSpacing(0, 1.5f);
            int paddingPixcel = DensityUtil.dp2px(mContext, 10);
            textView.setPadding(paddingPixcel, paddingPixcel, paddingPixcel, paddingPixcel);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.pc_live_qa_answer));
            textView.setGravity(Gravity.CENTER_VERTICAL);
//            textView.setBackground(mContext.getResources().getDrawable(R.drawable.qa_answer_textview_bg));
            holder.answerContainer.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            View mView = new View(mContext);
            mView.setBackground(new ColorDrawable(Color.rgb(232, 232, 232)));
            holder.answerContainer.addView(mView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }

        holder.qaSingleLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mQaInfoMap == null ? 0 : mQaInfoMap.size();
    }

    final class ChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_question_name)
        TextView questionName;
        @BindView(R.id.tv_question_time)
        TextView questionTime;
        @BindView(R.id.tv_question)
        TextView questionContent;
        @BindView(R.id.ll_answer)
        LinearLayout answerContainer;
        @BindView(R.id.ll_qa_single_layout)
        LinearLayout qaSingleLayout;

        public ChatViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
