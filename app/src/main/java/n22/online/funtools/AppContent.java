package n22.online.funtools;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by zhanxiaolin-n22 on 2018/10/16.
 */

public class AppContent extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);
        Utils.init(this);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }
}
