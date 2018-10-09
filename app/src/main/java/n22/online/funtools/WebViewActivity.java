package n22.online.funtools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import n22.online.funtools.widget.ProgressWebView;


public class WebViewActivity extends AppCompatActivity {

    private ProgressWebView web;

    public static void startWebPage(Activity activity) {
        activity.startActivity(new Intent(activity, WebViewActivity.class));
    }

    public static void startWebPage(Activity activity, String url) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra("URl", url);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        web = (ProgressWebView) findViewById(R.id.web);
        // 设置WebView属性，能够执行JavaScript脚本
        web.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        web.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        web.getSettings().setBuiltInZoomControls(true);
        // 为图片添加放大缩小功能
        web.getSettings().setUseWideViewPort(true);
        web.setInitialScale(120);   //100代表不缩放
        web.setOnHtmlEventListener(new ProgressWebView.OnHtmlEventListener() {
            @Override
            public void onFinished(String html) {
            }

            @Override
            public void onUriLoading(Uri uri) {
            }
        });
        String url = getIntent().getStringExtra("URl");
        if (TextUtils.isEmpty(url)) {
            web.loadUrl("https://www.wenjuan.com/s/26reMr/");
        } else {
            web.loadUrl(url);
        }
    }
}
