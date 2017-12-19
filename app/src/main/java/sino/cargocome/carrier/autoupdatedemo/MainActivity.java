package sino.cargocome.carrier.autoupdatedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.vector.update_app.UpdateAppManager;

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

    }
}
