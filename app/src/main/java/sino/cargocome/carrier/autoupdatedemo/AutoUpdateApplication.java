package sino.cargocome.carrier.autoupdatedemo;

import android.app.Application;

import com.zhy.http.okhttp.OkHttpUtils;

/**
 * Created by Jorgejie on 2017/12/18.
 */

public class AutoUpdateApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpUtils.getInstance()
                .init(this)
                .debug(true, "okHttp")
                .timeout(20 * 1000);
//        OkGo.getInstance().init(this);
    }
}
