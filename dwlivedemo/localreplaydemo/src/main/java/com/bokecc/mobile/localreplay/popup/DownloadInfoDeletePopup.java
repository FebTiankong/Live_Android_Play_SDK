package com.bokecc.mobile.localreplay.popup;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.bokecc.mobile.localreplay.PilotActivity;
import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.base.BasePopupWindow;
import com.bokecc.mobile.localreplay.base.PopupAnimUtil;
import com.bokecc.mobile.localreplay.util.DataSet;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class DownloadInfoDeletePopup extends BasePopupWindow {

    public DownloadInfoDeletePopup(Context context) {
        super(context);
    }

    TextView cancel;
    TextView deleteItem;
    @Override
    protected void onViewCreated() {
        cancel = findViewById(R.id.id_cancel);
        deleteItem = findViewById(R.id.id_delete_item);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileName != null) {

                    DataSet.removeDownloadInfo(fileName);

                    if (mContext instanceof PilotActivity) {
                        ((PilotActivity) mContext).notifyDataChanged();
                    }

                    dismiss();
                }
            }
        });

    }

    private String fileName;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected int getContentView() {
        return R.layout.delete_download_layout;
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
