package com.bokecc.mobile.localreplay;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.mobile.localreplay.adapter.DownloadListAdapter;
import com.bokecc.mobile.localreplay.base.BaseActivity;
import com.bokecc.mobile.localreplay.manage.DownloaderWrapper;
import com.bokecc.mobile.localreplay.popup.DownloadInfoDeletePopup;
import com.bokecc.mobile.localreplay.popup.DownloadUrlInputDialog;
import com.bokecc.mobile.localreplay.recycle.BaseOnItemTouch;
import com.bokecc.mobile.localreplay.recycle.ITouchListener;
import com.bokecc.mobile.localreplay.scan.qr_codescan.MipcaActivityCapture;
import com.bokecc.mobile.localreplay.util.DataSet;
import com.bokecc.mobile.localreplay.util.DownloadUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class PilotActivity extends BaseActivity {

    @BindView(R.id.id_new_add)
    TextView tvAddNewAddress;

    @BindView(R.id.id_download_list)
    RecyclerView downloadListView;

    DownloadInfoDeletePopup deletePopup;
    View mRoot;

    private DownloadReceiver downloadReceiver;

    class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    public void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    DownloadUrlInputDialog dialog;

    @Override
    protected void onViewCreated() {
        DataSet.init(this); // 最好放到启动页中

        dialog = new DownloadUrlInputDialog();

        int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        downloadReceiver = new DownloadReceiver();
        registerReceiver(downloadReceiver, new IntentFilter("com.service.action"));

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);

        initDownloadList();
        mRoot = getWindow().getDecorView().findViewById(android.R.id.content);

        deletePopup = new DownloadInfoDeletePopup(this);
        deletePopup.setOutsideCancel(true);
        deletePopup.setBackPressedCancel(true);
    }

    DownloadListAdapter adapter;
    private void initDownloadList() {
        downloadListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadListAdapter(this);
        downloadListView.setAdapter(adapter);
        downloadListView.addItemDecoration(new DividerItemDecoration(PilotActivity.this, LinearLayout.VERTICAL));
        downloadListView.addOnItemTouchListener(new BaseOnItemTouch(downloadListView, new ITouchListener() {
            @Override
            public void onClick(RecyclerView.ViewHolder viewHolder) {
                DownloadListAdapter.DownloadViewHolder holder = (DownloadListAdapter.DownloadViewHolder)viewHolder;
                String fileName = holder.fileName.getText().toString();
                DownloaderWrapper wrapper = DataSet.getDownloadWrapperMap().get(fileName);
                switch (wrapper.getStatus()) {
                    case DownloadUtil.PAUSE:
                    case DownloadUtil.WRONG:
                    case DownloadUtil.WAIT:
                        wrapper.setStatus(DownloadUtil.WAIT);
                        DataSet.checkNewDownload(fileName);
                        break;
                    case DownloadUtil.DOWNLOAD:
                        wrapper.pauseDownload();
                        break;
                    case DownloadUtil.ZIP_ERROR:
                        Toast.makeText(PilotActivity.this, "解压失败，文件重新加入下载队列", Toast.LENGTH_SHORT).show();
                        wrapper.resetDownloadStatus();
                        break;
                    case DownloadUtil.ZIP_FINISH:
                        Intent intent = new Intent(PilotActivity.this, ReplayActivity.class);
                        intent.putExtra("fileName", fileName);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onLongPress(RecyclerView.ViewHolder viewHolder) {

                DownloadListAdapter.DownloadViewHolder holder = (DownloadListAdapter.DownloadViewHolder)viewHolder;
                DownloaderWrapper wrapper = DataSet.getDownloadWrapperMap().get(holder.fileName.getText().toString());

                //TODO 暂时这么处理，看是否要加上解压的删除操作
                if (wrapper.getStatus() == DownloadUtil.ZIP_ING || wrapper.getStatus() == DownloadUtil.ZIP_WAIT) {
                    toastOnUiThread("解压中，请稍候……");
                    return;
                }

                deletePopup.setFileName(holder.fileName.getText().toString());
                deletePopup.show(mRoot);
            }

            @Override
            public void onTouchDown(RecyclerView.ViewHolder viewHolder) {}

            @Override
            public void onTouchUp(RecyclerView.ViewHolder viewHolder) {}
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.id_code_add)
    public void codeScanAddress(View v) {
        Intent intent = new Intent(this, MipcaActivityCapture.class);
        startActivityForResult(intent, qrRequestCode);
    }

    @OnClick(R.id.id_new_add)
    public void addNewAddress(View v) {
        if (!dialog.isAdded()) {
            dialog.show(getSupportFragmentManager(), "EditNameDialog");
        }

    }


    final int qrRequestCode = 111;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case qrRequestCode:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    String url = result.trim();
                    if (url.startsWith("http") && url.endsWith("ccr")) {
                        DataSet.addDownloadInfo(url);
                        notifyDataChanged();
                    } else {
                        Toast.makeText(getApplicationContext(), "扫描失败，请扫描正确的播放二维码", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    long lastTime;
    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000) {
            toastOnUiThread("再按一次，您就可以离开了！");
            lastTime = currentTime;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(downloadReceiver);
        DataSet.saveData();
        super.onDestroy();

    }
}
