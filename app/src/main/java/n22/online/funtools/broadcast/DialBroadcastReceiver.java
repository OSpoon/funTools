package n22.online.funtools.broadcast;

/**
 * Created by zhanxiaolin-n22 on 2019/1/31.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


import n22.online.funtools.MainActivity;

public class DialBroadcastReceiver extends BroadcastReceiver {

    private String TAG = "RuntimeSecretReceiver";
    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    private final Uri mUri = Uri.parse("android_secret_code://000");

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "on receive secret code" + " action is " + intent.getAction());
        if (intent.getAction() == null) {
            Log.i(TAG, "Null action");
            return;
        }
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Uri uri = intent.getData();
            if (uri.equals(mUri)) {
                //注意启动自己需要的APP主界面即可
                Intent mIntent = new Intent(context, MainActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mIntent);
            } else {
                Log.i(TAG, "No matched URI!");
            }
        } else {
            Log.i(TAG, "Not SECRET_CODE_ACTION!");
        }
    }
}
