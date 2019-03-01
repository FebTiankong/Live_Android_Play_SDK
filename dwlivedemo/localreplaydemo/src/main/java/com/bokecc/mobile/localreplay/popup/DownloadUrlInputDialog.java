package com.bokecc.mobile.localreplay.popup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.mobile.localreplay.PilotActivity;
import com.bokecc.mobile.localreplay.R;
import com.bokecc.mobile.localreplay.util.DataSet;

/**
 * Created by liufh on 2017/2/22.
 */

public class DownloadUrlInputDialog extends DialogFragment {



    TextView addNewUrl;
    EditText urlInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.add_download_layout, container);

        addNewUrl = (TextView) view.findViewById(R.id.id_add_new_url);
        urlInput = (EditText) view.findViewById(R.id.id_url_input);

        addNewUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = urlInput.getText().toString().trim();
                if (url.startsWith("http") && url.endsWith("ccr")) {
                    DataSet.addDownloadInfo(url);
                    if (getActivity() instanceof PilotActivity) {
                        ((PilotActivity) getActivity()).notifyDataChanged();
                    }

                    urlInput.setText("");
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "请输入正确的url", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
