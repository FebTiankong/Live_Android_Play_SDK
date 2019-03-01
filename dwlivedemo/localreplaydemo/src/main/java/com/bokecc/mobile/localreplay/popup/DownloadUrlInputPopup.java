package com.bokecc.mobile.localreplay.popup;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.mobile.localreplay.PilotActivity;
import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.base.BasePopupWindow;
import com.bokecc.mobile.localreplay.base.PopupAnimUtil;
import com.bokecc.mobile.localreplay.util.DataSet;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class DownloadUrlInputPopup extends BasePopupWindow {

    public DownloadUrlInputPopup(Context context) {
        super(context);
    }

    TextView addNewUrl;
    EditText urlInput;

    @Override
    protected void onViewCreated() {
        addNewUrl = findViewById(R.id.id_add_new_url);
        urlInput = findViewById(R.id.id_url_input);

        addNewUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = urlInput.getText().toString().trim();
                if (url.startsWith("http") && url.endsWith("ccr")) {
                    DataSet.addDownloadInfo(url);
                    if (mContext instanceof PilotActivity) {
                        ((PilotActivity) mContext).notifyDataChanged();
                    }

                    urlInput.setText("");
                    dismiss();
                } else {
                    Toast.makeText(mContext, "请输入正确的url", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected int getContentView() {
        return R.layout.add_download_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }
}
