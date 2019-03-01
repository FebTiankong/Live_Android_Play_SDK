package com.bokecc.mobile.localreplay.global;


import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;

import java.util.ArrayList;

/**
 * Created by liufh on 2017/1/6.
 */

public class QaInfo{
    Question mQuestion;
    ArrayList<Answer> answers;

    public QaInfo(Question question) {
        answers = new ArrayList<>();
        this.mQuestion = question;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }
}