package n22.online.funtools.utils;

import android.content.Context;

import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXUtils {
    public static void startMiniProgram(Context context){
        String appId = "wxd930ea5d5a258f4f"; // 填应用AppId
        IWXAPI api = WXAPIFactory.createWXAPI(context, appId);
        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        req.userName = "gh_d43f693ca31f"; // 填小程序原始id
        // req.path = path;                  ////拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
        api.sendReq(req);
    }
}
