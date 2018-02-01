package sino.cargocome.carrier.autoupdatedemo;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.vector.update_app.SilenceUpdateCallback;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.vector.update_app.service.DownloadService;
import com.vector.update_app.utils.AppUpdateUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import rx.functions.Action1;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private final String apk_url_github = "https://github.com/Jorgejie/AutoAppUpdateDemo/raw/master/json/json.txt";
    private boolean isShowDownloadProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
    }

    public void getPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "未授权", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    /**
     * 最简单方式
     */
    public void updateApp(View view) {
        new UpdateAppManager.Builder()
                .setActivity(this)
                .setUpdateUrl(apk_url_github)
                .setHttpManager(new UpdateAppHttpUtil())
                .build().update();
    }

    /**
     * 显示进度条,自定义对话框
     */
    public void updateDiy3(View view) {
        isShowDownloadProgress = true;
        diyUpdate();
    }

    private void diyUpdate() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Map<String, String> params = new HashMap<>();
        params.put("appVersion", BuildConfig.VERSION_NAME);
        new UpdateAppManager.Builder()
                .setActivity(this)
                .setHttpManager(new UpdateAppHttpUtil())
                .setUpdateUrl(apk_url_github)
                .setPost(false)
                .setParams(params)
                .setTargetPath(path)
                .build()
                .checkNewApp(new UpdateCallback() {
                    @Override
                    protected UpdateAppBean parseJson(String json) {
                        UpdateAppBean updateAppBean = new UpdateAppBean();
                        Gson gson = new Gson();
                        UpdateAppBean JsonToUpdateAppBean = gson.fromJson(json, UpdateAppBean.class);
                        updateAppBean
                                .setUpdate(JsonToUpdateAppBean.getUpdate())
                                .setNewVersion(JsonToUpdateAppBean.getNewVersion())
                                .setApkFileUrl(JsonToUpdateAppBean.getApkFileUrl())
                                .setUpdateLog(JsonToUpdateAppBean.getUpdateLog())
                                .setTargetSize(JsonToUpdateAppBean.getTargetSize())
                                .setConstraint(false)
                                //md5值在判断是否下载的时候需要用
                                .setNewMd5(JsonToUpdateAppBean.getNewMd5());

                        return updateAppBean;
                    }

                    @Override
                    protected void hasNewApp(UpdateAppBean updateApp, UpdateAppManager updateAppManager) {
//                        super.hasNewApp(updateApp, updateAppManager);
                        //自定义对话框
                        //判断文件是否已经下载
//                        if (AppUpdateUtils.appIsDownloaded(updateApp)) {
//
//                            showDiyDialog(updateApp, updateAppManager, AppUpdateUtils.getAppFile(updateApp));
//                        } else {

                        showDiyDialog(updateApp, updateAppManager);
//                        }
                    }

                    @Override
                    protected void onAfter() {
                        CProgressDialogUtils.cancelProgressDialog(MainActivity.this);
                    }

                    @Override
                    protected void onBefore() {
                        CProgressDialogUtils.showProgressDialog(MainActivity.this);
                    }
                });

    }

    private void showDiyDialog(UpdateAppBean updateApp, final UpdateAppManager updateAppManager) {
        String targetSize = updateApp.getTargetSize();
        String updateLog = updateApp.getUpdateLog();
        String msg = "";
        if (!TextUtils.isEmpty(targetSize)) {
            msg = "新版本大小:" + targetSize + "\n\n";
        }
        if (!TextUtils.isEmpty(updateLog)) {
            msg += updateLog;
        }
        new AlertDialog.Builder(this)
                .setTitle(String.format("是否升级到%s版本？", updateApp.getNewVersion()))
                .setMessage(msg)
                .setPositiveButton("升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //显示下载进度
                        updateAppManager.download(new DownloadService.DownloadCallback() {
                            @Override
                            public void onStart() {
                                HProgressDialogUtils.showHorizontalProgressDialog(MainActivity.this, "下载进度", false);
                            }

                            @Override
                            public void onProgress(float progress, long totalSize) {
                                HProgressDialogUtils.setProgress(Math.round(progress * 100));
                            }

                            @Override
                            public void setMax(long totalSize) {

                            }

                            @Override
                            public boolean onFinish(File file) {
                                HProgressDialogUtils.cancel();
                                return true;
                            }

                            @Override
                            public void onError(String msg) {
                                HProgressDialogUtils.cancel();
                            }
                        });
                    }
                })
                .setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void silenceUpdateAppAndDiyDialog(View view) {
        new UpdateAppManager
                .Builder()
                //当前Activity
                .setActivity(this)
                //更新地址
                .setUpdateUrl(apk_url_github)
                //实现httpManager接口的对象
                .setHttpManager(new UpdateAppHttpUtil())
                //只有wifi下进行，静默下载(只对静默下载有效)
                .setOnlyWifi()
                .build()
                .checkNewApp(new SilenceUpdateCallback() {
                    @Override
                    protected void showDialog(UpdateAppBean updateApp, UpdateAppManager updateAppManager, File appFile) {
                        showSilenceDiyDialog(updateApp, appFile);
                    }
                });
    }

    private void showSilenceDiyDialog(UpdateAppBean updateApp, final File appFile) {
        String targetSize = updateApp.getTargetSize();
        String updateLog = updateApp.getUpdateLog();

        String msg = "";

        if (!TextUtils.isEmpty(targetSize)) {
            msg = "新版本大小：" + targetSize + "\n\n";
        }

        if (!TextUtils.isEmpty(updateLog)) {
            msg += updateLog;
        }

        new AlertDialog.Builder(this)
                .setTitle(String.format("是否升级到%s版本？", updateApp.getNewVersion()))
                .setMessage(msg)
                .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppUpdateUtils.installApp(MainActivity.this, appFile);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
