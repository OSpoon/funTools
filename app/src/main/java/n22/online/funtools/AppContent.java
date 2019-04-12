package n22.online.funtools;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.Utils;
import com.fm.openinstall.OpenInstall;
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
        Utils.init(this);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        if (isMainProcess()) {
            OpenInstall.init(this);
        }
    }

    public boolean isMainProcess() {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return getApplicationInfo().packageName.equals(appProcess.processName);
            }
        }
        return false;
    }
}
